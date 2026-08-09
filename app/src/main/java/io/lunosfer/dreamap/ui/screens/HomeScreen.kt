package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrackChanges
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
import io.lunosfer.dreamap.data.model.Dream
import io.lunosfer.dreamap.data.model.FeedItem
import io.lunosfer.dreamap.data.model.Goal
import io.lunosfer.dreamap.ui.theme.*
import io.lunosfer.dreamap.ui.viewmodel.HomeViewModel
import io.lunosfer.dreamap.ui.viewmodel.UiState

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onDreamClick: (Long) -> Unit = {}) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Void950)) {
        when (val current = state) {
            is UiState.Loading -> HomeLoading()
            is UiState.Error -> HomeError(message = current.message, onRetry = viewModel::retry)
            is UiState.Success -> HomeFeedList(items = current.data, onDreamClick = onDreamClick)
        }
    }
}

@Composable
private fun HomeLoading() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = AstralGold)
    }
}

@Composable
private fun HomeError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Akış yüklenemedi",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = Color(0xFF94A3B8),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            onClick = onRetry,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = AstralGold),
            border = BorderStroke(1.dp, AstralGold.copy(alpha = 0.4f))
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text("Tekrar Dene")
        }
    }
}

@Composable
private fun HomeFeedList(items: List<FeedItem>, onDreamClick: (Long) -> Unit) {
    if (items.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Henüz akışında bir şey yok",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Bir rüya kaydet ya da bir vizyon oluştur, burada görünsün.",
                color = Color(0xFF94A3B8),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "HOŞ GELDİN",
                    color = AstralGold.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        letterSpacing = 2.sp,
                        fontFamily = SansFontFamily
                    )
                )
                Text(
                    text = "Astral Gezgin",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Light
                    )
                )
            }
        }

        items(items, key = { it.createdAt + it.hashCode() }) { feedItem ->
            when (feedItem) {
                is FeedItem.DreamItem -> DreamFeedCard(feedItem.dream, onDreamClick)
                is FeedItem.VisionItem -> VisionFeedCard(feedItem.goal)
            }
        }
    }
}

@Composable
private fun FeedCardOwnerHeader(ownerName: String, avatarUrl: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Void800),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(ownerName.take(1).uppercase(), color = AstralGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(ownerName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

/**
 * dreams tablosundan bir kart. content'ten üretilmiş görsel (ai_image_url)
 * varsa gösterilir; image_status "broken" ise home-feed.js zaten filtreliyor
 * (bkz. pages/api/home-feed.js fetchDreams neq('image_status', 'broken')),
 * ama null olabileceğinden burada da güvenli fallback var.
 */
@Composable
private fun DreamFeedCard(dream: Dream, onDreamClick: (Long) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDreamClick(dream.id) },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Void800.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedCardOwnerHeader(
                ownerName = dream.owner?.nameOrFallback ?: "Bilinmeyen",
                avatarUrl = dream.owner?.avatarUrl
            )

            if (dream.aiImageUrl != null) {
                AsyncImage(
                    model = dream.aiImageUrl,
                    contentDescription = dream.displayTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                )
            }

            Text(
                text = dream.displayTitle,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("❤ ${dream.likesCount ?: 0}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Message, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${dream.commentsCount ?: 0}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                }
            }
        }
    }
}

/** goals tablosundan bir kart — GoalCard.jsx'in ön yüzüyle aynı alanlar (title, cover, completion). */
@Composable
private fun VisionFeedCard(goal: Goal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Void800.copy(alpha = 0.6f)),
        border = BorderStroke(1.dp, AstralGold.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedCardOwnerHeader(
                ownerName = goal.owner?.nameOrFallback ?: "Bilinmeyen",
                avatarUrl = goal.owner?.avatarUrl
            )

            Box {
                if (goal.coverImageUrl != null) {
                    AsyncImage(
                        model = goal.coverImageUrl,
                        contentDescription = goal.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Void900),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.TrackChanges, contentDescription = null, tint = AstralGold.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Void950.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("VİZYON", color = AstralGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Text(
                text = goal.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(((goal.completionPercentage ?: 0) / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .background(AstralGold)
                    )
                }
                Text("%${goal.completionPercentage ?: 0} tamamlandı", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }
    }
}
