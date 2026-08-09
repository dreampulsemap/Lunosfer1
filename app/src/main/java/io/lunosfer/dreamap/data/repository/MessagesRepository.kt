package io.lunosfer.dreamap.data.repository

import io.lunosfer.dreamap.data.model.Conversation
import io.lunosfer.dreamap.data.network.NetworkModule

class MessagesRepository {
    private val api = NetworkModule.api

    suspend fun loadConversations(): Result<List<Conversation>> = runCatching {
        api.getConversations().conversations
    }
}
