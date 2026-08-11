package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import io.lunosfer.dreamap.data.model.DreamDetail
import io.lunosfer.dreamap.ui.theme.*
import io.lunosfer.dreamap.ui.viewmodel.DreamDetailUiState
import io.lunosfer.dreamap.ui.viewmodel.DreamDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DreamDetailScreen(
    dreamId: Long,
    onBack: () -> Unit,
    viewModel: DreamDetailViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(dreamId) {
        viewModel.loadDream(dreamId)
    }

    Scaffold(
        containerColor = Void950
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when (val s = state) {
                is DreamDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AstralGold)
                }
                is DreamDetailUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Hata: ${s.message}", color = Color(0xFFF87171))
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = { viewModel.loadDream(dreamId) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AstralGold)
                        ) {
                            Text("Tekrar Dene")
                        }
                    }
                }
                is DreamDetailUiState.Success -> {
                    DreamDetailContent(
                        dream = s.dream,
                        onBack = onBack,
                        onRefresh = { viewModel.loadDream(dreamId) },
                        onAnalyze = { viewModel.analyzeDream(dreamId, s.dream.content, s.dream.originalLanguage ?: "en") }
                    )
                }
            }
        }
    }
}

@Composable
private fun getSlideTitle(pageIndex: Int): String {
    return when (pageIndex) {
        0 -> stringResource(id = io.lunosfer.dreamap.R.string.dream_slide_title_0)
        1 -> stringResource(id = io.lunosfer.dreamap.R.string.dream_slide_title_1)
        2 -> stringResource(id = io.lunosfer.dreamap.R.string.dream_slide_title_2)
        else -> "Detail"
    }
}

@Composable
fun DreamDetailContent(
    dream: DreamDetail,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onAnalyze: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val currentLocale = Locale.getDefault().language

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Slim header row: back arrow, date + location, and visibility badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val dateDisplay = try {
                        val date = sdf.parse(dream.dreamDate)
                        SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date ?: java.util.Date())
                    } catch (e: Exception) {
                        dream.dreamDate.take(10)
                    }

                    Text(
                        text = dateDisplay,
                        color = AstralGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (!dream.locationName.isNullOrBlank()) {
                        Text(
                            text = dream.locationName,
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Small visibility badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Void800)
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)), shape = RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = dream.visibility.uppercase(),
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp
                )
            }
        }

        // Top-left page indicator text (e.g., "Rüya Görseli (1/3)") in gray monospace style
        val slideLabel = getSlideTitle(pagerState.currentPage)
        Text(
            text = "$slideLabel (${pagerState.currentPage + 1}/3)",
            color = Color(0xFF94A3B8),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )

        // 3-Page Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { page ->
            when (page) {
                0 -> DreamImageCardPage(dream = dream)
                1 -> DreamTextCardPage(dream = dream)
                2 -> DreamAnalysisCardPage(dream = dream, onRefresh = onRefresh, onAnalyze = onAnalyze)
            }
        }
    }
}

@Composable
private fun DreamImageCardPage(
    dream: DreamDetail
) {
    val locale = Locale.getDefault().language
    val titleMap = dream.aiJungianAnalysis?.title
    val titleText = titleMap?.get(locale)
        ?: titleMap?.get("en")
        ?: dream.content.take(40)

    val archetypes = dream.aiJungianAnalysis?.archetypes
        ?: dream.tags

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Void900),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val imageUrl = dream.displayImageUrl
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Rüya Görseli",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        AstralGold.copy(alpha = 0.18f),
                                        AetherViolet.copy(alpha = 0.28f),
                                        Void900
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = AstralGold.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "LUNOSFER",
                                color = AstralGold.copy(alpha = 0.6f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }

                // Dark gradient scrim ONLY inside this image card at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Void950.copy(alpha = 0.75f),
                                    Void950.copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (titleText.isNotBlank()) {
                            Text(
                                text = titleText,
                                color = AstralGold,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = SerifFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (!archetypes.isNullOrEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(archetypes) { arch ->
                                    ChipView(text = arch, isSelected = true)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DreamTextCardPage(
    dream: DreamDetail
) {
    val locale = Locale.getDefault().language
    val titleLabel = getSlideTitle(1)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Void900),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "📖", fontSize = 18.sp)
                Text(
                    text = titleLabel,
                    color = AstralGold,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Text(
                text = dream.content,
                color = Color(0xFFE2E8F0),
                style = MaterialTheme.typography.bodyLarge.copy(
                    lineHeight = 26.sp,
                    letterSpacing = 0.2.sp
                ),
                fontSize = 15.sp
            )

            if (!dream.userSelectedSentiment.isNullOrBlank()) {
                val emotions = dream.userSelectedSentiment.split(",").map { it.trim() }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(emotions) { emotion ->
                        ChipView(text = emotion, isSelected = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun DreamAnalysisCardPage(
    dream: DreamDetail,
    onRefresh: () -> Unit,
    onAnalyze: () -> Unit
) {
    val locale = Locale.getDefault().language
    val titleLabel = getSlideTitle(2)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Void900),
        border = BorderStroke(1.dp, AetherViolet.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "✨", fontSize = 18.sp)
                    Text(
                        text = titleLabel,
                        color = AstralGold,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AetherViolet.copy(alpha = 0.2f))
                        .border(BorderStroke(0.5.dp, AetherViolet.copy(alpha = 0.4f)), shape = RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "AI JUNG",
                        color = AstralGold,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            when (dream.analysisStatus) {
                "completed" -> {
                    val analysis = dream.aiJungianAnalysis
                    if (analysis != null) {
                        val title = analysis.title?.get(locale)
                            ?: analysis.title?.get("en")
                        val summary = analysis.summary?.get(locale)
                            ?: analysis.summary?.get("en")
                            ?: ""
                        val motiv = analysis.motiv?.get(locale)
                            ?: analysis.motiv?.get("en")
                            ?: ""

                        if (!title.isNullOrBlank()) {
                            Text(
                                text = title,
                                color = AstralGold,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = SerifFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        if (summary.isNotBlank()) {
                            Text(
                                text = summary,
                                color = Color(0xFFE2E8F0),
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                            )
                        }

                        if (!analysis.sentiment.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Void800)
                                    .border(BorderStroke(0.5.dp, AstralGold.copy(alpha = 0.3f)), shape = RoundedCornerShape(50))
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "Duygu: ${analysis.sentiment}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        if (motiv.isNotBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Void800.copy(alpha = 0.6f)),
                                border = BorderStroke(1.dp, AstralGold.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "\"$motiv\"",
                                    color = AstralGold,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(14.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        if (!analysis.archetypes.isNullOrEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(analysis.archetypes) { arch ->
                                    ChipView(text = arch, isSelected = true)
                                }
                            }
                        }
                    }
                }
                "failed" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(io.lunosfer.dreamap.R.string.dream_analysis_failed),
                            color = Color(0xFFF87171),
                            fontSize = 13.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = onAnalyze,
                            colors = ButtonDefaults.buttonColors(containerColor = AstralGold)
                        ) {
                            Text(
                                stringResource(io.lunosfer.dreamap.R.string.dream_analysis_retry),
                                color = Void950,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> { // processing or null
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(io.lunosfer.dreamap.R.string.dream_analysis_pending),
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Yenile", tint = AstralGold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChipView(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AstralGold.copy(alpha = 0.2f) else Void800)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) AstralGold else Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}


