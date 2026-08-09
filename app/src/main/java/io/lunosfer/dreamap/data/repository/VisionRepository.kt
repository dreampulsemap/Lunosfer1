package io.lunosfer.dreamap.data.repository

import io.lunosfer.dreamap.data.model.Goal
import io.lunosfer.dreamap.data.network.NetworkModule

/** "Vizyon" sekmesi: genel keşfet akışı, mode=feed (yalnızca public hedefler). */
class VisionRepository {
    private val api = NetworkModule.api

    suspend fun loadFirstPage(): Result<List<Goal>> = runCatching {
        api.getGoalsFeed(mode = "feed", page = 0, status = null).goals
    }
}
