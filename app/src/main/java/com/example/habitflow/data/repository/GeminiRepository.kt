package com.example.habitflow.data.repository

import com.example.habitflow.data.remote.Content
import com.example.habitflow.data.remote.GeminiApiService
import com.example.habitflow.data.remote.GeminiRequest
import com.example.habitflow.data.remote.GeneratedTaskItem
import com.example.habitflow.data.remote.Part
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val GEMINI_API_KEY = "AIzaSyBZO91ZZsW3aln_cxd-0YgHVzLcjt7wuVo"

open class GeminiRepository {
    private val apiService: GeminiApiService by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }

    open suspend fun generateStudyPlan(goal: String): List<GeneratedTaskItem> {
        val key = GEMINI_API_KEY
        if (key == "YOUR_GEMINI_API_KEY" || key.isBlank() || key.startsWith("AQ.")) {
            // Return mock smart micro-tasks if API key is not configured or dummy placeholder
            return listOf(
                GeneratedTaskItem("Research & outline key concepts for: $goal", 15),
                GeneratedTaskItem("Draft core implementation / notes", 25),
                GeneratedTaskItem("Review and summarize findings", 15)
            )
        }

        val prompt = "Break down the following study goal into 3 to 5 actionable micro-tasks. " +
                "For each task, provide the task title and estimated duration in minutes in this exact format: " +
                "Task Title (X mins). Goal: $goal"

        val request = GeminiRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            )
        )

        try {
            val response = apiService.generateContent(key, request)
            val textResponse = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            return parseTasksFromResponse(textResponse)
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback on error / offline
            return listOf(
                GeneratedTaskItem("Research & outline key concepts for: $goal", 15),
                GeneratedTaskItem("Draft core implementation / notes", 25),
                GeneratedTaskItem("Review and summarize findings", 15)
            )
        }
    }

    internal fun parseTasksFromResponse(response: String): List<GeneratedTaskItem> {
        val items = mutableListOf<GeneratedTaskItem>()
        val lines = response.lines()
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isNotEmpty()) {
                val regex = Regex("""^(?:\d+[\.\)]\s*)?(.+?)\s*[\(\[](\d+)\s*(?:mins?|m)?[\)\]]""", RegexOption.IGNORE_CASE)
                val match = regex.find(trimmed)
                if (match != null) {
                    val title = match.groupValues[1].trim().removePrefix("- ").removePrefix("* ")
                    val mins = match.groupValues[2].toIntOrNull() ?: 25
                    items.add(GeneratedTaskItem(title, mins))
                } else if (trimmed.contains("min") || trimmed.contains("m)")) {
                    items.add(GeneratedTaskItem(trimmed.removePrefix("- ").removePrefix("* "), 25))
                } else if (trimmed.length > 5 && !trimmed.lowercase().contains("here are")) {
                    items.add(GeneratedTaskItem(trimmed.removePrefix("- ").removePrefix("* "), 20))
                }
            }
        }
        if (items.isEmpty()) {
            items.add(GeneratedTaskItem("Study session for goal", 25))
            items.add(GeneratedTaskItem("Practice exercises", 20))
        }
        return items.take(5)
    }
}
