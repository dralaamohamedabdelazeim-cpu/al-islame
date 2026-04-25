package com.muhammad.islamicapp

import android.app.KeyguardManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muhammad.islamicapp.data.PRAYERS
import com.muhammad.islamicapp.ui.theme.*
import java.io.File
import java.util.Calendar

class AzanOverlayActivity : ComponentActivity() {

    companion object {
        const val ACTION_ATHAN_COMPLETE = "com.muhammad.islamicapp.ATHAN_COMPLETE"
    }

    private val AUTO_DISMISS_DELAY = 10 * 60 * 1000L
    private val autoHandler = Handler(Looper.getMainLooper())
    private var prayerId = "fajr"

    // ── BroadcastReceiver — يستمع لإنهاء الأذان من الـ Service ──────────────
    private val athanCompleteReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopAthanAndClose()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── إظهار فوق شاشة القفل ─────────────────────────────────────────
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            km.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        prayerId = intent.getStringExtra(AzanMediaService.EXTRA_PRAYER_ID) ?: "fajr"

        // شغّل الأذان
        playAthan()

        // إغلاق تلقائي بعد 10 دقايق
        autoHandler.postDelayed({ stopAthanAndClose() }, AUTO_DISMISS_DELAY)

        setContent {
            AzanOverlayScreen(
                prayerId = prayerId,
                onStop = { stopAthanAndClose() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // استمع لـ broadcast لما الأذان يخلص
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                athanCompleteReceiver,
                IntentFilter(ACTION_ATHAN_COMPLETE),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(athanCompleteReceiver, IntentFilter(ACTION_ATHAN_COMPLETE))
        }
    }

    override fun onPause() {
        super.onPause()
        try { unregisterReceiver(athanCompleteReceiver) } catch (e: Exception) { /* ignore */ }
    }

    private fun playAthan() {
        val intent = Intent(this, AzanMediaService::class.java).apply {
            action = AzanMediaService.ACTION_PLAY
            putExtra(AzanMediaService.EXTRA_PRAYER_ID, prayerId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    fun stopAthanAndClose() {
        // أوقف الـ Service
        val stopIntent = Intent(this, AzanMediaService::class.java).apply {
            action = AzanMediaService.ACTION_STOP
        }
        startService(stopIntent)
        autoHandler.removeCallbacksAndMessages(null)
        finish()
    }

    override fun onDestroy() {
        autoHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Compose UI
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AzanOverlayScreen(prayerId: String, onStop: () -> Unit) {
    val context = LocalContext.current

    // ── اسم الصلاة (مع دعم يوم الجمعة) ──────────────────────────────────
    val prayerName = remember(prayerId) {
        if (prayerId == "dhuhr") {
            val cal = Calendar.getInstance()
            if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) "الجمعة" else "الظهر"
        } else {
            PRAYERS.find { it.id == prayerId }?.name ?: "الصلاة"
        }
    }

    // ── تحريكات ───────────────────────────────────────────────────────────
    val inf = rememberInfiniteTransition(label = "azan")
    val glowAlpha by inf.animateFloat(
        0.35f, 1f,
        infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "glow"
    )
    val pulse by inf.animateFloat(
        1f, 1.06f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "pulse"
    )

    // ── صورة الوالد رحمه الله ─────────────────────────────────────────────
    val fatherBitmap = remember {
        try {
            val f = File(context.filesDir, "father_photo.jpg")
            if (f.exists()) android.graphics.BitmapFactory.decodeFile(f.absolutePath) else null
        } catch (e: Exception) { null }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(BgColor),
        contentAlignment = Alignment.Center
    ) {
        // وهج دائري
        Box(
            modifier = Modifier
                .size(320.dp)
                .alpha(glowAlpha * 0.12f)
                .background(Brush.radialGradient(listOf(GoldColor, Color.Transparent)), CircleShape)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {

            // ── العنوان ──────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("✦ ✦ ✦", color = GoldDimColor, fontSize = 20.sp, letterSpacing = 6.sp)
                Spacer(Modifier.height(10.dp))
                Text(
                    "اللهُ أَكْبَر",
                    color = GoldColor, fontSize = 34.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.alpha(glowAlpha)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "حان وقت صلاة $prayerName",
                    color = WhiteColor, fontSize = 20.sp, textAlign = TextAlign.Center
                )
            }

            // ── صورة الوالد ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(200.dp).scale(pulse)
                    .clip(CircleShape)
                    .border(3.dp, GoldColor, CircleShape)
                    .background(CardColor),
                contentAlignment = Alignment.Center
            ) {
                if (fatherBitmap != null) {
                    Image(
                        bitmap = fatherBitmap.asImageBitmap(),
                        contentDescription = "صورة الوالد",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text("🌙", fontSize = 44.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "محمد\nعبد العظيم\nالطويل",
                            color = GoldColor, fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center, lineHeight = 26.sp
                        )
                    }
                }
            }

            // ── الدعاء ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(CardColor)
                    .border(1.dp, Color(0x33FFD700), RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Text(
                    "اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ\nوَعَافِهِ وَاعْفُ عَنْهُ",
                    color = TextDimColor, fontSize = 14.sp,
                    textAlign = TextAlign.Center, lineHeight = 24.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // ── زر الإيقاف ───────────────────────────────────────────────
            Button(
                onClick = onStop,
                modifier = Modifier.fillMaxWidth().height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x33EF4444),
                    contentColor = RedColor
                ),
                border = BorderStroke(2.dp, RedColor),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Filled.StopCircle, null, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(10.dp))
                Text("إيقاف الأذان", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
