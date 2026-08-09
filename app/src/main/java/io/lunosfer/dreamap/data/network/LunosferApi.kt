package io.lunosfer.dreamap.data.network

import io.lunosfer.dreamap.data.model.ConversationsResponse
import io.lunosfer.dreamap.data.model.DreamsFeedResponse
import io.lunosfer.dreamap.data.model.ExploreFeedResponse
import io.lunosfer.dreamap.data.model.GoalsListResponse
import io.lunosfer.dreamap.data.model.ThreadResponse
import io.lunosfer.dreamap.data.model.UnreadCountResponse
import io.lunosfer.dreamap.data.model.VisionsFeedResponse
import io.lunosfer.dreamap.data.model.AnalyzeDreamRequest
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * lunosfer.com'daki Next.js API route'larına  karşılık gelen
 * Retrofit arayüzü. Base URL BuildConfig.SUPABASE_URL DEĞİL, web app'in
 * kendi domain'i olmalı (bkz. NetworkModule.kt) — bu route'lar Supabase'e
 * service-role client ile server tarafında bağlanıyor, cihazdan doğrudan
 * Supabase'e gidilmiyor.
 *
 * Auth: her istek AuthInterceptor tarafından enjekte edilen
 * "Authorization: Bearer <supabase_access_token>" header'ına ihtiyaç duyar
 * (bkz. lib/supabaseAdmin.js getAuthedUser). Girişsiz kullanıcı için token
 * yoksa interceptor header'ı hiç eklemez; sunucu tarafı buna göre ya misafir
 * modunda (yalnız public içerik) davranır ya da 401 döner (mode=own gibi).
 *
 * ÖNEMLİ: Retrofit'in Kotlin interface'leri implement etmek için kullandığı
 * dinamik proxy, Kotlin default parametre değerlerini (metod imzasına
 * gömülen sentetik $default çağrılarını) doğru işlemez ve runtime'da
 * NoSuchMethodError/UnsupportedOperationException fırlatabilir. Bu yüzden
 * BURADA hiçbir parametre default DEĞERE sahip değil — tüm defaultlar
 * çağıran taraf olan Repository sınıflarında (data/repository/) verilir.
 */
interface LunosferApi {

    // --- Home  ---
    // type=dreams ve type=visions ayrı çağrılıyor (bkz. HomeFeed.kt açıklaması).

    @GET("api/home-feed")
    suspend fun getHomeDreams(
        @Query("type") type: String,
        @Query("dreamsBefore") dreamsBefore: String?
    ): DreamsFeedResponse

    @GET("api/home-feed")
    suspend fun getHomeVisions(
        @Query("type") type: String,
        @Query("visionsBefore") visionsBefore: String?
    ): VisionsFeedResponse

    // --- Explore  ---

    @GET("api/explore/feed")
    suspend fun getExploreFeed(
        @Query("page") page: Int,
        @Query("rankToken") rankToken: String?,
        @Query("asOf") asOf: String?
    ): ExploreFeedResponse

    // --- Vision / Goals  ---
    // mode=feed: genel keşfet akışı (yalnızca public). "Vizyon" sekmesi bunu kullanıyor.

    @GET("api/goals/list")
    suspend fun getGoalsFeed(
        @Query("mode") mode: String,
        @Query("page") page: Int,
        @Query("status") status: String?
    ): GoalsListResponse

    // --- Messages  ---

    @GET("api/messages/conversations")
    suspend fun getConversations(): ConversationsResponse

    @GET("api/messages/thread")
    suspend fun getThread(
        @Query("with") otherUserId: String,
        @Query("before") before: String?
    ): ThreadResponse

    @GET("api/messages/unread-count")
    suspend fun getUnreadCount(): UnreadCountResponse

    @POST("api/analyze-dream")
    suspend fun analyzeDream(@Body request: AnalyzeDreamRequest)

    @GET("api/get-dream")
    suspend fun getDream(@Query("id") id: Long): io.lunosfer.dreamap.data.model.DreamDetailResponse


    @GET("api/pixabay/search")
    suspend fun searchPixabay(@Query("q") query: String): io.lunosfer.dreamap.data.model.PixabaySearchResponse

    @POST("api/dreams/pixabay-image")
    suspend fun savePixabayImage(@Body request: io.lunosfer.dreamap.data.model.PixabayImageRequest): io.lunosfer.dreamap.data.model.PixabayImageResponse

}
