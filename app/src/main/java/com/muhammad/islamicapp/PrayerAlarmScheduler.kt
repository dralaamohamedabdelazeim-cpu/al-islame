package com.muhammad.islamicapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

object PrayerAlarmScheduler {

    fun scheduleAll(context: Context, prayerTimes: Map<String, String>) {
        prayerTimes.forEach { (prayerId, time) ->
            schedule(context, prayerId, time)
        }
    }

    fun schedule(context: Context, prayerId: String, time: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
        val hour = parts.getOrElse(0) { 0 }
        val minute = parts.getOrElse(1) { 0 }

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // لو الوقت فات النهارده، جدوله بكرة
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, AzanReceiver::class.java).apply {
            putExtra("prayer_id", prayerId)
        }
        val pi = PendingIntent.getBroadcast(
            context,
            prayerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am.canScheduleExactAlarms()) {
                am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi), pi)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
            }
        } else {
            am.setAlarmClock(AlarmManager.AlarmClockInfo(cal.timeInMillis, pi), pi)
        }
    }

    fun cancel(context: Context, prayerId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AzanReceiver::class.java)
        val pi = PendingIntent.getBroadcast(
            context,
            prayerId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}

class AzanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prayerId = intent.getStringExtra("prayer_id") ?: return

        // شغّل الـ overlay activity
        val overlayIntent = Intent(context, AzanOverlayActivity::class.java).apply {
            putExtra("prayer_id", prayerId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(overlayIntent)

        // جدول نفس الصلاة ليوم بكرة
        // (لازم تجيب الوقت من DataStore - هنعمله في AppViewModel)
    }
}
