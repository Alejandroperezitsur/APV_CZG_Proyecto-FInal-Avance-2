package com.example.notesapp_apv_czg.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val dao: NoteDao) {
    fun getAllNotes(): Flow<List<Note>> = dao.getAllNotes()
    fun getAllTasks(): Flow<List<Note>> = dao.getAllTasks()
    fun search(q: String): Flow<List<Note>> = dao.search(q)

    suspend fun getById(id: Long): Note? = dao.getById(id)

    suspend fun insert(note: Note): Long = dao.insert(note)

    suspend fun update(note: Note) = dao.update(note)

    suspend fun delete(note: Note) = dao.delete(note)
}
