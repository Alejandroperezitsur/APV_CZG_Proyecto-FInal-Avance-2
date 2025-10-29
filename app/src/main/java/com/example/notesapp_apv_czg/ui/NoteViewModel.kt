package com.example.notesapp_apv_czg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.data.NoteRepository
import com.example.notesapp_apv_czg.data.Multimedia
import com.example.notesapp_apv_czg.data.MultimediaDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class NoteViewModel(private val repo: NoteRepository, private val multimediaDao: MultimediaDao) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _currentNote = MutableStateFlow<Note?>(null)
    val currentNote: StateFlow<Note?> = _currentNote.asStateFlow()

    private val _currentNoteAttachments = MutableStateFlow<List<Multimedia>>(emptyList())
    val currentNoteAttachments: StateFlow<List<Multimedia>> = _currentNoteAttachments.asStateFlow()

    init {
        repo.getAllNotes()
            .onEach { _notes.value = it }
            .catch { /* handle errors */ }
            .launchIn(viewModelScope)
    }

    fun getNoteById(id: Long) {
        viewModelScope.launch {
            _currentNote.value = repo.getById(id)
            loadAttachmentsForNote(id)
        }
    }

    fun clearCurrentNote() {
        _currentNote.value = null
        _currentNoteAttachments.value = emptyList()
    }

    private fun loadAttachmentsForNote(noteId: Long) {
        viewModelScope.launch {
            _currentNoteAttachments.value = multimediaDao.getByNoteId(noteId)
        }
    }

    fun deleteAttachment(noteId: Long, uri: String) {
        viewModelScope.launch {
            multimediaDao.deleteByNoteIdAndUri(noteId, uri)
            // Refresh attachments after deletion
            _currentNoteAttachments.value = multimediaDao.getByNoteId(noteId)
        }
    }

    fun search(q: String) {
        // This is now handled locally in the UI, but the function can be kept for other purposes
        repo.search(q)
            .onEach { _notes.value = it }
            .catch { /* handle */ }
            .launchIn(viewModelScope)
    }

    fun insert(note: Note, onResult: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repo.insert(note)
            onResult(id)
        }
    }

    fun update(note: Note) {
        viewModelScope.launch { repo.update(note) }
    }

    fun delete(note: Note) {
        viewModelScope.launch { repo.delete(note) }
    }
}
