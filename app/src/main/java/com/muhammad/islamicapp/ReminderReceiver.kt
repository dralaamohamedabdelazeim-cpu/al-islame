package com.muhammad.islamicapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "sallu_reminder"
        nm.createNotificationChannel(
            NotificationChannel(channelId, "تذكير الصلاة على النبي", NotificationManager.IMPORTANCE_HIGH)
        )
        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("اللَّهُمَّ صَلِّ عَلَى مُحَمَّدٍ")
            .setContentText("حان وقت الصلاة على النبي ﷺ")
            .setAutoCancel(true)
            .build()
        nm.notify(1001, notif)
    }
}
