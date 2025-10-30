package com.example.notesapp_apv_czg.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.SearchBar
import coil.compose.rememberAsyncImagePainter
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Definiciones de componentes reutilizables al inicio del archivo
@Composable
fun EmptyState(
    hasSearch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Default.Search else Icons.AutoMirrored.Filled.Notes,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearch) 
                stringResource(R.string.no_notes_found) 
            else 
                stringResource(R.string.empty_list_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: (Note) -> Unit,
    onDelete: () -> Unit,
    onToggleLock: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else MaterialTheme.colorScheme.surface,
                label = "background_color"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.3f else 0.8f,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.scale(scale),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = modifier
    ) {
        NoteCardContent(
            note = note,
            onClick = onClick,
            onToggleLock = onToggleLock
        )
    }
}

@Composable
fun NoteCardContent(
    note: Note,
    onClick: (Note) -> Unit,
    onToggleLock: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(note) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isTask && note.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note type indicator and completion status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (note.isTask) {
                        Icon(
                            imageVector = if (note.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (note.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.task),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.note),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Priority stars
                if (note.isTask && note.priority > 0) {
                    Row {
                        repeat(note.priority) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Lock toggle
                IconButton(onClick = {
                    onToggleLock(note, !note.isLocked)
                }) {
                    Icon(
                        imageVector = if (note.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (note.isTask && note.isCompleted) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else null
                ),
                color = if (note.isTask && note.isCompleted) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description preview
            note.description?.takeIf { it.isNotEmpty() }?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Attachments preview
            if (note.attachmentUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                AttachmentPreview(attachmentUris = note.attachmentUris)
            }

            // Footer with date and due date
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Due date (for tasks)
                note.dueDateMillis?.let { dueDate ->
                    val isOverdue = dueDate < System.currentTimeMillis() && !note.isCompleted
                    Text(
                        text = "Due: ${SimpleDateFormat.getDateInstance().format(Date(dueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Attachment count indicator
                if (note.attachmentUris.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = note.attachmentUris.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreview(
    attachmentUris: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(attachmentUris.take(3)) { uri ->
            AttachmentPreviewItem(uri = uri)
        }
        if (attachmentUris.size > 3) {
            item {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+${attachmentUris.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreviewItem(
    uri: String,
    modifier: Modifier = Modifier
) {
    val isAudio = uri.contains("audio") || uri.endsWith(".mp3", ignoreCase = true) || uri.endsWith(".m4a", ignoreCase = true) || uri.endsWith(".wav", ignoreCase = true) || uri.endsWith(".3gp", ignoreCase = true)
    
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (isAudio) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(Color(0xFF4ECDC4).copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF4ECDC4),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(
                    model = uri,
                    error = rememberAsyncImagePainter(model = R.drawable.ic_launcher_foreground)
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

enum class NoteFilter { ALL, NOTES, TASKS }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    notes: List<Note>,
    onAdd: () -> Unit = {},
    onOpen: (Long) -> Unit = {},
    onDelete: (Note) -> Unit = {},
    onToggleLock: (Note, Boolean) -> Unit = { _, _ -> },
    onOpenThemeSettings: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("all") } // "all", "notes", "tasks"
    var showDeleteDialog by remember { mutableStateOf<Note?>(null) }

    val filteredNotes = notes.filter { note ->
        val matchesSearch = note.title.contains(searchQuery, ignoreCase = true) ||
                note.description?.contains(searchQuery, ignoreCase = true) == true
        val matchesFilter = when (filterType) {
            "notes" -> !note.isTask
            "tasks" -> note.isTask
            else -> true
        }
        matchesSearch && matchesFilter
    }.sortedWith(compareByDescending<Note> { it.isTask && !it.isCompleted }
        .thenByDescending { it.priority }
        .thenByDescending { it.dueDateMillis ?: 0 })

    var pinTargetNote by remember { mutableStateOf<Note?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onOpenThemeSettings) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Tema",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_note_task))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.new_note_task),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    ) { paddingValues ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { newValue -> searchQuery = newValue },
                placeholder = { Text(stringResource(R.string.search_notes)) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Filter chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = filterType == "all",
                        onClick = { filterType = "all" },
                        label = {
                            Text(
                                stringResource(R.string.all),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = filterType == "notes",
                        onClick = { filterType = "notes" },
                        label = {
                            Text(
                                stringResource(R.string.notes),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.Notes,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                item {
                    FilterChip(
                        selected = filterType == "tasks",
                        onClick = { filterType = "tasks" },
                        label = {
                            Text(
                                stringResource(R.string.tasks),
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Task,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Notes list
            if (filteredNotes.isEmpty()) {
                EmptyState(hasSearch = searchQuery.isNotEmpty())
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredNotes,
                        key = { it.id }
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = {
                                if (note.isLocked) {
                                    pinTargetNote = note
                                } else {
                                    onOpen(note.id)
                                }
                            },
                            onDelete = { showDeleteDialog = note },
                            onToggleLock = onToggleLock
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    showDeleteDialog?.let { note ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_note_title)) },
            text = { Text(stringResource(R.string.delete_note_confirmation)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(note)
                    showDeleteDialog = null
                }) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun CustomSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(stringResource(R.string.search_notes)) },
        leadingIcon = {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        singleLine = true
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChips(
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        item {
            FilterChip(
                selected = selectedFilter == "all",
                onClick = { onFilterChange("all") },
                label = {
                    Text(
                        stringResource(R.string.all),
                        fontWeight = FontWeight.Medium
                    )
                }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "notes",
                onClick = { onFilterChange("notes") },
                label = {
                    Text(
                        stringResource(R.string.notes),
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.AutoMirrored.Filled.Notes,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        item {
            FilterChip(
                selected = selectedFilter == "tasks",
                onClick = { onFilterChange("tasks") },
                label = {
                    Text(
                        stringResource(R.string.tasks),
                        fontWeight = FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Task,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
    }

    // PIN unlock dialog
    pinTargetNote?.let { target ->
        com.example.notesapp_apv_czg.ui.components.PinDialog(
            onSuccess = {
                onOpen(target.id)
                pinTargetNote = null
            },
            onCancel = { pinTargetNote = null },
            title = "Desbloquear nota"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    onClick: (Note) -> Unit,
    onDelete: () -> Unit,
    onToggleLock: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                    MaterialTheme.colorScheme.errorContainer
                } else MaterialTheme.colorScheme.surface,
                label = "background_color"
            )
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 1.3f else 0.8f,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.scale(scale),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        },
        modifier = modifier
    ) {
        NoteCardContent(
            note = note,
            onClick = onClick,
            onToggleLock = onToggleLock
        )
    }
}

@Composable
fun NoteCardContent(
    note: Note,
    onClick: (Note) -> Unit,
    onToggleLock: (Note, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                if (note.isLocked) {
                    onClick(note) // let parent handle gating; we will gate above
                } else {
                    onClick(note)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isTask && note.isCompleted) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Note type indicator and completion status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (note.isTask) {
                        Icon(
                            imageVector = if (note.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (note.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.task),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.note),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Priority stars
                if (note.isTask && note.priority > 0) {
                    Row {
                        repeat(note.priority) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Lock toggle
                IconButton(onClick = {
                    onToggleLock(note, !note.isLocked)
                }) {
                    Icon(
                        imageVector = if (note.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (note.isTask && note.isCompleted) 
                        androidx.compose.ui.text.style.TextDecoration.LineThrough 
                    else null
                ),
                color = if (note.isTask && note.isCompleted) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Description preview and attachments
            if (!note.isLocked) {
                note.description?.takeIf { it.isNotEmpty() }?.let { description ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Attachments preview
                if (note.attachmentUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AttachmentPreview(attachmentUris = note.attachmentUris)
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Contenido bloqueado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Footer with date and due date
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                // Due date (for tasks)
                note.dueDateMillis?.let { dueDate ->
                    val isOverdue = dueDate < System.currentTimeMillis() && !note.isCompleted
                    Text(
                        text = "Due: ${SimpleDateFormat.getDateInstance().format(Date(dueDate))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                        fontWeight = if (isOverdue) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Attachment count indicator
                if (!note.isLocked && note.attachmentUris.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = note.attachmentUris.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreview(
    attachmentUris: List<String>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        items(attachmentUris.take(3)) { uri ->
            AttachmentPreviewItem(uri = uri)
        }
        if (attachmentUris.size > 3) {
            item {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "+${attachmentUris.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentPreviewItem(
    uri: String,
    modifier: Modifier = Modifier
) {
    val isAudio = uri.contains("audio") || uri.endsWith(".mp3", ignoreCase = true) || uri.endsWith(".m4a", ignoreCase = true) || uri.endsWith(".wav", ignoreCase = true) || uri.endsWith(".3gp", ignoreCase = true)
    
    Surface(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        if (isAudio) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.background(Color(0xFF4ECDC4).copy(alpha = 0.2f))
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color(0xFF4ECDC4),
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Image(
                painter = rememberAsyncImagePainter(
                    model = uri,
                    error = rememberAsyncImagePainter(model = R.drawable.ic_launcher_foreground)
                ),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun EmptyState(
    hasSearch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (hasSearch) Icons.Default.Search else Icons.AutoMirrored.Filled.Notes,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (hasSearch) stringResource(R.string.no_notes_found) else stringResource(R.string.empty_list_message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}}
