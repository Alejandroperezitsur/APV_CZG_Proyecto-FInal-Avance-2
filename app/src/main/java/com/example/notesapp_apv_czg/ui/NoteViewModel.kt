package com.example.notesapp_apv_czg.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class EditorUiState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val isTask: Boolean = false,
    val isCompleted: Boolean = false,
    val priority: Int = 0,
    val dueDateMillis: Long? = null,
    val attachmentUris: List<String> = emptyList(),
    val isNewNote: Boolean = true
)

class NoteViewModel(private val repo: NoteRepository) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList()) //
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _editorState = MutableStateFlow(EditorUiState())
    val editorState: StateFlow<EditorUiState> = _editorState.asStateFlow()

    init {
        repo.getAllNotes()
            .onEach { _notes.value = it }
            .catch { /* handle errors */ }
            .launchIn(viewModelScope)
    }

    fun search(q: String) {
        repo.search(q)
            .onEach { _notes.value = it }
            .catch { /* handle */ }
            .launchIn(viewModelScope)
    }

    // Editor functions
    fun onTitleChange(newTitle: String) {
        _editorState.value = _editorState.value.copy(title = newTitle)
    }

    fun onDescriptionChange(newDescription: String) {
        _editorState.value = _editorState.value.copy(description = newDescription)
    }

    fun onIsTaskChange(isTask: Boolean) {
        _editorState.value = _editorState.value.copy(isTask = isTask)
    }

    fun onIsCompletedChange(isCompleted: Boolean) {
        _editorState.value = _editorState.value.copy(isCompleted = isCompleted)
    }

    fun onPriorityChange(priority: Int) {
        _editorState.value = _editorState.value.copy(priority = priority)
    }

    fun onDueDateChange(dueDate: Long?) {
        _editorState.value = _editorState.value.copy(dueDateMillis = dueDate)
    }

    fun onAttachmentAdded(uri: String) {
        val currentUris = _editorState.value.attachmentUris
        _editorState.value = _editorState.value.copy(attachmentUris = currentUris + uri)
    }

    fun onAttachmentRemoved(uri: String) {
        val currentUris = _editorState.value.attachmentUris
        _editorState.value = _editorState.value.copy(attachmentUris = currentUris - uri)
    }

    fun prepareNewNote() {
        _editorState.value = EditorUiState()
    }

    fun loadNote(noteId: Long) {
        val note = notes.value.find { it.id == noteId }
        note?.let {
            _editorState.value = EditorUiState(
                id = it.id,
                title = it.title,
                description = it.description ?: "",
                isTask = it.isTask,
                isCompleted = it.isCompleted,
                priority = it.priority,
                dueDateMillis = it.dueDateMillis,
                attachmentUris = it.attachmentUris,
                isNewNote = false
            )
        }
    }

    fun saveNote(onSaveFinished: (note: Note) -> Unit) {
        val currentEditorState = _editorState.value
        if (currentEditorState.title.isBlank() && currentEditorState.description.isBlank()) {
            return
        }

        val note = Note(
            id = currentEditorState.id,
            title = currentEditorState.title,
            description = currentEditorState.description,
            isTask = currentEditorState.isTask,
            isCompleted = currentEditorState.isCompleted,
            priority = currentEditorState.priority,
            dueDateMillis = currentEditorState.dueDateMillis,
            attachmentUris = currentEditorState.attachmentUris
        )

        viewModelScope.launch {
            if (currentEditorState.isNewNote) {
                val newId = repo.insert(note)
                onSaveFinished(note.copy(id = newId))
            } else {
                repo.update(note)
                onSaveFinished(note)
            }
        }
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
