package com.example.notesapp_apv_czg.data

import kotlinx.coroutines.flow.Flow

/**
 * Interface for the data layer that provides access to notes data.
 */
interface NotesRepository {
    fun getAllNotes(): Flow<List<Note>>
    fun getAllTasks(): Flow<List<Note>>
    fun search(q: String): Flow<List<Note>>
    suspend fun getById(id: Long): Note?
    suspend fun insert(note: Note): Long
    suspend fun update(note: Note)
    suspend fun delete(note: Note)
}

/**
 * Offline-first implementation of the [NotesRepository]. This repository is the single source
 * of truth for all notes data in the app.
 */
class OfflineNotesRepository(private val dao: NoteDao) : NotesRepository {
    override fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    override fun getAllTasks(): Flow<List<Note>> = dao.getAllTasks()
    override fun search(q: String): Flow<List<Note>> = dao.search(q)

    override suspend fun getById(id: Long): Note? = dao.getById(id)

    override suspend fun insert(note: Note): Long = dao.insert(note)

    override suspend fun update(note: Note) = dao.update(note)

    override suspend fun delete(note: Note) = dao.delete(note)
}
