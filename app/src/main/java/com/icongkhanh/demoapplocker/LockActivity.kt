package com.icongkhanh.demoapplocker

import android.content.DialogInterface
import android.content.Intent
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.icongkhanh.demoapplocker.databinding.ActivityLockBinding
import java.util.concurrent.Executor

class LockActivity : AppCompatActivity() {

    lateinit var binding : ActivityLockBinding
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var packageNameLock: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        packageNameLock = intent.getStringExtra("packagename")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            executor = ContextCompat.getMainExecutor(this)
            val bio = BiometricPrompt.Builder(this)
                .setTitle("Biometric login for my app")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButton("Exit", executor, DialogInterface.OnClickListener { dialog, which ->
                    onBackPressed()
                })
                .build()

            val cancellationSignal = CancellationSignal()
            cancellationSignal.setOnCancelListener {
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
            }

            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    val intent = Intent(this@LockActivity, LockService::class.java)
                    intent.putExtra("packagename", packageNameLock)
                    intent.action = "com.icongkhanh.UNLOCK"
                    startService(intent)
                    finish()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }
            }

            bio.authenticate(cancellationSignal, executor, callback)
        }

//        binding = ActivityLockBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val packageName = intent.getStringExtra("pakagename")
//
//        executor = ContextCompat.getMainExecutor(this)
//        biometricPrompt = BiometricPrompt(this, executor,
//            object : BiometricPrompt.AuthenticationCallback() {
//                override fun onAuthenticationError(errorCode: Int,
//                                                   errString: CharSequence) {
//                    super.onAuthenticationError(errorCode, errString)
//                    Toast.makeText(applicationContext,
//                        "Authentication error: $errString", Toast.LENGTH_SHORT)
//                        .show()
//                }
//
//                override fun onAuthenticationSucceeded(
//                    result: BiometricPrompt.AuthenticationResult) {
//                    super.onAuthenticationSucceeded(result)
//                    Toast.makeText(applicationContext,
//                        "Authentication succeeded!", Toast.LENGTH_SHORT)
//                        .show()
//                }
//
//                override fun onAuthenticationFailed() {
//                    super.onAuthenticationFailed()
//                    Toast.makeText(applicationContext, "Authentication failed",
//                        Toast.LENGTH_SHORT)
//                        .show()
//                }
//            })
//
//        promptInfo = BiometricPrompt.PromptInfo.Builder()
//            .setTitle("Biometric login for my app")
//            .setSubtitle("Log in using your biometric credential")
//            .setNegativeButtonText("Use account password")
//            .build()
//
//        biometricPrompt.authenticate(promptInfo)
    }

    override fun onBackPressed() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
        finish()
    }
}