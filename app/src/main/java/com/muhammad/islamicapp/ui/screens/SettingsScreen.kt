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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.islamicapp.data.*
import com.muhammad.islamicapp.ui.theme.*
import com.muhammad.islamicapp.viewmodel.AppState

@Composable
fun SettingsScreen(
    state: AppState,
    onToggleSection: (String) -> Unit,
    onSetMoazen: (String, String) -> Unit,
    onSetCalcMethod: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        // ── Moazen Section ───────────────────────────────────────────────
        SettingsSection(
            id = "moazen",
            title = "🕌 اختيار المؤذن لكل صلاة",
            expanded = state.expandSection == "moazen",
            onToggle = { onToggleSection("moazen") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PRAYERS.forEach { prayer ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text(prayer.icon, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(prayer.name, color = TextColor, fontSize = 15.sp, modifier = Modifier.width(55.dp))
                        Spacer(Modifier.width(8.dp))
                        MoazenDropdown(
                            selected = state.moazens[prayer.id] ?: MOAZENS[0],
                            onSelect = { onSetMoazen(prayer.id, it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Calc Method Section ──────────────────────────────────────────
        SettingsSection(
            id = "calc",
            title = "📐 طريقة حساب أوقات الصلاة",
            expanded = state.expandSection == "calc",
            onToggle = { onToggleSection("calc") }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CALC_METHODS.forEach { method ->
                    val selected = state.calcMethod == method
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) Brush.linearGradient(listOf(NavyColor, Color(0xFF1A3A6A)))
                                else Brush.linearGradient(listOf(CardAltColor, CardAltColor))
                            )
                            .border(1.dp, if (selected) GoldColor else Color(0x1AFFD700), RoundedCornerShape(10.dp))
                            .clickable { onSetCalcMethod(method) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) GoldColor else Color.Transparent)
                                .border(2.dp, if (selected) GoldColor else TextDimColor, RoundedCornerShape(50))
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(method, color = if (selected) GoldColor else TextDimColor, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Memorial Card ─────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Brush.linearGradient(listOf(NavyColor, Color(0xFF0A1628))))
                .border(1.dp, GoldDimColor, RoundedCornerShape(14.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "محمد عبد العظيم الطويل الإسلامي",
                    color = GoldColor, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ وَعَافِهِ وَاعْفُ عَنْهُ",
                    color = TextDimColor, fontSize = 12.sp,
                    textAlign = TextAlign.Center, lineHeight = 20.sp
                )
                Spacer(Modifier.height(8.dp))
                Text("v1.0 · حفظ تلقائي ✓", color = Color(0x59FFD700), fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun SettingsSection(id: String, title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardColor),
        border = BorderStroke(1.dp, Color(0x1AFFD700))
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = GoldColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(if (expanded) "▴" else "▾", color = GoldDimColor, fontSize = 18.sp)
            }
            AnimatedVisibility(visible = expanded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoazenDropdown(selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        Box(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardAltColor)
                .border(1.dp, Color(0x4DFFD700), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(selected, color = GoldColor, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(CardColor)
        ) {
            MOAZENS.forEach { m ->
                DropdownMenuItem(
                    text = { Text(m, color = if (m == selected) GoldColor else TextColor, fontSize = 13.sp) },
                    onClick = { onSelect(m); expanded = false },
                    modifier = Modifier.background(if (m == selected) Color(0x1AFFD700) else Color.Transparent)
                )
            }
        }
    }
}
