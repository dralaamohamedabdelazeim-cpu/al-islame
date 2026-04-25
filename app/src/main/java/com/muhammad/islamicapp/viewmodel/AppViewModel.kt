package com.muhammad.islamicapp.viewmodel

import android.app.Application
import android.media.MediaPlayer
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.muhammad.islamicapp.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class AppState(
    val activeTab: AppTab = AppTab.HOME,
    val prayerTimes: Map<String, String> = PRAYERS.associate { it.id to it.defaultTime },
    val prayerModes: Map<String, PrayerMode> = PRAYERS.associate { it.id to PrayerMode.AZAN },
    val moazens: Map<String, String> = PRAYERS.associate { it.id to MOAZENS[0] },
    val salluCount: Int = 0,
    val salluInterval: Int = 30,
    val salluActive: Boolean = false,
    val salluNextInSecs: Int? = null,
    val azkarCounts: Map<String, Int> = emptyMap(),
    val dhikrCategory: DhikrCategory = DhikrCategory.MORNING,
    val calcMethod: String = CALC_METHODS[0],
    val expandSection: String = "moazen",
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = AppRepository(application)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private var salluJob: Job? = null
    private var mediaPlayer: MediaPlayer? = null

    init {
        viewModelScope.launch {
            repo.prefs.collect { prefs -> loadState(prefs) }
        }
    }

    private fun loadState(prefs: Preferences) {
        _state.update { cur ->
            cur.copy(
                activeTab = AppTab.entries.find {
                    it.name.lowercase() == prefs[AppRepository.KEY_ACTIVE_TAB]
                } ?: AppTab.HOME,
                salluCount = prefs[AppRepository.KEY_SALLU_COUNT] ?: 0,
                salluInterval = prefs[AppRepository.KEY_SALLU_INTERVAL] ?: 30,
                dhikrCategory = DhikrCategory.entries.find {
                    it.name.lowercase() == prefs[AppRepository.KEY_DHIKR_CATEGORY]
                } ?: DhikrCategory.MORNING,
                calcMethod = prefs[AppRepository.KEY_CALC_METHOD] ?: CALC_METHODS[0],
                expandSection = prefs[AppRepository.KEY_EXPAND_SECTION] ?: "moazen",
                prayerTimes = PRAYERS.associate { p ->
                    p.id to (prefs[AppRepository.prayerTimeKey(p.id)] ?: p.defaultTime)
                },
                prayerModes = PRAYERS.associate { p ->
                    p.id to (PrayerMode.entries.find { m ->
                        m.name.lowercase() == prefs[AppRepository.prayerModeKey(p.id)]
                    } ?: PrayerMode.AZAN)
                },
                moazens = PRAYERS.associate { p ->
                    p.id to (prefs[AppRepository.moazenKey(p.id)] ?: MOAZENS[0])
                },
                azkarCounts = buildMap {
                    AZKAR.values.flatten().forEach { z ->
                        put(z.id, prefs[AppRepository.azkarCountKey(z.id)] ?: 0)
                    }
                }
            )
        }
    }

    // ── Tab ─────────────────────────────────────────────────────────────────
    fun setTab(tab: AppTab) {
        _state.update { it.copy(activeTab = tab) }
        viewModelScope.launch { repo.setString(AppRepository.KEY_ACTIVE_TAB, tab.name.lowercase()) }
    }

    // ── Prayer Times ─────────────────────────────────────────────────────────
    fun updatePrayerTime(id: String, time: String) {
        _state.update { it.copy(prayerTimes = it.prayerTimes + (id to time)) }
        viewModelScope.launch { repo.setString(AppRepository.prayerTimeKey(id), time) }
    }

    fun cyclePrayerMode(id: String) {
        val modes = PrayerMode.entries
        val cur = _state.value.prayerModes[id] ?: PrayerMode.AZAN
        val next = modes[(modes.indexOf(cur) + 1) % modes.size]
        _state.update { it.copy(prayerModes = it.prayerModes + (id to next)) }
        viewModelScope.launch { repo.setString(AppRepository.prayerModeKey(id), next.name.lowercase()) }
    }

    fun getNextPrayer(): String {
        val cal = Calendar.getInstance()
        val nowMins = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        val times = _state.value.prayerTimes
        for (p in PRAYERS) {
            val parts = (times[p.id] ?: p.defaultTime).split(":").map { it.toIntOrNull() ?: 0 }
            if (parts[0] * 60 + parts[1] > nowMins) return p.id
        }
        return PRAYERS[0].id
    }

    // ── Sallu ────────────────────────────────────────────────────────────────
    fun incrementSallu() {
        val new = _state.value.salluCount + 1
        _state.update { it.copy(salluCount = new) }
        viewModelScope.launch { repo.setInt(AppRepository.KEY_SALLU_COUNT, new) }
    }

    fun resetSallu() {
        _state.update { it.copy(salluCount = 0) }
        viewModelScope.launch { repo.setInt(AppRepository.KEY_SALLU_COUNT, 0) }
    }

    fun setSalluInterval(mins: Int) {
        _state.update { it.copy(salluInterval = mins) }
        viewModelScope.launch { repo.setInt(AppRepository.KEY_SALLU_INTERVAL, mins) }
        if (_state.value.salluActive) { stopSalluTimer(); startSalluTimer() }
    }

    fun toggleSalluTimer() {
        if (_state.value.salluActive) stopSalluTimer() else startSalluTimer()
    }

    private fun startSalluTimer() {
        val secs = _state.value.salluInterval * 60
        _state.update { it.copy(salluActive = true, salluNextInSecs = secs) }
        salluJob = viewModelScope.launch {
            var remaining = secs
            while (true) {
                delay(1000)
                remaining--
                _state.update { it.copy(salluNextInSecs = remaining) }
                if (remaining <= 0) {
                    remaining = _state.value.salluInterval * 60
                    _state.update { it.copy(salluNextInSecs = remaining) }
                }
            }
        }
    }

    private fun stopSalluTimer() {
        salluJob?.cancel()
        salluJob = null
        _state.update { it.copy(salluActive = false, salluNextInSecs = null) }
    }

    // ── Dhikr ────────────────────────────────────────────────────────────────
    fun setDhikrCategory(cat: DhikrCategory) {
        _state.update { it.copy(dhikrCategory = cat) }
        viewModelScope.launch { repo.setString(AppRepository.KEY_DHIKR_CATEGORY, cat.name.lowercase()) }
    }

    fun doZikr(id: String, goal: Int) {
        val cur = _state.value.azkarCounts[id] ?: 0
        if (cur >= goal) return
        val newCount = cur + 1
        _state.update { it.copy(azkarCounts = it.azkarCounts + (id to newCount)) }
        viewModelScope.launch { repo.incrementAzkar(id, goal) }
    }

    fun resetDhikr() {
        val catKey = _state.value.dhikrCategory.name.lowercase().replace("_","")
        val ids = AZKAR[when(_state.value.dhikrCategory) {
            DhikrCategory.MORNING -> "morning"
            DhikrCategory.EVENING -> "evening"
            DhikrCategory.AFTER_PRAYER -> "afterPrayer"
        }]!!.map { it.id }
        _state.update { it.copy(azkarCounts = it.azkarCounts - ids.toSet()) }
        viewModelScope.launch {
            repo.resetAzkar(when(_state.value.dhikrCategory) {
                DhikrCategory.MORNING -> "morning"
                DhikrCategory.EVENING -> "evening"
                DhikrCategory.AFTER_PRAYER -> "afterPrayer"
            })
        }
    }

    // ── Settings ─────────────────────────────────────────────────────────────
    fun setMoazen(prayerId: String, moazen: String) {
        _state.update { it.copy(moazens = it.moazens + (prayerId to moazen)) }
        viewModelScope.launch { repo.setString(AppRepository.moazenKey(prayerId), moazen) }
    }

    fun setCalcMethod(method: String) {
        _state.update { it.copy(calcMethod = method) }
        viewModelScope.launch { repo.setString(AppRepository.KEY_CALC_METHOD, method) }
    }

    fun toggleSection(id: String) {
        val new = if (_state.value.expandSection == id) "" else id
        _state.update { it.copy(expandSection = new) }
        viewModelScope.launch { repo.setString(AppRepository.KEY_EXPAND_SECTION, new) }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        salluJob?.cancel()
    }
}

// Extension to schedule alarms (called from outside)
fun AppViewModel.scheduleAllPrayerAlarms(context: android.content.Context) {
    com.muhammad.islamicapp.PrayerAlarmScheduler.scheduleAll(context, state.value.prayerTimes)
}
