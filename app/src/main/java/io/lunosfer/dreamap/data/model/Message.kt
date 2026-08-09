package io.lunosfer.dreamap.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    val id: Long,
    @SerialName("sender_id") val senderId: String,
    @SerialName("recipient_id") val recipientId: String,
    val content: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("attachment_type") val attachmentType: String? = null,
    @SerialName("attachment_name") val attachmentName: String? = null
)

/** pages/api/messages/conversations.js: her satır bir kişiyle olan son durumu özetler. */
@Serializable
data class Conversation(
    val otherUser: UserProfile,
    val lastMessage: Message,
    val unreadCount: Int
)

@Serializable
data class ConversationsResponse(
    val conversations: List<Conversation>
)

@Serializable
data class ThreadResponse(
    val messages: List<Message>,
    val otherUser: UserProfile,
    val hasMore: Boolean = false
)

@Serializable
data class UnreadCountResponse(
    val unreadCount: Int
)
