package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.lunosfer.dreamap.data.model.Message
import io.lunosfer.dreamap.data.model.UserProfile
import io.lunosfer.dreamap.supabase.supabaseClient
import io.lunosfer.dreamap.ui.theme.*
import io.lunosfer.dreamap.ui.viewmodel.ThreadUiState
import io.lunosfer.dreamap.ui.viewmodel.ThreadViewModel
import io.github.jan.supabase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/**
 * pages/api/messages/thread.js + send.js için ekran. otherUserId
 * MainScreen.kt'deki NavHost'tan route argümanı olarak geliyor
 * (bkz. Screen.Thread). currentUserId, kendi mesajlarımızı sağda/farklı
 * renkte göstermek için supabaseClient'ten senkron okunuyor — AuthScreen
 * ve CreateDreamScreen'de zaten kanıtlanmış aynı desen
 * (supabaseClient.auth.currentUserOrNull()).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(otherUserId: String, navController: androidx.navigation.NavController) {
    val currentUserId = remember { supabaseClient.auth.currentUserOrNull()?.id }
    val viewModel: ThreadViewModel = viewModel(
        factory = ThreadViewModel.Factory(otherUserId, currentUserId)
    )
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    ThreadTopBarTitle(otherUser = state.otherUser, fallbackId = otherUserId)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Void950)
            )
        },
        bottomBar = {
            ThreadInputBar(
                isSending = state.isSending,
                onSend = viewModel::sendMessage
            )
        },
        containerColor = Void950
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isInitialLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AetherViolet)
                }
                state.loadError != null -> ThreadLoadError(message = state.loadError!!, onRetry = viewModel::retry)
                else -> ThreadMessageList(
                    state = state,
                    isOwnMessage = viewModel::isOwnMessage,
                    onLoadOlder = viewModel::loadOlder,
                    onReactMessage = viewModel::reactMessage
                )
            }
        }

        if (state.sendError != null) {
            LaunchedEffect(state.sendError) {
                // Hata birkaç saniye görünsün, sonra kendiliğinden kapansın
                kotlinx.coroutines.delay(3000)
                viewModel.dismissSendError()
            }
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.BottomCenter) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ShadowWorkRose.copy(alpha = 0.95f),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        state.sendError ?: "",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreadTopBarTitle(otherUser: UserProfile?, fallbackId: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape).background(Void800),
            contentAlignment = Alignment.Center
        ) {
            if (otherUser?.avatarUrl != null) {
                AsyncImage(
                    model = otherUser.avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    (otherUser?.nameOrFallback ?: fallbackId).take(1).uppercase(),
                    color = AstralGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            otherUser?.nameOrFallback ?: "…",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ThreadLoadError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Konuşma yüklenemedi", color = Color.White, style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily))
        Spacer(Modifier.height(8.dp))
        Text(message, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AetherViolet),
            border = androidx.compose.foundation.BorderStroke(1.dp, AetherViolet.copy(alpha = 0.4f))
        ) {
            Text("Tekrar Dene")
        }
    }
}

@Composable
private fun ThreadMessageList(
    state: ThreadUiState,
    isOwnMessage: (Message) -> Boolean,
    onLoadOlder: () -> Unit,
    onReactMessage: (String, String) -> Unit
) {
    val listState = rememberLazyListState()
    val latestState = rememberUpdatedState(state)
    val lastMessageId = state.messages.lastOrNull()?.id

    LaunchedEffect(lastMessageId) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { firstVisible ->
                val current = latestState.value
                if (firstVisible == 0 && current.hasMoreOlder && !current.isLoadingOlder) {
                    onLoadOlder()
                }
            }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (state.isLoadingOlder) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AetherViolet, modifier = Modifier.size(20.dp))
                }
            }
        }

        itemsIndexed(state.messages, key = { _, message -> message.id }) { index, message ->
            val previous = state.messages.getOrNull(index - 1)
            val showDateDivider = previous == null || dayLabel(previous.createdAt) != dayLabel(message.createdAt)

            if (showDateDivider) {
                DateDivider(label = dayLabel(message.createdAt))
            }
            MessageBubble(message = message, isOwn = isOwnMessage(message), onReact = { reaction -> onReactMessage(message.id, reaction) })
        }
    }
}

@Composable
private fun DateDivider(label: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
        Surface(shape = RoundedCornerShape(50), color = Void800) {
            Text(
                label,
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun MessageBubble(message: Message, isOwn: Boolean, onReact: (String) -> Unit) {
    var showReactions by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp),
            horizontalAlignment = if (isOwn) Alignment.End else Alignment.Start
        ) {
            Box {
                Surface(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isOwn) 16.dp else 4.dp,
                        bottomEnd = if (isOwn) 4.dp else 16.dp
                    ),
                    color = if (isOwn) AstralGold.copy(alpha = 0.9f) else Void800,
                    modifier = Modifier.clickable { showReactions = true }
                ) {
                    SelectionContainer {
                        if (message.content != null && message.content.isNotBlank()) {
                            Text(
                                text = message.content,
                                color = if (isOwn) Void950 else Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        } else if (message.attachmentType != null) {
                            Text(
                                text = attachmentLabel(message.attachmentType),
                                color = if (isOwn) Void950 else Color(0xFF94A3B8),
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                DropdownMenu(expanded = showReactions, onDismissRequest = { showReactions = false }) {
                    val reactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
                    Row(modifier = Modifier.padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        reactions.forEach { reaction ->
                            Text(
                                text = reaction,
                                fontSize = 24.sp,
                                modifier = Modifier.clickable {
                                    onReact(reaction)
                                    showReactions = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (message.reaction != null) {
                    Surface(
                        shape = CircleShape,
                        color = Void800,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Void900),
                        modifier = Modifier.padding(end = 4.dp).offset(y = (-8).dp)
                    ) {
                        Text(message.reaction, fontSize = 12.sp, modifier = Modifier.padding(4.dp))
                    }
                }
                Text(
                    text = timeLabel(message.createdAt),
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                if (isOwn) {
                    Icon(
                        imageVector = if (message.isRead) Icons.Filled.DoneAll else Icons.Filled.Done,
                        contentDescription = if (message.isRead) "Okundu" else "Gönderildi",
                        tint = if (message.isRead) Color(0xFF34D399) else Color(0xFF64748B),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreadInputBar(isSending: Boolean, onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val maxLen = 4000
    val context = LocalContext.current

    Surface(color = Void900) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = {
                    Toast.makeText(context, "Medya gönderimi yakında!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachFile,
                    contentDescription = "Dosya ekle",
                    tint = Color(0xFF94A3B8)
                )
            }
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= maxLen) text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Mesaj yaz…", color = Color.Gray) },
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AstralGold,
                    unfocusedBorderColor = Void800,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(20.dp)
            )

            val canSend = text.isNotBlank() && !isSending
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (canSend) AstralGold else Void800)
                    .clickable(enabled = canSend) {
                        onSend(text)
                        text = ""
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Void950, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Gönder",
                        tint = if (canSend) Void950 else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun attachmentLabel(type: String): String = when (type) {
    "image" -> "📷 Fotoğraf"
    "video" -> "🎥 Video"
    "file" -> "📎 Dosya"
    else -> "Ek"
}

private fun parseSupabaseTimestamp(isoTimestamp: String): java.util.Date? {
    return try {
        val withoutOffset = isoTimestamp
            .replace(Regex("[+-]\\d{2}:\\d{2}$"), "")
            .removeSuffix("Z")
        val truncated = if (withoutOffset.contains(".")) {
            val (base, fraction) = withoutOffset.split(".", limit = 2)
            base + "." + fraction.take(3).padEnd(3, '0')
        } else {
            "$withoutOffset.000"
        }
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        format.parse(truncated)
    } catch (e: Exception) {
        null
    }
}

private fun timeLabel(isoTimestamp: String): String {
    val date = parseSupabaseTimestamp(isoTimestamp) ?: return ""
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    return formatter.format(date)
}

private fun dayLabel(isoTimestamp: String): String {
    val date = parseSupabaseTimestamp(isoTimestamp) ?: return isoTimestamp
    val formatter = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).apply {
        timeZone = TimeZone.getDefault()
    }
    return formatter.format(date)
}
