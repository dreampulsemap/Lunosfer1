package io.lunosfer.dreamap

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.lunosfer.dreamap.supabase.supabaseClient
import io.lunosfer.dreamap.ui.screens.MainScreen
import io.lunosfer.dreamap.ui.theme.MyApplicationTheme
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        supabaseClient.handleDeeplinks(intent)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}
