package io.lunosfer.dreamap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import io.lunosfer.dreamap.service.LunosferMessagingService
import io.lunosfer.dreamap.supabase.supabaseClient
import io.lunosfer.dreamap.ui.screens.MainScreen
import io.lunosfer.dreamap.ui.theme.MyApplicationTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        supabaseClient.handleDeeplinks(intent)
        
        requestNotificationPermission()
        initFcmToken()

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun initFcmToken() {
        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    if (!token.isNull_or_blank()) {
                        Log.d("MainActivity", "FCM Token: $token")
                        LunosferMessagingService.sendTokenToServer(token)
                    }
                } else {
                    Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "FCM token initialization error", e)
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()
}

