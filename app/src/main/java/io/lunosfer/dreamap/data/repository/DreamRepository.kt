package io.lunosfer.dreamap.data.repository

import io.lunosfer.dreamap.data.model.AnalyzeDreamRequest
import io.lunosfer.dreamap.data.model.DreamDetail
import io.lunosfer.dreamap.data.network.NetworkModule

class DreamRepository {
    private val api = NetworkModule.api

    suspend fun getDream(id: Long): Result<DreamDetail> = runCatching {
        api.getDream(id).dream
    }

    suspend fun analyzeDream(dreamId: Long, content: String, lang: String): Result<Unit> = runCatching {
        api.analyzeDream(AnalyzeDreamRequest(dreamId, content, lang))
    }
}
