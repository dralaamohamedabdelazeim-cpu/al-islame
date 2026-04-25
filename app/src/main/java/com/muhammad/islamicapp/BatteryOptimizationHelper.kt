package com.muhammad.islamicapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * مساعد لإعداد أذونات البطارية على تليفونات Xiaomi / OPPO / Vivo / Samsung
 * عشان الأذان يشتغل حتى لو التطبيق في الخلفية
 */
object BatteryOptimizationHelper {

    /**
     * شيك لو التطبيق محتاج يطلب إعفاء من توفير الطاقة
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    /**
     * افتح صفحة الإعفاء من توفير الطاقة (الأندرويد العادي)
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback: افتح إعدادات البطارية العامة
            openBatterySettings(context)
        }
    }

    /**
     * افتح إعدادات التطبيق مباشرة
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * افتح مدير بدء التشغيل التلقائي حسب ماركة التليفون
     */
    fun openAutoStartSettings(context: Context): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()

        val intents = when {
            // ── Xiaomi / Poco / Redmi ────────────────────────────────────
            brand.contains("xiaomi") || brand.contains("poco") || brand.contains("redmi")
            || manufacturer.contains("xiaomi") -> listOf(
                // MIUI AutoStart
                Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                },
                Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.securitycenter.MainActivity"
                    )
                }
            )

            // ── OPPO / Realme / OnePlus ──────────────────────────────────
            brand.contains("oppo") || brand.contains("realme") || brand.contains("oneplus")
            || manufacturer.contains("oppo") -> listOf(
                Intent().apply {
                    component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.FakeActivity"
                    )
                },
                Intent().apply {
                    component = ComponentName(
                        "com.oppo.safe",
                        "com.oppo.safe.permission.startup.StartupAppListActivity"
                    )
                },
                Intent().apply {
                    action = "com.coloros.mcs.action.VIRTUAL_DEVICE_MANAGER"
                }
            )

            // ── Vivo ─────────────────────────────────────────────────────
            brand.contains("vivo") || manufacturer.contains("vivo") -> listOf(
                Intent().apply {
                    component = ComponentName(
                        "com.vivo.permissionmanager",
                        "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"
                    )
                },
                Intent().apply {
                    component = ComponentName(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"
                    )
                }
            )

            // ── Samsung ──────────────────────────────────────────────────
            brand.contains("samsung") || manufacturer.contains("samsung") -> listOf(
                Intent().apply {
                    component = ComponentName(
                        "com.samsung.android.lool",
                        "com.samsung.android.sm.ui.battery.BatteryActivity"
                    )
                }
            )

            // ── Huawei ───────────────────────────────────────────────────
            brand.contains("huawei") || manufacturer.contains("huawei") -> listOf(
                Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                },
                Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.optimize.process.ProtectActivity"
                    )
                }
            )

            else -> emptyList()
        }

        // جرب كل intent لحد ما واحدة تشتغل
        for (intent in intents) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            try {
                val resolved = context.packageManager.resolveActivity(
                    intent, PackageManager.MATCH_DEFAULT_ONLY
                )
                if (resolved != null) {
                    context.startActivity(intent)
                    return true
                }
            } catch (e: Exception) { /* جرب الي بعدها */ }
        }
        return false
    }

    private fun openBatterySettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) { /* ignore */ }
    }

    /**
     * هل التليفون من الماركات اللي بتوقف التطبيقات؟
     */
    fun isAggressiveBrand(): Boolean {
        val brand = Build.BRAND.lowercase()
        val manufacturer = Build.MANUFACTURER.lowercase()
        return listOf("xiaomi", "poco", "redmi", "oppo", "realme", "oneplus",
            "vivo", "iqoo", "huawei", "honor").any {
            brand.contains(it) || manufacturer.contains(it)
        }
    }

    fun getBrandName(): String = Build.BRAND
}
