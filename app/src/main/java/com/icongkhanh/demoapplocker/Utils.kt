package com.icongkhanh.demoapplocker

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri

object Utils {

    fun getIconApp(packageName: String, pm: PackageManager): Drawable? {
        return try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationIcon(packageName)
        } catch (ex: PackageManager.NameNotFoundException) {
            null
        }
    }
}