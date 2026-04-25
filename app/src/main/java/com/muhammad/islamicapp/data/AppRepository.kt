package com.muhammad.islamicapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "islamic_prefs")

class AppRepository(private val context: Context) {

    // ── Keys ────────────────────────────────────────────────────────────────
    companion object {
        val KEY_ACTIVE_TAB     = stringPreferencesKey("activeTab")
        val KEY_SALLU_COUNT    = intPreferencesKey("salluCount")
        val KEY_SALLU_INTERVAL = intPreferencesKey("salluInterval")
        val KEY_DHIKR_CATEGORY = stringPreferencesKey("dhikrCategory")
        val KEY_CALC_METHOD    = stringPreferencesKey("calcMethod")
        val KEY_EXPAND_SECTION = stringPreferencesKey("expandSection")
        fun prayerTimeKey(id: String)   = stringPreferencesKey("pt_$id")
        fun prayerModeKey(id: String)   = stringPreferencesKey("pm_$id")
        fun moazenKey(id: String)       = stringPreferencesKey("mz_$id")
        fun azkarCountKey(id: String)   = intPreferencesKey("az_$id")
    }

    val prefs: Flow<Preferences> = context.dataStore.data

    suspend fun setString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }
    suspend fun setInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }
    suspend fun incrementAzkar(id: String, goal: Int) {
        context.dataStore.edit { prefs ->
            val cur = prefs[azkarCountKey(id)] ?: 0
            if (cur < goal) prefs[azkarCountKey(id)] = cur + 1
        }
    }
    suspend fun resetAzkar(category: String) {
        val ids = when(category) {
            "morning"     -> AZKAR["morning"]!!
            "evening"     -> AZKAR["evening"]!!
            else          -> AZKAR["afterPrayer"]!!
        }.map { it.id }
        context.dataStore.edit { prefs -> ids.forEach { prefs.remove(azkarCountKey(it)) } }
    }
}
