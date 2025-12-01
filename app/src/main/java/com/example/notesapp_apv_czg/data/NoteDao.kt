package com.example.notesapp_apv_czg.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY createdAtMillis DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes ORDER BY createdAtMillis DESC")
    suspend fun getAllNotesSuspendable(): List<Note>

    @Query("SELECT * FROM notes WHERE isTask = 1 ORDER BY dueDateMillis ASC")
    fun getAllTasks(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Note?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :q || '%' OR description LIKE '%' || :q || '%' ORDER BY createdAtMillis DESC")
    fun search(q: String): Flow<List<Note>>
}
