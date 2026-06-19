package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AgendaItem
import com.example.data.database.AppDatabase
import com.example.data.database.ChatMessage
import com.example.data.database.ChatSession
import com.example.data.database.ProgressLog
import com.example.data.network.*
import com.example.data.repository.OdevRepository
import com.example.BuildConfig
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OdevViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: OdevRepository

    // Native app modes
    // 0: Homework AI Solver
    // 1: Chatbot
    // 2: Agenda
    // 3: Progress & Report
    private val _currentMode = MutableStateFlow(0)
    val currentMode: StateFlow<Int> = _currentMode.asStateFlow()

    // --- State: Homework AI Solver ---
    private val _selectedBitmap = MutableStateFlow<Bitmap?>(null)
    val selectedBitmap: StateFlow<Bitmap?> = _selectedBitmap.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _solveResult = MutableStateFlow<OdevSolveResponse?>(null)
    val solveResult: StateFlow<OdevSolveResponse?> = _solveResult.asStateFlow()

    private val _customServerUrl = MutableStateFlow("https://bosforlab.online/upload.php")
    val customServerUrl: StateFlow<String> = _customServerUrl.asStateFlow()

    private val _uploadStatusMessage = MutableStateFlow<String?>(null)
    val uploadStatusMessage: StateFlow<String?> = _uploadStatusMessage.asStateFlow()

    // --- State: AI Chatbot ---
    val chatSessions: StateFlow<List<ChatSession>>
    
    private val _currentSessionId = MutableStateFlow<Int?>(null)
    val currentSessionId: StateFlow<Int?> = _currentSessionId.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    // --- State: Agenda ---
    val agendaItems: StateFlow<List<AgendaItem>>

    // --- State: Progress & Report ---
    val progressLogs: StateFlow<List<ProgressLog>>

    private val _aiRecommendation = MutableStateFlow<String>("")
    val aiRecommendation: StateFlow<String> = _aiRecommendation.asStateFlow()

    private val _isGeneratingRecommendation = MutableStateFlow(false)
    val isGeneratingRecommendation: StateFlow<Boolean> = _isGeneratingRecommendation.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = OdevRepository(database)

        // Load Agenda
        agendaItems = repository.getAllAgendaItems()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Load Progress Logs
        progressLogs = repository.getAllProgressLogs()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Load Chat Sessions
        chatSessions = repository.getAllSessions()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Automatically load messages when active session changes
        viewModelScope.launch {
            currentSessionId.collectLatest { sessionId ->
                if (sessionId != null) {
                    repository.getMessagesForSession(sessionId).collect { messages ->
                        _chatMessages.value = messages
                    }
                } else {
                    _chatMessages.value = emptyList()
                }
            }
        }
    }

    // Navigation mode
    fun changeMode(mode: Int) {
        _currentMode.value = mode
    }

    // --- Actions: Homework Solver ---
    fun selectBitmap(bitmap: Bitmap?) {
        _selectedBitmap.value = bitmap
        // Reset result when new image is captured/chosen
        _solveResult.value = null
    }

    fun setCustomServerUrl(url: String) {
        _customServerUrl.value = url
    }

    fun solveHomework(userNote: String) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _uploadStatusMessage.value = "Yapay zeka görseli analiz ediyor ve çözüyor..."
            try {
                val response = repository.solveHomeworkWithGemini(_selectedBitmap.value, userNote)
                _solveResult.value = response
                _uploadStatusMessage.value = "Analiz tamamlandı!"
            } catch (e: Exception) {
                _uploadStatusMessage.value = "Hata oluştu: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // Direct Upload request to user's PHP code
    fun uploadToPhpServer() {
        val bitmap = _selectedBitmap.value ?: return
        viewModelScope.launch {
            _isAnalyzing.value = true
            _uploadStatusMessage.value = "Görsel sunucuya yükleniyor..."
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val bytes = stream.toByteArray()
                
                val response = repository.uploadImageToServer(_customServerUrl.value, bytes, "odev_upload.jpg")
                if (response.success) {
                    _uploadStatusMessage.value = "Sunucu Yüklemesi Başarılı!\nGörsel: ${response.image_url}\nOCR Metni: ${response.ocr_text ?: "Okunamadı"}"
                    
                    // If server successfully uploaded it, we can trigger solver using the ocr text or solve it directly!
                    if (!response.ocr_text.isNullOrBlank()) {
                        solveHomework("Mobil Sunucu Yükleme Analizi: " + response.ocr_text)
                    }
                } else {
                    _uploadStatusMessage.value = "Sunucu hatası: ${response.message}"
                }
            } catch (e: Exception) {
                _uploadStatusMessage.value = "Bağlantı hatası: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // --- Actions: AI Chatbot ---
    fun createNewChatSession(title: String) {
        viewModelScope.launch {
            val newId = repository.insertSession(title)
            _currentSessionId.value = newId
        }
    }

    fun selectChatSession(sessionId: Int) {
        _currentSessionId.value = sessionId
    }

    fun deleteChatSession(sessionId: Int) {
        viewModelScope.launch {
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
            repository.deleteSession(sessionId)
        }
    }

    fun sendChatMessage(text: String) {
        val sessionId = _currentSessionId.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            // Save user message
            repository.insertMessage(sessionId, "user", text)
            
            _isChatGenerating.value = true
            val currentHistory = _chatMessages.value
            
            try {
                val aiResponse = repository.chatWithGemini(sessionId, text, currentHistory)
                // Save AI response
                repository.insertMessage(sessionId, "model", aiResponse)
            } catch (e: Exception) {
                repository.insertMessage(sessionId, "model", "Hata oluştu: ${e.localizedMessage}")
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    // --- Actions: Agenda ---
    fun addAgendaItem(title: String, description: String, dateMillis: Long, category: String) {
        viewModelScope.launch {
            repository.insertAgendaItem(title, description, dateMillis, category)
        }
    }

    fun toggleAgendaCompletion(item: AgendaItem, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateAgendaCompletion(item, isCompleted)
        }
    }

    fun deleteAgendaItem(id: Int) {
        viewModelScope.launch {
            repository.deleteAgendaItem(id)
        }
    }

    // --- Actions: Progress & Report ---
    fun generateAiStudyReport() {
        val currentLogs = progressLogs.value
        if (currentLogs.isEmpty()) {
            _aiRecommendation.value = "Analiz için henüz çözülmüş soru kaydı bulunmamaktadır. Ödevlerinizi analiz ederek ilk verilerinizi oluşturun!"
            return
        }

        viewModelScope.launch {
            _isGeneratingRecommendation.value = true
            try {
                // Construct progress distribution summary
                val summary = currentLogs.groupBy { it.category }.map { (category, logs) ->
                    "- $category: ${logs.size} adet soru çözüldü."
                }.joinToString("\n")

                val prompt = "Aşağıda bir ortaöğretim öğrencisinin ders çalışma ve ödev asistanı uygulaması üzerinden çözdüğü ders konuları ve miktarları verilmiştir:\n$summary\n\nYapay zeka eğitim koçu olarak bu öğrenciye güçlü yanlarını, geliştirmesi gereken ders alanlarını, özel haftalık çalışma tavsiyelerini ve ders motivasyonunu yükseltecek 2 paragraflık samimi ve profesyonel bir Türkçe analiz raporu hazırla."
                
                val key = BuildConfig.GEMINI_API_KEY
                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt)))),
                    generationConfig = GenerationConfig(temperature = 0.7f),
                    systemInstruction = Content(parts = listOf(Part(text = "Sen öğrencilerin verilerini analiz eden uzman bir eğitim danışmanısın. Tavsiyelerin yapıcı, teşvik edici ve öğretici olmalıdır.")))
                )
                
                val response = RetrofitClient.geminiService.generateContent("gemini-3.5-flash", key, request)
                _aiRecommendation.value = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Öneri raporu oluşturulamadı."
            } catch (e: Exception) {
                _aiRecommendation.value = "Hafıza Analiz Hatası: ${e.localizedMessage}. Lütfen internetinizi kontrol edin."
            } finally {
                _isGeneratingRecommendation.value = false
            }
        }
    }

    fun deleteProgressLogById(id: Int) {
        viewModelScope.launch {
            repository.deleteProgressLog(id)
        }
    }

    fun clearAllProgressHistory() {
        viewModelScope.launch {
            repository.clearAllProgressLogs()
            _aiRecommendation.value = ""
        }
    }
}
