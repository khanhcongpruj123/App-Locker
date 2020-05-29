package com.icongkhanh.demoapplocker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.icongkhanh.demoapplocker.databinding.ActivityMainBinding
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.SchedulerSupport
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity(), ListAppAdapter.OnToggleLockedListener {

    lateinit var binding: ActivityMainBinding
    lateinit var listAppAdapter: ListAppAdapter
    lateinit var loader: AppInfoLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        if (BiometricUtils.isBiometricPromptEnabled(this)) {
//            Toast.makeText(this, "App do not support biometric!", Toast.LENGTH_LONG).show()
//            binding.root.postDelayed({
//                finish()
//            }, 200)
//        } else {

            val isGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    startActivityForResult(
                        intent,
                        10
                    )
                }
            }

            val intent = Intent(this, LockService::class.java)
            startService(intent)

            loader = AppInfoLoader(this)

            listAppAdapter = ListAppAdapter(this)
            binding.listApp.apply {
                adapter = listAppAdapter.apply {
                    setOnToggleLockedListener(this@MainActivity)
                }
                layoutManager = LinearLayoutManager(this@MainActivity).apply {
                    orientation = RecyclerView.VERTICAL
                }
            }

            loader.load()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    listAppAdapter.submitList(it)
                }
//        }
    }

    override fun onToggle(isLocked: Boolean, appInfo: AppInfo) {
        val pref = getSharedPreferences(Constant.APP_LOCK, Context.MODE_PRIVATE)
        val edit = pref.edit()
        Log.d("AppLog", "${appInfo.packageName} - $isLocked")
        edit.putBoolean(appInfo.packageName, isLocked)
        edit.apply()
    }
}