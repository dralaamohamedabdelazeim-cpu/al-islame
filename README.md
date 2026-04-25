# 🕌 محمد عبد العظيم الطويل الإسلامي

تطبيق إسلامي مكرس لذكرى والدنا الغالي **محمد عبد العظيم الطويل** رحمه الله.

> اللَّهُمَّ اغْفِرْ لَهُ وَارْحَمْهُ وَعَافِهِ وَاعْفُ عَنْهُ

---

## ✨ المميزات

- **🏠 الرئيسية** — ساعة حية + أوقات الصلاة الخمسة مع إمكانية تعديل الوقت + أوضاع (أذان / صامت / مؤذن خاص)
- **📿 صلوا** — عداد الصلاة على النبي ﷺ + تذكير دوري (15 / 30 / 60 دقيقة)
- **🕌 الأذكار** — أذكار الصباح والمساء وبعد الصلاة مع عداد وهدف ودائرة تقدم
- **⚙️ الإعدادات** — اختيار المؤذن لكل صلاة + طريقة حساب أوقات الصلاة

---

## 🚀 طريقة رفع المشروع على GitHub والبناء

### الخطوة 1: إنشاء Repository جديد
1. افتح **github.com** من المتصفح
2. اضغط **New repository**
3. سمّه: `islamic-app-muhammad`
4. اجعله **Public**
5. اضغط **Create repository**

### الخطوة 2: رفع الملفات
افتح الـ repository وارفع كل محتويات هذا المجلد.

> ⚠️ **مهم:** ارفع كل الملفات والمجلدات كما هي، خصوصاً مجلد `.github/workflows/`

### الخطوة 3: تشغيل GitHub Actions
- بمجرد رفع الملفات، سيبدأ الـ build تلقائياً
- اذهب لـ **Actions** tab في الـ repository
- انتظر ظهور ✅ (حوالي 10-15 دقيقة)
- اضغط على الـ workflow واضغط **Artifacts** لتحميل الـ APK

---

## 🔧 التقنيات المستخدمة

| التقنية | الاستخدام |
|---------|-----------|
| Kotlin | لغة البرمجة الأساسية |
| Jetpack Compose | واجهة المستخدم |
| DataStore | حفظ البيانات محلياً |
| ViewModel | إدارة الحالة |
| GitHub Actions | بناء APK تلقائياً |

---

## 📁 هيكل المشروع

```
islamic_app/
├── .github/workflows/build.yml    ← GitHub Actions
├── app/src/main/
│   ├── java/com/muhammad/islamicapp/
│   │   ├── MainActivity.kt        ← الشاشة الرئيسية + Navigation
│   │   ├── ReminderReceiver.kt    ← إشعارات التذكير
│   │   ├── data/
│   │   │   ├── AppData.kt         ← البيانات والثوابت
│   │   │   └── AppRepository.kt   ← DataStore
│   │   ├── viewmodel/
│   │   │   └── AppViewModel.kt    ← منطق التطبيق
│   │   └── ui/
│   │       ├── theme/Theme.kt     ← الألوان
│   │       └── screens/
│   │           ├── HomeScreen.kt
│   │           ├── SalluScreen.kt
│   │           ├── DhikrScreen.kt
│   │           └── SettingsScreen.kt
│   └── AndroidManifest.xml
├── build.gradle.kts
├── settings.gradle.kts
└── gradlew
```

---

*رحم الله محمد عبد العظيم الطويل وأسكنه فسيح جناته* 🤲
