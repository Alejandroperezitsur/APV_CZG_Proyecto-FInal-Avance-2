package com.example.notesapp_apv_czg.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.ui.components.AttachmentViewer
import com.example.notesapp_apv_czg.ui.components.PinDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: Long,
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit
) {
    LaunchedEffect(noteId) {
        viewModel.getNoteById(noteId)
    }

    val currentNote by viewModel.currentNote.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentNote?.title ?: stringResource(R.string.note),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { currentNote?.let { onEdit(it.id) } },
                        enabled = currentNote != null
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val note = currentNote
            if (note == null) {
                Text(text = stringResource(R.string.search_notes))
                return@Column
            }

            // If locked, show unlock dialog flow before rendering content
            var showUnlock by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(note.isLocked) }

            if (showUnlock) {
                PinDialog(
                    onSuccess = {
                        showUnlock = false
                        val unlocked = note.copy(isLocked = false)
                        viewModel.update(unlocked)
                    },
                    onCancel = { onBack() },
                    title = stringResource(R.string.unlock_note_title)
                )
            }

            if (!showUnlock) {
                // Description/content
                note.description?.takeIf { it.isNotEmpty() }?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Task due date
                if (note.isTask && note.dueDateMillis != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val formatted = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(note.dueDateMillis))
                    Text(
                        text = stringResource(R.string.due_date, formatted),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Attachments viewer
                if (note.attachmentUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AttachmentViewer(
                        attachmentUris = note.attachmentUris,
                        onRemoveAttachment = { /* no-op in detail screen */ },
                        modifier = Modifier.fillMaxWidth(),
                        allowRemove = false
                    )
                }
            }
        }
    }
}