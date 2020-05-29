package com.icongkhanh.demoapplocker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable

class AppInfoLoader(private val context: Context) {

    fun load() : Flowable<List<AppInfo>> {
        return Flowable.create({emitter ->

            val listAppInfo = mutableListOf<AppInfo>()

            val packageManager = context.packageManager
            val pref = context.getSharedPreferences(Constant.APP_LOCK, Context.MODE_PRIVATE)

            val appInfo = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            appInfo.forEach { app ->
                if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0) {
                    val packageName = app.packageName
                    val name = packageManager.getApplicationLabel(app).toString()
                    val isLocked = pref.getBoolean(packageName, false)

                    listAppInfo.add(AppInfo(name, packageName, isLocked))
                    emitter.onNext(listAppInfo)
                }
            }
            emitter.onComplete()

        }, BackpressureStrategy.BUFFER)
    }
}