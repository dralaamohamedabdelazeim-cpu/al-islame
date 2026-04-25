package com.muhammad.islamicapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.islamicapp.data.SALLU_INTERVALS
import com.muhammad.islamicapp.ui.theme.*
import com.muhammad.islamicapp.viewmodel.AppState

@Composable
fun SalluScreen(
    state: AppState,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onIntervalChange: (Int) -> Unit,
    onToggleTimer: () -> Unit
) {
    val glowAnim = rememberInfiniteTransition(label = "glow")
    val glowScale by glowAnim.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ وَعَلَى آلِ مُحَمَّدٍ",
            color = TextDimColor,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(24.dp))

        // ── Counter Circle ────────────────────────────────────────────────
        var countPop by remember { mutableStateOf(false) }
        val countScale by animateFloatAsState(
            targetValue = if (countPop) 1.3f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            label = "pop",
            finishedListener = { countPop = false }
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(glowScale)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color(0xFF0D2040), NavyColor)))
                .border(3.dp, GoldColor, CircleShape)
                .clickable {
                    onIncrement()
                    countPop = true
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.salluCount.toString(),
                    fontSize = 52.sp,
                    color = GoldColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.scale(countScale)
                )
                Text("اضغط للعد", color = TextDimColor, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Reset ─────────────────────────────────────────────────────────
        OutlinedButton(
            onClick = onReset,
            border = BorderStroke(1.dp, Color(0x4DFFD700)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDimColor)
        ) {
            Text("↺ إعادة تعيين", fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))
        GoldDivider()
        SectionHeader("⏰ التذكير الدوري")
        Spacer(Modifier.height(12.dp))

        // ── Interval Selector ────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SALLU_INTERVALS.forEach { (mins, label) ->
                val selected = state.salluInterval == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) GoldColor else CardAltColor)
                        .border(1.dp, if (selected) GoldColor else Color(0x33FFD700), RoundedCornerShape(10.dp))
                        .clickable { onIntervalChange(mins) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(label, color = if (selected) BgColor else TextColor, fontSize = 13.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }

        // ── Countdown ────────────────────────────────────────────────────
        if (state.salluActive && state.salluNextInSecs != null) {
            Spacer(Modifier.height(14.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("التذكير القادم في", color = TextDimColor, fontSize = 12.sp)
                Text(
                    formatCountdown(state.salluNextInSecs),
                    color = GoldColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── Timer Toggle ─────────────────────────────────────────────────
        val active = state.salluActive
        Button(
            onClick = onToggleTimer,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (active) Color(0x33EF4444) else GoldColor,
                contentColor = if (active) RedColor else BgColor
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (active) "⏹ إيقاف التذكير" else "▶ تشغيل التذكير",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

private fun formatCountdown(secs: Int): String {
    val m = secs / 60
    val s = secs % 60
    return "%02d:%02d".format(m, s)
}
