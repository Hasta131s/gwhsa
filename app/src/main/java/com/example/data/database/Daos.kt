package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatSessionDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long

    @Query("DELETE FROM chat_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Update
    suspend fun updateSession(session: ChatSession)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Int): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage): Long

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: Int)
}

@Dao
interface AgendaItemDao {
    @Query("SELECT * FROM agenda_items ORDER BY dateMillis ASC")
    fun getAllAgendaItems(): Flow<List<AgendaItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaItem): Long

    @Query("DELETE FROM agenda_items WHERE id = :id")
    suspend fun deleteAgendaItemById(id: Int)

    @Update
    suspend fun updateAgendaItem(item: AgendaItem)
}

@Dao
interface ProgressLogDao {
    @Query("SELECT * FROM progress_logs ORDER BY timestamp DESC")
    fun getAllProgressLogs(): Flow<List<ProgressLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressLog(log: ProgressLog): Long

    @Query("DELETE FROM progress_logs WHERE id = :id")
    suspend fun deleteProgressLogById(id: Int)

    @Query("DELETE FROM progress_logs")
    suspend fun clearLogs()
}
