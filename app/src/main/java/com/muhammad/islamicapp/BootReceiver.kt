package com.muhammad.islamicapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.datastore.preferences.core.stringPreferencesKey
import com.muhammad.islamicapp.data.AppRepository
import com.muhammad.islamicapp.data.PRAYERS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        // بعد إعادة التشغيل، أعد جدولة أذان كل الصلوات
        CoroutineScope(Dispatchers.IO).launch {
            val repo = AppRepository(context)
            val prefs = repo.prefs.first()
            val times = PRAYERS.associate { p ->
                p.id to (prefs[stringPreferencesKey("pt_${p.id}")] ?: p.defaultTime)
            }
            PrayerAlarmScheduler.scheduleAll(context, times)
        }
    }
}
