package com.example.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// --- Gemini REST API Data Classes (Moshi) ---

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    @Json(name = "text") val text: String? = null,
    @Json(name = "inlineData") val inlineData: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "data") val data: String // Base64 encoded string
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: Content?
)

// --- Homework AI Response JSON Schema representation ---

@JsonClass(generateAdapter = true)
data class OdevSolveResponse(
    @Json(name = "question") val question: String,
    @Json(name = "correct_option") val correct_option: String,
    @Json(name = "explanation") val explanation: String,
    @Json(name = "options_breakdown") val options_breakdown: Map<String, String>,
    @Json(name = "subject_category") val subject_category: String // "Matematik", "Türkçe", "Fen Bilgisi", "Sosyal Bilgiler", "İngilizce", "Diğer"
)

// --- Custom Upload & OCR Server responses (PHP helper simulation) ---

@JsonClass(generateAdapter = true)
data class PhpUploadResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?,
    @Json(name = "image_url") val image_url: String?,
    @Json(name = "ocr_text") val ocr_text: String?
)

// --- Retrofit API Interfaces ---

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

interface CustomUploadService {
    @Multipart
    @POST("upload.php") // Or custom url provided by client at runtime
    suspend fun uploadImage(
        @Url url: String,
        @retrofit2.http.Part image: MultipartBody.Part
    ): PhpUploadResponse
}

object RetrofitClient {
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val geminiService: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    val uploadService: CustomUploadService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://bosforlab.online/") // Placeholder default
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
        retrofit.create(CustomUploadService::class.java)
    }
}
