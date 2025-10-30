package com.example.notesapp_apv_czg.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    notes: List<Note>,
    filter: NoteFilter,
    onFilterChange: (NoteFilter) -> Unit,
    modifier: Modifier = Modifier,
    showFilterBar: Boolean = true,
    onAdd: () -> Unit = {},
    onOpen: (Long) -> Unit = {},
    onDelete: (Note) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val filteredNotes = remember(notes, query) {
        if (query.isBlank()) {
            notes
        } else {
            notes.filter {
                it.title.contains(query, ignoreCase = true) ||
                (it.description?.contains(query, ignoreCase = true) ?: false)
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            HomeAppBar(
                isSearchActive = isSearchActive,
                query = query,
                onQueryChange = { query = it },
                onToggleSearch = {
                    isSearchActive = !isSearchActive
                    if (!isSearchActive) query = "" 
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note_task))
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (showFilterBar) {
                FilterChips(selectedFilter = filter, onFilterChange = onFilterChange)
            }
            if (filteredNotes.isEmpty()) {
                EmptyListView()
            } else {
                NotesGrid(notes = filteredNotes, onOpen = onOpen, onLongPress = { noteToDelete = it })
            }

            if (noteToDelete != null) {
                DeleteConfirmationDialog(
                    onConfirm = { 
                        onDelete(noteToDelete!!)
                        noteToDelete = null
                    },
                    onDismiss = { noteToDelete = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeAppBar(isSearchActive: Boolean, query: String, onQueryChange: (String) -> Unit, onToggleSearch: () -> Unit) {
    TopAppBar(
        title = { 
            if(!isSearchActive) {
                Text(stringResource(R.string.app_name))
            }
        },
        actions = {
            if (isSearchActive) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    label = { Text(stringResource(R.string.search_notes)) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    trailingIcon = { IconButton(onClick = onToggleSearch) { Icon(Icons.Default.Close, contentDescription = null) } }
                )
            } else {
                IconButton(onClick = onToggleSearch) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChips(selectedFilter: NoteFilter, onFilterChange: (NoteFilter) -> Unit) {
    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedFilter == NoteFilter.ALL,
            onClick = { onFilterChange(NoteFilter.ALL) },
            label = { Text(stringResource(R.string.all)) }
        )
        FilterChip(
            selected = selectedFilter == NoteFilter.NOTES,
            onClick = { onFilterChange(NoteFilter.NOTES) },
            label = { Text(stringResource(R.string.notes)) }
        )
        FilterChip(
            selected = selectedFilter == NoteFilter.TASKS,
            onClick = { onFilterChange(NoteFilter.TASKS) },
            label = { Text(stringResource(R.string.tasks)) }
        )
    }
}

@Composable
private fun NotesGrid(notes: List<Note>, onOpen: (Long) -> Unit, onLongPress: (Note) -> Unit) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Adaptive(150.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalItemSpacing = 16.dp
    ) {
        items(notes, key = { it.id }) { note ->
            NoteCard(note = note, onClick = { onOpen(note.id) }, onLongClick = { onLongPress(note) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteCard(note: Note, onClick: () -> Unit, onLongClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if(note.title.isNotBlank()) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (note.description?.isNotBlank() == true) Spacer(modifier = Modifier.height(8.dp))
            }
            if (note.description?.isNotBlank() == true) {
                Text(text = note.description, style = MaterialTheme.typography.bodyMedium, maxLines = 8, overflow = TextOverflow.Ellipsis)
            }

            if(note.isTask && note.dueDateMillis != null) {
                Spacer(modifier = Modifier.height(8.dp))
                DueDateIndicator(dueDateMillis = note.dueDateMillis, priority = note.priority)
            }
            
            if(note.attachmentUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AttachmentIcons(uris = note.attachmentUris)
            }
        }
    }
}

@Composable
private fun DueDateIndicator(dueDateMillis: Long, priority: Int) {
    val formattedDate = remember { SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT).format(Date(dueDateMillis)) }
    val priorityColor = when (priority) {
        0 -> Color(0xFF4CAF50) // Low
        1 -> Color(0xFFFF9800) // Medium
        else -> Color(0xFFF44336) // High
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(priorityColor, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AttachmentIcons(uris: List<String>) {
    val context = LocalContext.current
    val hasImage = remember(uris) { uris.any { context.contentResolver.getType(it.toUri())?.startsWith("image/") == true } }
    val hasAudio = remember(uris) { uris.any { context.contentResolver.getType(it.toUri())?.startsWith("audio/") == true } }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (hasImage) {
            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (hasAudio) {
            Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EmptyListView() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.empty_list_message), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_note_title)) },
        text = { Text(stringResource(R.string.delete_note_confirmation)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
