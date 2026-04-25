package com.muhammad.islamicapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.islamicapp.data.PRAYERS
import com.muhammad.islamicapp.data.PrayerMode
import com.muhammad.islamicapp.ui.theme.*
import com.muhammad.islamicapp.viewmodel.AppState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(state: AppState, onTimeChange: (String, String) -> Unit, onModeClick: (String) -> Unit, nextPrayerId: String) {
    var currentTime by remember { mutableStateOf(currentTimeStr()) }
    var currentDate by remember { mutableStateOf(currentDateStr()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            currentTime = currentTimeStr()
            currentDate = currentDateStr()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // ── Clock Card ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(listOf(NavyColor, Color(0xFF0A1628)))
                )
                .border(1.dp, GoldDimColor, RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentTime,
                    fontSize = 42.sp,
                    color = GoldColor,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Text(
                    text = currentDate,
                    fontSize = 13.sp,
                    color = TextDimColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
                GoldDivider()
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(GoldColor, shape = RoundedCornerShape(50))
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "الصلاة القادمة: ",
                        color = TextColor,
                        fontSize = 13.sp
                    )
                    Text(
                        text = PRAYERS.find { it.id == nextPrayerId }?.name ?: "",
                        color = GoldColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionHeader("أوقات الصلاة")
        Spacer(Modifier.height(12.dp))

        // ── Prayer Rows ──────────────────────────────────────────────────────
        PRAYERS.forEachIndexed { _, prayer ->
            val isNext = prayer.id == nextPrayerId
            val mode = state.prayerModes[prayer.id] ?: PrayerMode.AZAN
            val time = state.prayerTimes[prayer.id] ?: prayer.defaultTime

            PrayerRow(
                prayer = prayer,
                time = time,
                mode = mode,
                isNext = isNext,
                onTimeChange = { onTimeChange(prayer.id, it) },
                onModeClick = { onModeClick(prayer.id) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun PrayerRow(
    prayer: com.muhammad.islamicapp.data.Prayer,
    time: String,
    mode: PrayerMode,
    isNext: Boolean,
    onTimeChange: (String) -> Unit,
    onModeClick: () -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isNext) Brush.linearGradient(listOf(NavyColor, Color(0xFF1A3A6A)))
                else Brush.linearGradient(listOf(CardColor, CardColor))
            )
            .border(
                1.dp,
                if (isNext) GoldColor else Color(0x1AFFD700),
                RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(prayer.icon, fontSize = 22.sp)
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(prayer.name, color = if (isNext) GoldColor else TextColor, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text(prayer.nameEn, color = TextDimColor, fontSize = 11.sp)
        }
        // Time display/picker
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x1AFFD700))
                .clickable { showTimePicker = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = time,
                color = if (isNext) GoldColor else TextColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(8.dp))
        // Mode badge
        ModeChip(mode = mode, onClick = onModeClick)
    }

    if (showTimePicker) {
        TimePickerDialog(
            initialTime = time,
            onDismiss = { showTimePicker = false },
            onConfirm = { newTime -> onTimeChange(newTime); showTimePicker = false }
        )
    }
}

@Composable
fun ModeChip(mode: PrayerMode, onClick: () -> Unit) {
    val (color, label) = when (mode) {
        PrayerMode.AZAN   -> BlueColor to "أذان"
        PrayerMode.SILENT -> TextDimColor to "صامت"
        PrayerMode.CUSTOM -> GoldColor to "مؤذن خاص"
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp)
    }
}

@Composable
fun TimePickerDialog(initialTime: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    val parts = initialTime.split(":").map { it.toIntOrNull() ?: 0 }
    var hour by remember { mutableIntStateOf(parts.getOrElse(0){0}) }
    var minute by remember { mutableIntStateOf(parts.getOrElse(1){0}) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardColor,
        title = { Text("تعيين وقت الصلاة", color = GoldColor, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ساعة", color = TextDimColor, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if(hour > 0) hour-- }) {
                            Text("▼", color = GoldColor)
                        }
                        Text(
                            "%02d".format(hour),
                            color = GoldColor, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(50.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { if(hour < 23) hour++ }) {
                            Text("▲", color = GoldColor)
                        }
                    }
                }
                Text(":", color = GoldColor, fontSize = 28.sp, modifier = Modifier.padding(horizontal = 4.dp))
                // Minute
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("دقيقة", color = TextDimColor, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if(minute > 0) minute-- }) {
                            Text("▼", color = GoldColor)
                        }
                        Text(
                            "%02d".format(minute),
                            color = GoldColor, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(50.dp),
                            textAlign = TextAlign.Center
                        )
                        IconButton(onClick = { if(minute < 59) minute++ }) {
                            Text("▲", color = GoldColor)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm("%02d:%02d".format(hour, minute)) }) {
                Text("تأكيد", color = GoldColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextDimColor)
            }
        }
    )
}

// ── Helpers ─────────────────────────────────────────────────────────────────
@Composable
fun GoldDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent, GoldDimColor))
        ))
        Text("  ✦  ", color = GoldDimColor, fontSize = 10.sp)
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(GoldDimColor, Color.Transparent))
        ))
    }
}

@Composable
fun SectionHeader(text: String) {
    Column {
        Text(text, color = GoldColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Box(modifier = Modifier.width(40.dp).height(2.dp).background(GoldColor, RoundedCornerShape(2.dp)))
    }
}

private fun currentTimeStr(): String {
    val cal = Calendar.getInstance()
    return "%02d:%02d".format(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}

private fun currentDateStr(): String {
    val sdf = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
    return sdf.format(Date())
}
