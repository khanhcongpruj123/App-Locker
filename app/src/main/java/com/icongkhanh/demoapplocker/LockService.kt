package com.icongkhanh.demoapplocker

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat

class LockService : IntentService("DemoService") {

    private val CHANNEL_ID = "DemoAppLock"

    private lateinit var sUsageStatsManager: UsageStatsManager
    private lateinit var activityManager: ActivityManager
    private var packageNameUnlock: String = "com.icongkhanh.NONAME"

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

        createNotificationChannel()
        startForeground(1, buildNotification())

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
            if (packageName == "com.sec.android.app.launcher") packageNameUnlock = "com.icongkhanh.NONAME"
            Log.d("AppLog", "${packageName} - ${packageNameUnlock}")
            if (isLockedApp && packageName != packageNameUnlock) {
                val intent = Intent(this, LockActivity::class.java)
                intent.putExtra("packagename", packageName)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            Thread.sleep(100)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val layout = RemoteViews(packageName, R.layout.notification)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(layout)
            .build()
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

    /**
     * above android O, must create notification channel before create notification
     * */
    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Demo App Lock"
            val descriptionText = "description"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {

        stopForeground(true)

        super.onDestroy()
    }

}