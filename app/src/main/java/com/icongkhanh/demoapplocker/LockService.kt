package com.icongkhanh.demoapplocker

import android.app.ActivityManager
import android.app.IntentService
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log

class LockService : IntentService("DemoService") {

    private lateinit var sUsageStatsManager: UsageStatsManager
    private lateinit var activityManager: ActivityManager
    private var packageNameUnlock: String? = null

    override fun onCreate() {
        super.onCreate()

        activityManager =
            getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sUsageStatsManager =
                this.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.action
        action?.let {
            val packageName = intent.getStringExtra("packagename")
            if (action == "com.icongkhanh.UNLOCK") {
                packageNameUnlock = packageName
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onHandleIntent(intent: Intent?) {

        while(true) {
            val packageName = getLauncherTopApp(applicationContext, activityManager)
            val pref = applicationContext.getSharedPreferences(Constant.APP_LOCK, Context.MODE_PRIVATE)
            val isLockedApp = pref.getBoolean(packageName, false)
            Log.d("AppLog", "${packageName} - $isLockedApp")
            if (packageName == "com.sec.android.app.launcher") packageNameUnlock = "com.icongkhanh.NONAME"
            if (isLockedApp && packageName != packageNameUnlock) {
                val intent = Intent(this, LockActivity::class.java)
                intent.putExtra("packagename", packageName)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            Thread.sleep(100)
        }
    }

    fun getLauncherTopApp(
        context: Context?,
        activityManager: ActivityManager
    ): String? {
        //isLockTypeAccessibility = SpUtil.getInstance().getBoolean(AppConstants.LOCK_TYPE, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val appTasks =
                activityManager.getRunningTasks(1)
            if (null != appTasks && !appTasks.isEmpty()) {
                return appTasks[0].topActivity!!.packageName
            }
        } else {
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000
            var result = ""
            val event = UsageEvents.Event()
            val usageEvents: UsageEvents = sUsageStatsManager.queryEvents(beginTime, endTime)
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.packageName
                }
            }
            if (!TextUtils.isEmpty(result)) {
                return result
            }
        }
        return ""
    }

}