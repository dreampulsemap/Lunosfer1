package io.lunosfer.dreamap.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DreamDetailResponse(
    val dream: DreamDetail
)

@Serializable
data class DreamDetail(
    val id: Long,
    @SerialName("user_id") val userId: String,
    val content: String,
    @SerialName("location_name") val locationName: String? = null,
    val visibility: String,
    @SerialName("in_feed") val inFeed: Boolean,
    @SerialName("user_selected_sentiment") val userSelectedSentiment: String? = null,
    @SerialName("dream_date") val dreamDate: String,
    @SerialName("original_language") val originalLanguage: String? = null,
    val tags: List<String>? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("analysis_status") val analysisStatus: String? = null,
    @SerialName("analysis_error") val analysisError: String? = null,
    @SerialName("ai_jungian_analysis") val aiJungianAnalysis: AiJungianAnalysis? = null
)

@Serializable
data class AiJungianAnalysis(
    val title: Map<String, String>? = null,
    val summary: Map<String, String>? = null,
    val motiv: Map<String, String>? = null,
    val sentiment: String? = null,
    val archetypes: List<String>? = null
)
