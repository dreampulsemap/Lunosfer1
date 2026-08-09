package io.lunosfer.dreamap.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.lunosfer.dreamap.data.model.DreamDetail
import io.lunosfer.dreamap.ui.theme.*
import io.lunosfer.dreamap.ui.viewmodel.DreamDetailUiState
import io.lunosfer.dreamap.ui.viewmodel.DreamDetailViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Void950)
            )
        },
        containerColor = Void950
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                is DreamDetailUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = AstralGold)
                }
                is DreamDetailUiState.Error -> {
                    Text(text = "Hata: ${s.message}", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                }
                is DreamDetailUiState.Success -> {
                    DreamDetailContent(
                        dream = s.dream,
                        onRefresh = { viewModel.loadDream(dreamId) },
                        onAnalyze = { viewModel.analyzeDream(dreamId, s.dream.content, s.dream.originalLanguage ?: "en") }
                    )
                }
            }
        }
    }
}

@Composable
fun DreamDetailContent(dream: DreamDetail, onRefresh: () -> Unit, onAnalyze: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date, Location, Visibility
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateDisplay = try {
            val date = sdf.parse(dream.dreamDate)
            SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(date ?: java.util.Date())
        } catch (e: Exception) {
            dream.dreamDate
        }

        Text(text = dateDisplay, color = AstralGold, style = MaterialTheme.typography.labelLarge)
        
        if (!dream.locationName.isNullOrBlank()) {
            Text(text = dream.locationName, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Void800)
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(text = dream.visibility.uppercase(), color = Color.White, fontSize = 10.sp, letterSpacing = 1.sp)
        }

        // Content
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Void900)
        ) {
            Text(
                text = dream.content,
                color = Color.White,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp)
            )
        }

        // Emotions & Tags
        if (!dream.userSelectedSentiment.isNullOrBlank()) {
            val emotions = dream.userSelectedSentiment.split(",").map { it.trim() }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(emotions) { emotion ->
                    ChipView(text = emotion, isSelected = true)
                }
            }
        }

        if (!dream.tags.isNullOrEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dream.tags) { tag ->
                    ChipView(text = "#$tag", isSelected = false)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Analysis Section
        Text(
            text = stringResource(io.lunosfer.dreamap.R.string.dream_analysis_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily)
        )

        val locale = Locale.getDefault().language
        
        when (dream.analysisStatus) {
            "completed" -> {
                val analysis = dream.aiJungianAnalysis
                if (analysis != null) {
                    val title = analysis.title?.get(locale) ?: analysis.title?.get("en") ?: stringResource(io.lunosfer.dreamap.R.string.dream_analysis_title)
                    val summary = analysis.summary?.get(locale) ?: analysis.summary?.get("en") ?: ""
                    val motiv = analysis.motiv?.get(locale) ?: analysis.motiv?.get("en") ?: ""

                    Text(text = title, color = AstralGold, style = MaterialTheme.typography.headlineSmall.copy(fontFamily = SerifFontFamily))
                    
                    if (summary.isNotBlank()) {
                        Text(text = summary, color = Color(0xFFE2E8F0), style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
                    }

                    if (motiv.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(1.dp, AstralGold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "\"$motiv\"",
                                color = AstralGold,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (!analysis.sentiment.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Void800)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = analysis.sentiment, color = Color.White, fontSize = 12.sp)
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
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(io.lunosfer.dreamap.R.string.dream_analysis_failed), color = Color.Red)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onAnalyze,
                        colors = ButtonDefaults.buttonColors(containerColor = AstralGold)
                    ) {
                        Text(stringResource(io.lunosfer.dreamap.R.string.dream_analysis_retry), color = Void950)
                    }
                }
            }
            else -> { // processing or null
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(io.lunosfer.dreamap.R.string.dream_analysis_pending), color = Color(0xFF94A3B8))
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Yenile", tint = AstralGold)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ChipView(text: String, isSelected: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) AstralGold.copy(alpha = 0.2f) else Void800)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) AstralGold else Color.White,
            style = MaterialTheme.typography.bodySmall
        )
    }
}
