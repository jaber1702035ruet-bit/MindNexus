package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiApiClient {
    private const val TAG = "GeminiApiClient"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val mediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun expandNode(nodeText: String, parentContext: String?): List<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured. Using mock response for $nodeText")
            return@withContext getMockSuggestions(nodeText)
        }

        val prompt = """
            You are a creative mind mapping assistant helping to expand a node.
            ${if (parentContext != null) "The parent context is: '$parentContext'." else ""}
            The current node to expand is: '$nodeText'.
            Please provide 3 to 5 highly relevant, concise, and specific child sub-nodes or actions that branch out logically from '$nodeText'.
            Return ONLY a raw JSON list of strings, for example: ["Sub-idea A", "Sub-idea B", "Sub-idea C"].
            Do NOT include markdown block markers, do NOT write any explanation before or after.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            // enforce structural JSON response if possible
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.7)
            })
        }

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(jsonRequest.toString().toRequestBody(mediaType))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string()
                if (!response.isSuccessful || body == null) {
                    Log.e(TAG, "API request failed with code ${response.code}: $body")
                    return@withContext getMockSuggestions(nodeText)
                }

                val responseJson = JSONObject(body)
                val candidates = responseJson.optJSONArray("candidates")
                val firstCandidate = candidates?.optJSONObject(0)
                val content = firstCandidate?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val textResponse = parts?.optJSONObject(0)?.optString("text") ?: ""

                Log.d(TAG, "Raw response from Gemini: $textResponse")
                
                parseSuggestionsJson(textResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during Gemini API call", e)
            getMockSuggestions(nodeText)
        }
    }

    private fun parseSuggestionsJson(rawJson: String): List<String> {
        return try {
            // strip markdown formatting if any (just in case model output isn't perfect)
            var cleanJson = rawJson.trim()
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substringAfter("```json").substringAfter("```").substringBeforeLast("```").trim()
            }
            val array = JSONArray(cleanJson)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse suggestions JSON: $rawJson", e)
            emptyList()
        }
    }

    fun getMockSuggestions(nodeText: String): List<String> {
        val lower = nodeText.lowercase().trim()
        return when {
            lower.contains("marketing") || lower.contains("promote") -> listOf(
                "Social Media Campaign",
                "SEO & Content Blog",
                "Influencer Partnerships",
                "Email Newsletter",
                "AdWords & Retargeting"
            )
            lower.contains("product") || lower.contains("roadmap") || lower.contains("features") -> listOf(
                "User Research & Interviews",
                "Prototype Mockups",
                "MVP Core Development",
                "Beta Testing Phase",
                "App Store Launch"
            )
            lower.contains("design") || lower.contains("ui") || lower.contains("ux") -> listOf(
                "Style Guide & Color Palette",
                "Wireframe Navigation Flow",
                "High-Fidelity Component Library",
                "Interactive Clickable Prototype",
                "Accessibility Audit (WCAG)"
            )
            lower.contains("code") || lower.contains("dev") || lower.contains("tech") -> listOf(
                "Set Up Database Schema",
                "API Endpoints & Integration",
                "State Management Flow",
                "Unit & Screenshot Tests",
                "CI/CD Pipeline Setup"
            )
            lower.contains("business") || lower.contains("strategy") || lower.contains("monetize") -> listOf(
                "Subscription SaaS Tiers",
                "In-App Purchases (IAP)",
                "Ad-Supported Freemium",
                "Enterprise B2B Licensing",
                "Investor Pitch Deck"
            )
            else -> listOf(
                "Research & Analysis",
                "Brainstorm Concepts",
                "Draft Initial Plan",
                "Define Key Metrics",
                "Review & Iterate"
            )
        }
    }
}
