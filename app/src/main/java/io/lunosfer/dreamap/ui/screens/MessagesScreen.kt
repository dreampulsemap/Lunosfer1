package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.lunosfer.dreamap.data.model.Conversation
import io.lunosfer.dreamap.ui.theme.*
import io.lunosfer.dreamap.ui.viewmodel.MessagesViewModel
import io.lunosfer.dreamap.ui.viewmodel.UiState

@Composable
fun MessagesScreen(viewModel: MessagesViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Void950)) {
        when (val current = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AetherViolet)
            }
            is UiState.Error -> MessagesError(message = current.message, onRetry = viewModel::retry)
            is UiState.Success -> ConversationsList(conversations = current.data)
        }
    }
}

@Composable
private fun MessagesError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Mesajlar yüklenemedi", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily))
        Spacer(Modifier.height(8.dp))
        Text(message, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherViolet),
            border = BorderStroke(1.dp, AetherViolet.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tekrar Dene")
        }
    }
}

@Composable
private fun ConversationsList(conversations: List<Conversation>) {
    if (conversations.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Henüz bir konuşman yok",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Bir arkadaşına mesaj gönderdiğinde burada görünecek.",
                    color = Color(0xFF94A3B8),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(conversations, key = { it.otherUser.id }) { conversation ->
            ConversationRow(conversation)
            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        }
    }
}

/**
 * pages/api/messages/conversations.js'in döndüğü şekil: otherUser (profil),
 * lastMessage (içerik + is_read + created_at), unreadCount. Şimdilik satıra
 * tıklama davranışı yok (thread ekranı henüz eklenmedi) — bu bilinçli bir
 * kapsam sınırlaması, sonraki adımda /api/messages/thread'e bağlanacak.
 */
@Composable
private fun ConversationRow(conversation: Conversation) {
    val hasUnread = conversation.unreadCount > 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* TODO: thread ekranına navigasyon (sonraki adım) */ }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Void800),
            contentAlignment = Alignment.Center
        ) {
            if (conversation.otherUser.avatarUrl != null) {
                AsyncImage(
                    model = conversation.otherUser.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    conversation.otherUser.nameOrFallback.take(1).uppercase(),
                    color = AstralGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = conversation.otherUser.nameOrFallback,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold
            )
            Text(
                text = conversation.lastMessage.content
                    ?: if (conversation.lastMessage.attachmentType != null) "Ek gönderildi" else "",
                color = if (hasUnread) Color.White.copy(alpha = 0.85f) else Color(0xFF94A3B8),
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (hasUnread) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(ShadowWorkRose),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (conversation.unreadCount > 9) "9+" else "${conversation.unreadCount}",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
