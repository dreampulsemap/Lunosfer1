package io.lunosfer.dreamap.data.network

import android.util.Log
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.lunosfer.dreamap.supabase.supabaseClient
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Her isteğe "Authorization: Bearer <access_token>" ekler — lib/supabaseAdmin.js'deki
 * getAuthedUser tam olarak bu header'ı bekliyor.
 *
 * supabaseClient.auth.sessionStatus bir StateFlow<SessionStatus> — MainScreen.kt'de
 * zaten aynı API kanıtlanmış şekilde kullanılıyor (collectAsState ile). Burada
 * senkron OkHttp Interceptor içindeyiz (suspend değil), bu yüzden Flow'u collect
 * etmek yerine StateFlow.value ile ANLIK son değeri senkron okuyoruz — StateFlow
 * her zaman bir değere sahiptir (hot flow), .value okuması bloklamaz, bu yüzden
 * runBlocking'e gerek yok.
 *
 * Girişsiz kullanıcı (SessionStatus.Authenticated dışında herhangi bir durum:
 * NotAuthenticated, Initializing, RefreshFailure) için header hiç eklenmez;
 * sunucu tarafı buna göre misafir modunda davranır (bkz. LunosferApi.kt route
 * açıklamaları).
 */
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val status = supabaseClient.auth.sessionStatus.value
        val isAuthenticated = status is SessionStatus.Authenticated
        val token = (status as? SessionStatus.Authenticated)?.session?.accessToken
        val hasToken = token != null
        val tokenPrefix = token?.take(20) ?: "null"

        Log.d("AuthInterceptor", "sessionStatus Authenticated: $isAuthenticated, token present: $hasToken, token snippet: $tokenPrefix")

        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}
