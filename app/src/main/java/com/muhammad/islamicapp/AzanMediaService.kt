package com.muhammad.islamicapp

import android.app.*
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.File

class AzanMediaService : Service() {

    companion object {
        const val ACTION_PLAY  = "PLAY"
        const val ACTION_STOP  = "STOP"
        const val EXTRA_PRAYER_ID = "prayer_id"
        const val CHANNEL_ID   = "azan_channel"
        const val NOTIF_ID     = 2001
    }

    private var mediaPlayer: MediaPlayer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val prayerId = intent.getStringExtra(EXTRA_PRAYER_ID) ?: "fajr"
                startForeground(NOTIF_ID, buildNotification(prayerId))
                playAzan(prayerId)
            }
            ACTION_STOP -> {
                stopAzan()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun playAzan(prayerId: String) {
        stopAzan()

        val customFile  = File(filesDir, "azan_$prayerId.mp3")
        val defaultFile = File(filesDir, "azan_default.mp3")
        val fileToPlay  = when {
            customFile.exists()  -> customFile
            defaultFile.exists() -> defaultFile
            else -> { stopSelf(); return }
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(fileToPlay.absolutePath)
                prepare()
                start()

                // ── لما الأذان يخلص: أرسل broadcast للـ Activity تقفل ──
                setOnCompletionListener {
                    sendBroadcast(Intent(AzanOverlayActivity.ACTION_ATHAN_COMPLETE))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun stopAzan() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        } catch (e: Exception) { /* ignore */ }
    }

    private fun buildNotification(prayerId: String): Notification {
        val prayerName = com.muhammad.islamicapp.data.PRAYERS
            .find { it.id == prayerId }?.name ?: "الصلاة"

        val pi = PendingIntent.getActivity(
            this, 0,
            Intent(this, AzanOverlayActivity::class.java).apply {
                putExtra(EXTRA_PRAYER_ID, prayerId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // زرار إيقاف مباشرة من الإشعار
        val stopPi = PendingIntent.getService(
            this, 1,
            Intent(this, AzanMediaService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("🕌 حان وقت $prayerName")
            .setContentText("اضغط لفتح شاشة الأذان")
            .setContentIntent(pi)
            .addAction(android.R.drawable.ic_media_pause, "إيقاف الأذان", stopPi)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setFullScreenIntent(pi, true) // يفتح الشاشة كاملة مباشرة
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "أذان الصلاة", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "إشعار أوقات الصلاة"
            setSound(null, null)
            enableLights(true)
            enableVibration(false)
        }
        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    override fun onDestroy() {
        stopAzan()
        super.onDestroy()
    }
}
