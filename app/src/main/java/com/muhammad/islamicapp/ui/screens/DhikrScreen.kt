package com.muhammad.islamicapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.islamicapp.data.*
import com.muhammad.islamicapp.ui.theme.*
import com.muhammad.islamicapp.viewmodel.AppState

@Composable
fun DhikrScreen(
    state: AppState,
    onCategoryChange: (DhikrCategory) -> Unit,
    onZikr: (String, Int) -> Unit,
    onReset: () -> Unit
) {
    val cat = state.dhikrCategory
    val azkarKey = when (cat) {
        DhikrCategory.MORNING -> "morning"
        DhikrCategory.EVENING -> "evening"
        DhikrCategory.AFTER_PRAYER -> "afterPrayer"
    }
    val azkar = AZKAR[azkarKey] ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // ── Category Tabs ─────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            DhikrCategory.entries.forEach { c ->
                val selected = c == cat
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) Brush.linearGradient(listOf(NavyColor, Color(0xFF1A3A6A)))
                            else Brush.linearGradient(listOf(CardColor, CardColor))
                        )
                        .border(1.dp, if (selected) GoldColor else Color(0x26FFD700), RoundedCornerShape(10.dp))
                        .clickable { onCategoryChange(c) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(c.emoji, fontSize = 16.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(c.label, color = if (selected) GoldColor else TextDimColor,
                            fontSize = 10.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Header row ───────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            SectionHeader(cat.label)
            Spacer(Modifier.weight(1f))
            OutlinedButton(
                onClick = onReset,
                border = BorderStroke(1.dp, Color(0x4DFFD700)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextDimColor),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text("↺ إعادة", fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // ── Zikr Cards ───────────────────────────────────────────────────
        azkar.forEach { zikr ->
            val count = state.azkarCounts[zikr.id] ?: 0
            val done = count >= zikr.goal
            ZikrCard(zikr = zikr, count = count, done = done, onTap = { onZikr(zikr.id, zikr.goal) })
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
fun ZikrCard(zikr: Zikr, count: Int, done: Boolean, onTap: () -> Unit) {
    var popTrigger by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (popTrigger) 1.15f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "pop",
        finishedListener = { popTrigger = false }
    )

    val progress = (count.toFloat() / zikr.goal.toFloat()).coerceIn(0f, 1f)
    val borderColor = if (done) GreenColor else Color(0x1FFFD700)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardAltColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = zikr.text,
                color = TextColor,
                fontSize = 18.sp,
                lineHeight = 30.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = zikr.desc,
                color = TextDimColor,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Button ───────────────────────────────────────────────
                Button(
                    onClick = {
                        if (!done) { onTap(); popTrigger = true }
                    },
                    enabled = !done,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (done) Color(0x26228B22) else Brush.linearGradient(
                            listOf(NavyColor, Color(0xFF0F2A48))
                        ).let { NavyColor },
                        contentColor = if (done) GreenColor else GoldColor,
                        disabledContainerColor = Color(0x26228B22),
                        disabledContentColor = GreenColor
                    ),
                    border = BorderStroke(1.dp, if (done) GreenColor else GoldColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (done) "✓ مكتمل" else "+ ذكر", fontWeight = FontWeight.Bold)
                }

                // ── Count + Arc ──────────────────────────────────────────
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(60.dp)) {
                    Text(
                        text = count.toString(),
                        fontSize = 22.sp,
                        color = if (done) GreenColor else GoldColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.scale(scale)
                    )
                    Text("من ${zikr.goal}", color = TextDimColor, fontSize = 10.sp)
                }

                // Circular progress
                Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = if (done) GreenColor else GoldColor,
                        trackColor = Color.White.copy(alpha = 0.06f),
                        strokeWidth = 3.dp,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
