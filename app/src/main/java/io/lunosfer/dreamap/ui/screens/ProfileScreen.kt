package io.lunosfer.dreamap.ui.screens

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.lunosfer.dreamap.supabase.supabaseClient
import io.lunosfer.dreamap.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onLogout: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    
    val languages = listOf(
        "en" to "English",
        "tr" to "Türkçe",
        "es" to "Español",
        "fr" to "Français",
        "de" to "Deutsch",
        "pt" to "Português",
        "ru" to "Русский",
        "ja" to "日本語",
        "ar" to "العربية",
        "hi" to "हिन्दी",
        "zh" to "中文"
    )

    Box(modifier = Modifier.fillMaxSize().background(Void950), contentAlignment = Alignment.TopCenter) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Profile & Settings", color = AstralGold, style = MaterialTheme.typography.headlineMedium)
            
            Spacer(Modifier.height(24.dp))
            
            Text("Language", color = Color.White, style = MaterialTheme.typography.titleMedium)
            
            LazyColumn(modifier = Modifier.height(300.dp).padding(vertical = 16.dp)) {
                items(languages) { lang ->
                    TextButton(onClick = {
                        val localeList = LocaleListCompat.forLanguageTags(lang.first)
                        AppCompatDelegate.setApplicationLocales(localeList)
                        
                        coroutineScope.launch {
                            val user = supabaseClient.auth.currentUserOrNull()
                            if (user != null) {
                                supabaseClient.postgrest["user_profiles"].update({
                                    set("language", lang.first)
                                }) {
                                    filter { eq("id", user.id) }
                                }
                            }
                        }
                    }) {
                        Text(lang.second, color = AetherCyan)
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        supabaseClient.auth.signOut()
                        onLogout()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ShadowWorkRose)
            ) {
                Text("Log Out", color = Color.White)
            }
        }
    }
}
