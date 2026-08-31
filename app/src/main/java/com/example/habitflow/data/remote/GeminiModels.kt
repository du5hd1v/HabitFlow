package com.example.habitflow.data.remote

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: ContentResponse?
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    val parts: List<PartResponse>?
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    val text: String?
)

data class GeneratedTaskItem(
    val title: String,
    val estimatedMins: Int
)
