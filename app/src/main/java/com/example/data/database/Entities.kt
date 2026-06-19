package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "chat_sessions")
data class ChatSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null // For user messages containing image uploads or local file URIs
) : Serializable

@Entity(tableName = "agenda_items")
data class AgendaItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val dateMillis: Long, // Target date
    val isCompleted: Boolean = false,
    val category: String = "Matematik" // "Matematik", "Türkçe", "Fen Bilgisi", "Sosyal Bilgiler", "İngilizce", "Diğer"
) : Serializable

@Entity(tableName = "progress_logs")
data class ProgressLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val questionText: String,
    val category: String, // math, science, language, other
    val timestamp: Long = System.currentTimeMillis(),
    val isCorrect: Boolean? = null, // solved status: true = correct, false = incorrect, null = just solved/analyzed without choices
    val selectedAnswer: String? = null,
    val correctAnswer: String? = null,
    val explanation: String = ""
) : Serializable
