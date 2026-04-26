package com.muhammad.islamicapp

import android.os.Bundle
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.muhammad.islamicapp.data.AppTab
import com.muhammad.islamicapp.ui.screens.*
import com.muhammad.islamicapp.ui.theme.*
import com.muhammad.islamicapp.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            IslamicApp()
        }
    }
}

@Composable
fun IslamicApp() {
    val vm: AppViewModel = viewModel()
    val state by vm.state.collectAsState()
    val nextPrayerId = remember(state.prayerTimes) { vm.getNextPrayer() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Header ──────────────────────────────────────────────────────
            AppHeader(activeTab = state.activeTab)

            // ── Content ─────────────────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                when (state.activeTab) {
                    AppTab.HOME -> HomeScreen(
                        state = state,
                        onTimeChange = vm::updatePrayerTime,
                        onModeClick = vm::cyclePrayerMode,
                        nextPrayerId = nextPrayerId
                    )
                    AppTab.SALLU -> SalluScreen(
                        state = state,
                        onIncrement = vm::incrementSallu,
                        onReset = vm::resetSallu,
                        onIntervalChange = vm::setSalluInterval,
                        onToggleTimer = vm::toggleSalluTimer
                    )
                    AppTab.DHIKR -> DhikrScreen(
                        state = state,
                        onCategoryChange = vm::setDhikrCategory,
                        onZikr = vm::doZikr,
                        onReset = vm::resetDhikr
                    )
                    AppTab.SETTINGS -> SettingsScreen(
                        state = state,
                        onToggleSection = vm::toggleSection,
                        onSetMoazen = vm::setMoazen,
                        onSetCalcMethod = vm::setCalcMethod
                    )
                }
            }

            // ── Bottom Navigation ────────────────────────────────────────────
            BottomNavBar(activeTab = state.activeTab, onTabClick = vm::setTab)
        }
    }
}

@Composable
fun AppHeader(activeTab: AppTab) {
    val subtitle = activeTab.label
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color(0xFF0A1628), BgColor)))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color.Transparent, GoldDimColor, Color.Transparent)),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(Color.Transparent, GoldDimColor))
                ))
                Text("  ✦ ✦ ✦  ", color = GoldDimColor, fontSize = 10.sp)
                Box(modifier = Modifier.weight(1f).height(1.dp).background(
                    Brush.horizontalGradient(listOf(GoldDimColor, Color.Transparent))
                ))
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "محمد عبد العظيم الطويل الإسلامي",
                color = GoldColor, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(subtitle, color = TextDimColor, fontSize = 11.sp)
        }
    }
}

data class NavItem(val tab: AppTab, val icon: ImageVector, val iconSelected: ImageVector)

@Composable
fun BottomNavBar(activeTab: AppTab, onTabClick: (AppTab) -> Unit) {
    val items = listOf(
        NavItem(AppTab.HOME,     Icons.Outlined.Home,        Icons.Filled.Home),
        NavItem(AppTab.SALLU,    Icons.Outlined.PlayCircle,  Icons.Filled.PlayCircle),
        NavItem(AppTab.DHIKR,    Icons.Outlined.RadioButtonChecked, Icons.Filled.RadioButtonChecked),
        NavItem(AppTab.SETTINGS, Icons.Outlined.Settings,    Icons.Filled.Settings),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xF0050A14))))
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(Color.Transparent, Color(0x33FFD700), Color.Transparent)),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(vertical = 6.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                val selected = activeTab == item.tab
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabClick(item.tab) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) Color(0x2EFFD700) else Color.Transparent)
                            .border(
                                1.dp,
                                if (selected) Color(0x4DFFD700) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(6.dp)
                    ) {
                        Icon(
                            imageVector = if (selected) item.iconSelected else item.icon,
                            contentDescription = item.tab.label,
                            tint = if (selected) GoldColor else TextDimColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        item.tab.label,
                        color = if (selected) GoldColor else TextDimColor,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                    if (selected) {
                        Box(modifier = Modifier.size(4.dp).clip(RoundedCornerShape(50)).background(GoldColor))
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Battery Optimization Dialog (Xiaomi / OPPO / Poco / Vivo)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun BatteryOptimizationDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val isAggressive = remember { BatteryOptimizationHelper.isAggressiveBrand() }
    val brand = remember { BatteryOptimizationHelper.getBrandName() }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardColor,
        title = {
            Text(
                "⚠️ مهم لتشغيل الأذان",
                color = GoldColor, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "لضمان عمل الأذان على تليفون $brand، احنا محتاجين:",
                    color = TextColor, fontSize = 14.sp
                )
                listOf(
                    "✅ إعفاء التطبيق من توفير الطاقة",
                    "✅ السماح بالتشغيل التلقائي (AutoStart)",
                    "✅ تثبيت التطبيق في الخلفية"
                ).forEach { step ->
                    Text(step, color = TextDimColor, fontSize = 13.sp)
                }
                if (isAggressive) {
                    Text(
                        "⚠️ تليفونات $brand بتوقف التطبيقات في الخلفية — الخطوات دي ضرورية جداً!",
                        color = RedColor, fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldColor, contentColor = BgColor)
                ) { Text("توفير الطاقة", fontWeight = FontWeight.Bold) }

                if (isAggressive) {
                    OutlinedButton(
                        onClick = {
                            val opened = BatteryOptimizationHelper.openAutoStartSettings(context)
                            if (!opened) BatteryOptimizationHelper.openAppSettings(context)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, GoldDimColor),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldColor)
                    ) { Text("التشغيل التلقائي (AutoStart)") }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("لاحقاً", color = TextDimColor) }
            }
        }
    )
}
