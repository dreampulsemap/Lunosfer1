package io.lunosfer.dreamap.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PixabaySearchResponse(
    val total: Int = 0,
    val totalHits: Int = 0,
    val hits: List<PixabayHit> = emptyList()
)

@Serializable
data class PixabayHit(
    val id: Long,
    val tags: String? = null,
    val webformatURL: String,
    val previewURL: String? = null,
    val user: String,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0
)

@Serializable
data class PixabayImageRequest(
    val pixabayId: Long,
    val imageUrl: String,
    val tags: String,
    val pixabayUser: String,
    val width: Int,
    val height: Int
)

@Serializable
data class PixabayImageResponse(
    val url: String
)
