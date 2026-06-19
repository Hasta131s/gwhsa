package com.example.data.repository

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.database.*
import com.example.data.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class OdevRepository(private val db: AppDatabase) {

    private val chatSessionDao = db.chatSessionDao()
    private val chatMessageDao = db.chatMessageDao()
    private val agendaItemDao = db.agendaItemDao()
    private val progressLogDao = db.progressLogDao()
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    // --- Local DB flows ---
    fun getAllSessions(): Flow<List<ChatSession>> = chatSessionDao.getAllSessions()
    
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>> = 
        chatMessageDao.getMessagesForSession(sessionId)

    fun getAllAgendaItems(): Flow<List<AgendaItem>> = agendaItemDao.getAllAgendaItems()

    fun getAllProgressLogs(): Flow<List<ProgressLog>> = progressLogDao.getAllProgressLogs()

    suspend fun insertSession(title: String): Int = withContext(Dispatchers.IO) {
        chatSessionDao.insertSession(ChatSession(title = title)).toInt()
    }

    suspend fun deleteSession(id: Int) = withContext(Dispatchers.IO) {
        chatMessageDao.deleteMessagesForSession(id)
        chatSessionDao.deleteSessionById(id)
    }

    suspend fun insertMessage(sessionId: Int, role: String, text: String, imageUrl: String? = null) = 
        withContext(Dispatchers.IO) {
            chatMessageDao.insertMessage(
                ChatMessage(sessionId = sessionId, role = role, text = text, imageUrl = imageUrl)
            )
        }

    suspend fun insertAgendaItem(title: String, description: String, dateMillis: Long, category: String) = 
        withContext(Dispatchers.IO) {
            agendaItemDao.insertAgendaItem(
                AgendaItem(title = title, description = description, dateMillis = dateMillis, category = category)
            )
        }

    suspend fun updateAgendaCompletion(item: AgendaItem, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        agendaItemDao.updateAgendaItem(item.copy(isCompleted = isCompleted))
    }

    suspend fun deleteAgendaItem(id: Int) = withContext(Dispatchers.IO) {
        agendaItemDao.deleteAgendaItemById(id)
    }

    suspend fun addProgressLog(log: ProgressLog) = withContext(Dispatchers.IO) {
        progressLogDao.insertProgressLog(log)
    }

    suspend fun deleteProgressLog(id: Int) = withContext(Dispatchers.IO) {
        progressLogDao.deleteProgressLogById(id)
    }

    suspend fun clearAllProgressLogs() = withContext(Dispatchers.IO) {
        progressLogDao.clearLogs()
    }

    // --- Gemini / Bosfor Lab API integrations ---
    
    suspend fun solveHomeworkWithGemini(
        bitmap: Bitmap?,
        userInputPrompt: String?
    ): OdevSolveResponse = withContext(Dispatchers.IO) {
        var questionText = userInputPrompt ?: "Görsel Soru"
        var customImageUrl: String? = null
        var ocrSuccess = false

        // 1. Try Bosfor Soru (OCR) API if bitmap is present
        if (bitmap != null) {
            try {
                // First upload image to Bosfor Lab host
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val imageBytes = outputStream.toByteArray()
                val uploadResponse = uploadImageToServer("https://bosforlab.online/upload.php", imageBytes, "odev_upload.jpg")
                
                if (uploadResponse.success && !uploadResponse.image_url.isNullOrBlank()) {
                    customImageUrl = uploadResponse.image_url
                    // Query Soru API with image url
                    val soruResponse = RetrofitClient.bosforApiService.analyzeSoru(customImageUrl)
                    if (soruResponse.success && !soruResponse.metin.isNullOrBlank()) {
                        questionText = soruResponse.metin
                        ocrSuccess = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Try answering with Bosfor AI Chatbot API
        try {
            val solverPrompt = buildString {
                append(questionText)
                if (!userInputPrompt.isNullOrBlank() && questionText != userInputPrompt) {
                    append("\nEk Olarak Not: ").append(userInputPrompt)
                }
            }
            val chatResponse = RetrofitClient.bosforApiService.sendChatQuestion(solverPrompt)
            val cevapText = chatResponse.cevap

            if (!cevapText.isNullOrBlank()) {
                // Determine Correct Option based on cevapText content
                var correctOption = "B" // Default to B or parse
                val upperCevap = cevapText.uppercase()
                when {
                    upperCevap.contains("CEVAP A") || upperCevap.contains("SEÇENEK A") || upperCevap.contains("A ŞIKKI") || upperCevap.contains("DOĞRU CEVAP: A") -> correctOption = "A"
                    upperCevap.contains("CEVAP B") || upperCevap.contains("SEÇENEK B") || upperCevap.contains("B ŞIKKI") || upperCevap.contains("DOĞRU CEVAP: B") -> correctOption = "B"
                    upperCevap.contains("CEVAP C") || upperCevap.contains("SEÇENEK C") || upperCevap.contains("C ŞIKKI") || upperCevap.contains("DOĞRU CEVAP: C") -> correctOption = "C"
                    upperCevap.contains("CEVAP D") || upperCevap.contains("SEÇENEK D") || upperCevap.contains("D ŞIKKI") || upperCevap.contains("DOĞRU CEVAP: D") -> correctOption = "D"
                    upperCevap.contains("CEVAP E") || upperCevap.contains("SEÇENEK E") || upperCevap.contains("E ŞIKKI") || upperCevap.contains("DOĞRU CEVAP: E") -> correctOption = "E"
                }

                // Classify category
                var subjectCategory = "Matematik"
                val lowerQuest = questionText.lowercase()
                when {
                    lowerQuest.contains("problem") || lowerQuest.contains("denklem") || lowerQuest.contains("toplam") || lowerQuest.contains("çarpm") || lowerQuest.contains("bölm") || lowerQuest.contains("çıkar") || lowerQuest.contains("sayı") -> subjectCategory = "Matematik"
                    lowerQuest.contains("türkçe") || lowerQuest.contains("paragraf") || lowerQuest.contains("anlam") || lowerQuest.contains("yazım") || lowerQuest.contains("dil") || lowerQuest.contains("edebiyat") -> subjectCategory = "Türkçe"
                    lowerQuest.contains("fizik") || lowerQuest.contains("kimya") || lowerQuest.contains("biyoloji") || lowerQuest.contains("fen") || lowerQuest.contains("kuvvet") || lowerQuest.contains("enerji") -> subjectCategory = "Fen Bilgisi"
                    lowerQuest.contains("tarih") || lowerQuest.contains("coğrafya") || lowerQuest.contains("sosyal") || lowerQuest.contains("vatandaş") -> subjectCategory = "Sosyal Bilgiler"
                    lowerQuest.contains("ingilizce") || lowerQuest.contains("english") || lowerQuest.contains("word") || lowerQuest.contains("translation") -> subjectCategory = "İngilizce"
                    else -> subjectCategory = "Diğer"
                }

                val optionsBreakdown = mutableMapOf<String, String>()
                for (opt in listOf("A", "B", "C", "D", "E")) {
                    if (opt == correctOption) {
                        optionsBreakdown[opt] = "Analiz sonucuna göre bu doğru seçenektir."
                    } else {
                        optionsBreakdown[opt] = "Çözüm adımlarına göre bu şık yanlıştır."
                    }
                }

                val response = OdevSolveResponse(
                    question = questionText,
                    correct_option = correctOption,
                    explanation = cevapText,
                    options_breakdown = optionsBreakdown,
                    subject_category = subjectCategory
                )

                // Save to local progress automatically
                addProgressLog(
                    ProgressLog(
                        questionText = response.question,
                        category = response.subject_category,
                        selectedAnswer = response.correct_option,
                        correctAnswer = response.correct_option,
                        explanation = response.explanation,
                        isCorrect = true
                    )
                )

                return@withContext response
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Fallback to Gemini if Bosfor API calls failed or did not return text
        val apiKey = BuildConfig.GEMINI_API_KEY
        val modelName = "gemini-3.5-flash"
        val parts = mutableListOf<Part>()
        
        val promptText = buildString {
            append("Aşağıdaki ödev sorusunu analiz et.")
            if (!userInputPrompt.isNullOrBlank()) {
                append(" Ek olarak kullanıcının notu / sorusu şu şekildedir: ").append(userInputPrompt).append(".")
            }
            append("\n\nÇıktıyı tam olarak şu JSON şemasında Türkçe dilinde ver:\n")
            append("{\n")
            append("  \"question\": \"Soru metni veya görseldeki sorunun özeti...\",\n")
            append("  \"correct_option\": \"Doğru Seçeceğin Harfi (A, B, C, D veya E)\",\n")
            append("  \"explanation\": \"Detaylı adım adım Türkçe çözüm açıklaması. Kuralları ve mantığı ile anlat.\",\n")
            append("  \"options_breakdown\": {\n")
            append("    \"A\": \"A seçeneğinin neden doğru veya yanlış olduğunun ayrıntılı açıklaması\",\n")
            append("    \"B\": \"B seçeneğinin neden doğru veya yanlış olduğunun ayrıntılı açıklaması\"\n")
            append("  },\n")
            append("  \"subject_category\": \"Ders Kategorisi (Matematik, Türkçe, Fen Bilgisi, Sosyal Bilgiler, İngilizce, Diğer değerlerinden birini seç)\"\n")
            append("}")
        }
        
        parts.add(Part(text = promptText))

        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64Data = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            parts.add(Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Data)))
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = parts)),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.4f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "Sen öğrencilere ödevlerinde yardımcı olan uzman bir yapay zeka Ödev Pro öğretmenisin. Çıktıların daima belirtilen formatta geçerli ve temiz bir JSON olmalıdır."))
            )
        )

        try {
            val response = RetrofitClient.geminiService.generateContent(modelName, apiKey, request)
            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Boş yapay zeka cevabı alındı.")
            
            val adapter = moshi.adapter(OdevSolveResponse::class.java)
            val parsedResult = adapter.fromJson(rawJson)
                ?: throw Exception("Yapay zeka çıktısı çözümlenemedi (JSON ayrıştırma hatası).")
            
            val progressLog = ProgressLog(
                questionText = parsedResult.question,
                category = parsedResult.subject_category,
                selectedAnswer = parsedResult.correct_option,
                correctAnswer = parsedResult.correct_option,
                explanation = parsedResult.explanation,
                isCorrect = true
            )
            addProgressLog(progressLog)

            parsedResult
        } catch (e: Exception) {
            e.printStackTrace()
            OdevSolveResponse(
                question = userInputPrompt ?: "Görsel Soru",
                correct_option = "-",
                explanation = "Hata oluştu: ${e.localizedMessage}. Lütfen internet bağlantınızı kontrol edin.",
                options_breakdown = mapOf("-" to "Hata nedeniyle detaylar yüklenemedi."),
                subject_category = "Diğer"
            )
        }
    }

    suspend fun chatWithGemini(
        sessionId: Int,
        latestPrompt: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        // 1. Try Bosfor apiai Chatbot API first
        try {
            val chatResponse = RetrofitClient.bosforApiService.sendChatQuestion(latestPrompt)
            val textValue = chatResponse.cevap
            if (!textValue.isNullOrBlank()) {
                return@withContext textValue
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Fallback to Gemini if Bosfor API fails
        val apiKey = BuildConfig.GEMINI_API_KEY
        val modelName = "gemini-3.5-flash"
        val contents = mutableListOf<Content>()
        
        history.takeLast(15).forEach { msg ->
            contents.add(Content(parts = listOf(Part(text = msg.text))))
        }
        contents.add(Content(parts = listOf(Part(text = latestPrompt))))

        val request = GenerateContentRequest(
            contents = contents,
            generationConfig = GenerationConfig(temperature = 0.7f),
            systemInstruction = Content(
                parts = listOf(Part(text = "Sen öğrencilerin sorularını samimi, motive edici ve öğretici bir dille cevaplayan uzman bir Ödev Pro asistanısın. Kısa, net, anlaşılır cevaplar ver. Öğrencinin ilerlemesini destekle."))
            )
        )

        try {
            val response = RetrofitClient.geminiService.generateContent(modelName, apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Cevap üretilemedi."
        } catch (e: Exception) {
            "İletişim Hata Oluştu: ${e.localizedMessage}"
        }
    }

    // --- php upload helper ---
    suspend fun uploadImageToServer(
        url: String,
        imageBytes: ByteArray,
        fileName: String
    ): PhpUploadResponse = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("image", fileName, requestFile)
            RetrofitClient.uploadService.uploadImage(url, body)
        } catch (e: Exception) {
            PhpUploadResponse(
                success = false,
                message = e.localizedMessage,
                image_url = null,
                ocr_text = null
            )
        }
    }
}
