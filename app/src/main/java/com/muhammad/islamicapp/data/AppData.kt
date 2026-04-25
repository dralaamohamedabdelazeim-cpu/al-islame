package com.muhammad.islamicapp.data

// ── Prayer ──────────────────────────────────────────────────────────────────
data class Prayer(
    val id: String,
    val name: String,
    val nameEn: String,
    val icon: String,
    val defaultTime: String
)

val PRAYERS = listOf(
    Prayer("fajr",    "الفجر",  "Fajr",    "🌙", "04:32"),
    Prayer("dhuhr",   "الظهر",  "Dhuhr",   "☀️", "12:15"),
    Prayer("asr",     "العصر",  "Asr",     "🌤", "15:48"),
    Prayer("maghrib", "المغرب", "Maghrib", "🌅", "18:22"),
    Prayer("isha",    "العشاء", "Isha",    "🌙", "19:55"),
)

// ── Azkar ───────────────────────────────────────────────────────────────────
data class Zikr(val id: String, val text: String, val desc: String, val goal: Int)

val AZKAR: Map<String, List<Zikr>> = mapOf(
    "morning" to listOf(
        Zikr("m1", "أَعُوذُ بِاللهِ مِنَ الشَّيطَانِ الرَّجِيمِ",      "أعوذ بالله من الشيطان الرجيم", 1),
        Zikr("m2", "اللَّهُمَّ بِكَ أَصْبَحْنَا وَبِكَ أَمْسَيْنَا",   "دعاء الصباح",                  1),
        Zikr("m3", "سُبْحَانَ اللهِ وَبِحَمْدِهِ",                       "تسبيح الصباح",                 100),
        Zikr("m4", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ","التهليل",                      10),
        Zikr("m5", "أَسْتَغْفِرُ اللهَ وَأَتُوبُ إِلَيْهِ",             "الاستغفار",                    100),
    ),
    "evening" to listOf(
        Zikr("e1", "اللَّهُمَّ بِكَ أَمْسَيْنَا وَبِكَ أَصْبَحْنَا",   "دعاء المساء",   1),
        Zikr("e2", "أَعُوذُ بِكَلِمَاتِ اللهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ", "التعوذ مساءً", 3),
        Zikr("e3", "سُبْحَانَ اللهِ وَبِحَمْدِهِ",                       "تسبيح المساء",  100),
        Zikr("e4", "اللَّهُمَّ عَافِنِي فِي بَدَنِي",                    "دعاء العافية",  3),
    ),
    "afterPrayer" to listOf(
        Zikr("p1", "سُبْحَانَ اللهِ",                                    "التسبيح",       33),
        Zikr("p2", "الْحَمْدُ لِلَّهِ",                                  "الحمد",          33),
        Zikr("p3", "اللهُ أَكْبَرُ",                                     "التكبير",        33),
        Zikr("p4", "لَا إِلَهَ إِلَّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ","التهليل",        10),
        Zikr("p5", "اللَّهُمَّ أَنْتَ السَّلَامُ",                       "دعاء الانصراف",  1),
    )
)

// ── Constants ────────────────────────────────────────────────────────────────
val MOAZENS = listOf("عبد الباسط", "مشاري العفاسي", "سعد الغامدي", "ماهر المعيقلي", "رعد الكردي", "رفع صوت مخصص")
val CALC_METHODS = listOf("رابطة العالم الإسلامي", "الهيئة الإسلامية لأمريكا الشمالية", "الأزهر الشريف", "أم القرى", "كراتشي")
val SALLU_INTERVALS = listOf(15 to "١٥ دقيقة", 30 to "٣٠ دقيقة", 60 to "ساعة")

enum class PrayerMode(val label: String) {
    AZAN("أذان"), SILENT("صامت"), CUSTOM("مؤذن خاص")
}

enum class AppTab(val label: String) {
    HOME("الرئيسية"), SALLU("صلوا"), DHIKR("الأذكار"), SETTINGS("الإعدادات")
}

enum class DhikrCategory(val label: String, val emoji: String) {
    MORNING("أذكار الصباح", "🌅"),
    EVENING("أذكار المساء", "🌙"),
    AFTER_PRAYER("أذكار بعد الصلاة", "🕌")
}
