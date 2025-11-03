package com.example.notesapp_apv_czg.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import com.example.notesapp_apv_czg.security.PinManager
import com.example.notesapp_apv_czg.ui.components.PinDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.ui.components.AttachmentOptions
import com.example.notesapp_apv_czg.ui.components.AttachmentViewer
import coil.compose.AsyncImage
import android.media.MediaPlayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

private object AudioPlaybackCoordinator {
    private val players = mutableSetOf<MediaPlayer>()
    fun play(player: MediaPlayer) {
        players.add(player)
        try { player.start() } catch (_: Exception) {}
    }
    fun pause(player: MediaPlayer) {
        try { player.pause() } catch (_: Exception) {}
        players.add(player)
    }
    fun pauseAll() {
        players.forEach { p -> try { p.pause() } catch (_: Exception) {} }
        players.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    viewModel: NoteViewModel,
    onCancel: () -> Unit,
    onSave: (Note) -> Unit
) {
    val isNewNote = noteId == null
    val currentNote by viewModel.currentNote.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val scope = rememberCoroutineScope()

    // Initialize with current note if editing
    LaunchedEffect(noteId) {
        if (noteId != null) {
            viewModel.getNoteById(noteId)
        } else {
            viewModel.clearCurrentNote()
        }
    }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var isTask by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var priority by remember { mutableStateOf(0) }
    var dueDateMillis by remember { mutableStateOf<Long?>(null) }
    val attachmentUris = remember { mutableStateListOf<String>() }
    var isLocked by remember { mutableStateOf(currentNote?.isLocked ?: false) }
    var showPinSetDialog by remember { mutableStateOf(false) }
    var showPinUnlockDialog by remember { mutableStateOf(false) }
    val tags = remember { mutableStateListOf<String>() }
    var newTag by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(currentNote?.isFavorite ?: false) }
    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var audioFile by remember { mutableStateOf<File?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }
    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    var recordingSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingSeconds = 0
            while (isRecording) {
                kotlinx.coroutines.delay(1000)
                recordingSeconds += 1
            }
        }
    }

    LaunchedEffect(currentNote) {
        currentNote?.let { note ->
            title = note.title
            description = TextFieldValue(note.description ?: "")
            isTask = note.isTask
            isCompleted = note.isCompleted
            priority = note.priority
            dueDateMillis = note.dueDateMillis
            attachmentUris.clear()
            attachmentUris.addAll(note.attachmentUris)
            isLocked = note.isLocked
            tags.clear()
            tags.addAll(note.tags)
            isFavorite = note.isFavorite
        }
    }

    // Pause audio when leaving the editor
    DisposableEffect(Unit) {
        onDispose {
            AudioPlaybackCoordinator.pauseAll()
        }
    }

    // Launchers for media selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { attachmentUris.add(it.toString()) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // Photo was taken successfully, URI is already in attachmentUris
        }
    }

    val audioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { attachmentUris.add(it.toString()) }
    }

    fun saveNote() {
        val note = Note(
            id = currentNote?.id ?: 0,
            title = title,
            description = description.text,
            isTask = isTask,
            isCompleted = isCompleted,
            priority = priority,
            dueDateMillis = dueDateMillis,
            attachmentUris = attachmentUris.toList(),
            isLocked = isLocked,
            tags = tags.toList(),
            isFavorite = isFavorite
        )

        if (isNewNote) {
            viewModel.insert(note) { id ->
                onSave(note.copy(id = id))
            }
        } else {
            viewModel.update(note)
            onSave(note)
        }
    }

    fun createImageUri(): Uri {
        val imageFile = File(context.cacheDir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        attachmentUris.add(uri.toString())
        return uri
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNewNote) stringResource(R.string.new_note_task) else stringResource(
                            R.string.save
                        ),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (isLocked) {
                            showPinUnlockDialog = true
                        } else {
                            if (!PinManager.isPinSet(context)) {
                                showPinSetDialog = true
                            } else {
                                isLocked = true
                                if (!isNewNote) {
                                    val updated = Note(
                                        id = currentNote?.id ?: 0,
                                        title = title,
                                        description = description.text,
                                        isTask = isTask,
                                        isCompleted = isCompleted,
                                        priority = priority,
                                        dueDateMillis = dueDateMillis,
                                        attachmentUris = attachmentUris.toList(),
                                        isLocked = true
                                    )
                                    viewModel.update(updated)
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (isLocked) Icons.Filled.LockOpen else Icons.Filled.Lock,
                            contentDescription = if (isLocked) "Desbloquear" else "Bloquear"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        sheetPeekHeight = 0.dp,
        sheetContent = {
            AttachmentOptions(
                onCameraClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                        val uri = createImageUri()
                        cameraLauncher.launch(uri)
                    }
                },
                onGalleryClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                        galleryLauncher.launch("image/*")
                    }
                },
                onAudioClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                        audioLauncher.launch("audio/*")
                    }
                },
                onRecordClick = {
                    if (isRecording) {
                        audioRecorder.stop()
                        isRecording = false
                        audioFile?.let { file ->
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                            attachmentUris.add(uri.toString())
                        }
                        audioFile = null
                        scope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    } else {
                        try {
                            File(context.cacheDir, "audio_${UUID.randomUUID()}.3gp").also {
                                audioRecorder.start(it)
                                audioFile = it
                                isRecording = true
                            }
                        } catch (e: Exception) {
                            scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.recording_start_failed)) }
                        }
                    }
                },
                isRecording = isRecording
            )
        },
        sheetDragHandle = null,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.imePadding()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // PIN dialogs
                if (showPinSetDialog) {
                    PinDialog(
                        onSuccess = {
                            showPinSetDialog = false
                            isLocked = true
                            if (!isNewNote) {
                                val updated = Note(
                                    id = currentNote?.id ?: 0,
                                    title = title,
                                    description = description.text,
                                    isTask = isTask,
                                    isCompleted = isCompleted,
                                    priority = priority,
                                    dueDateMillis = dueDateMillis,
                                    attachmentUris = attachmentUris.toList(),
                                    isLocked = true
                                )
                                viewModel.update(updated)
                            }
                        },
                        onCancel = { showPinSetDialog = false },
                        title = stringResource(R.string.configure_pin_title)
                    )
                }

                if (showPinUnlockDialog) {
                    PinDialog(
                        onSuccess = {
                            showPinUnlockDialog = false
                            isLocked = false
                            if (!isNewNote) {
                                val updated = Note(
                                    id = currentNote?.id ?: 0,
                                    title = title,
                                    description = description.text,
                                    isTask = isTask,
                                    isCompleted = isCompleted,
                                    priority = priority,
                                    dueDateMillis = dueDateMillis,
                                    attachmentUris = attachmentUris.toList(),
                                    isLocked = false
                                )
                                viewModel.update(updated)
                            }
                        },
                        onCancel = { showPinUnlockDialog = false },
                        title = stringResource(R.string.unlock_note_title)
                    )
                }

                // Note type selection with improved design
                NoteTypeSelection(
                    isTask = isTask,
                    onIsTaskChange = { isTask = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title field with improved styling
                TitleTextField(
                    value = title,
                    onValueChange = { title = it },
                    enabled = !isLocked
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Format toolbar with WhatsApp-like design
                FormatToolbar(
                    onBold = {
                        if (isLocked) return@FormatToolbar
                        val selection = description.selection
                        if (!selection.collapsed) {
                            val builder = AnnotatedString.Builder(description.annotatedString)
                            builder.addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                selection.min,
                                selection.max
                            )
                            description =
                                description.copy(annotatedString = builder.toAnnotatedString())
                        }
                    },
                    onItalic = {
                        if (isLocked) return@FormatToolbar
                        val selection = description.selection
                        if (!selection.collapsed) {
                            val builder = AnnotatedString.Builder(description.annotatedString)
                            builder.addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                selection.min,
                                selection.max
                            )
                            description =
                                description.copy(annotatedString = builder.toAnnotatedString())
                        }
                    },
                    onChecklist = {
                        if (isLocked) return@FormatToolbar
                        val selection = description.selection
                        val lineStart = description.text.lastIndexOf('\n', selection.start - 1)
                            .let { if (it < 0) 0 else it + 1 }
                        val newText = description.text.substring(
                            0,
                            lineStart
                        ) + "☐ " + description.text.substring(lineStart)
                        description = TextFieldValue(
                            text = newText,
                            selection = TextRange(selection.start + 2)
                        )
                    },
                    onBullet = {
                        if (isLocked) return@FormatToolbar
                        val selection = description.selection
                        val lineStart = description.text.lastIndexOf('\n', selection.start - 1)
                            .let { if (it < 0) 0 else it + 1 }
                        val newText = description.text.substring(
                            0,
                            lineStart
                        ) + "• " + description.text.substring(lineStart)
                        description = TextFieldValue(
                            text = newText,
                            selection = TextRange(selection.start + 2)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description field with improved styling
                DescriptionTextField(
                    value = description,
                    onValueChange = { description = it },
                    enabled = !isLocked,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tags section
                TagsSection(
                    tags = tags,
                    newTag = newTag,
                    onNewTagChange = { newTag = it },
                    onAddTag = {
                        if (newTag.isNotBlank() && !tags.contains(newTag.trim())) {
                            tags.add(newTag.trim())
                            newTag = ""
                        }
                    },
                    onRemoveTag = { tag -> tags.remove(tag) },
                    enabled = !isLocked
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Favorite toggle
                FavoriteToggle(
                    isFavorite = isFavorite,
                    onToggle = { isFavorite = it },
                    enabled = !isLocked
                )

                // Attachments section
                if (attachmentUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    AttachmentPreviewSection(
                        attachmentUris = attachmentUris,
                        onRemoveAttachment = { uri ->
                            attachmentUris.remove(uri)
                        }
                    )
                }

                if (isTask) {
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskOptions(
                        isCompleted = isCompleted,
                        onIsCompletedChange = { isCompleted = it },
                        priority = priority,
                        onPriorityChange = { priority = it },
                        dueDateMillis = dueDateMillis,
                        onDueDateChange = { dueDateMillis = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Spacer(modifier = Modifier.height(100.dp)) // Space for FABs
            }

            // Floating Action Buttons with WhatsApp-like design
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Attachment FAB
                FloatingActionButton(
                    onClick = {
                        scope.launch {
                            val sheetState = scaffoldState.bottomSheetState
                            if (sheetState.currentValue == androidx.compose.material3.SheetValue.Expanded) {
                                sheetState.partialExpand()
                            } else {
                                sheetState.expand()
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = stringResource(R.string.attachments),
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Save FAB
                ExtendedFloatingActionButton(
                    onClick = { saveNote() },
                    containerColor = Color(0xFF128C7E), // Dark Blue
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = stringResource(R.string.save)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.save),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewSection(
    attachmentUris: List<String>,
    onRemoveAttachment: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.attachments),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val isWide = maxWidth >= 600.dp
                if (isWide) {
                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                        columns = androidx.compose.foundation.lazy.grid.GridCells.Adaptive(minSize = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 240.dp)
                    ) {
                        items(attachmentUris.size) { index ->
                            val uri = attachmentUris[index]
                            AttachmentPreviewItem(
                                uri = uri,
                                onRemove = { onRemoveAttachment(uri) }
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(attachmentUris) { uri ->
                            AttachmentPreviewItem(
                                uri = uri,
                                onRemove = { onRemoveAttachment(uri) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttachmentPreviewItem(
    uri: String,
    onRemove: () -> Unit
) {
    val isAudio = uri.contains("audio") || uri.endsWith(".3gp") || uri.endsWith(".mp3") || uri.endsWith(".wav") || uri.endsWith(".m4a")
    val isImage = uri.contains("image") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".png") || uri.endsWith(".gif")
    
    Box {
        Card(
            modifier = Modifier.size(96.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isAudio -> Color(0xFF4ECDC4).copy(alpha = 0.1f)
                    isImage -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isAudio -> {
                        // Audio preview with playback controls
                        AudioAttachmentPlayer(uri = uri)
                    }
                    isImage -> {
                        // Image preview
                        AsyncImage(
                            model = uri,
                            contentDescription = stringResource(R.string.attachment_image),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // Generic file preview
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.AttachFile,
                                contentDescription = "File attachment",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.attachment_file_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
                
                // Type indicator badge
                if (isAudio || isImage) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .background(
                                Color.Black.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when {
                                isAudio -> stringResource(R.string.attachment_audio_label)
                                isImage -> stringResource(R.string.image)
                                else -> stringResource(R.string.attachment_file_label)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)
                .background(
                    MaterialTheme.colorScheme.error,
                    CircleShape
                )
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = "Remove attachment",
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AudioAttachmentPlayer(uri: String) {
    val context = LocalContext.current
    var isPrepared by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var durationMs by remember { mutableStateOf(0) }
    var positionMs by remember { mutableStateOf(0) }

    val mediaPlayer = remember(uri) { MediaPlayer() }

    DisposableEffect(uri) {
        try {
            val contentUri = android.net.Uri.parse(uri)
            val pfd = context.contentResolver.openFileDescriptor(contentUri, "r")
            mediaPlayer.reset()
            if (pfd != null) {
                mediaPlayer.setDataSource(pfd.fileDescriptor)
                pfd.close()
            } else {
                // Fallback for file:// or other schemes
                try {
                    mediaPlayer.setDataSource(context, contentUri)
                } catch (_: Exception) {
                    mediaPlayer.setDataSource(uri)
                }
            }
            mediaPlayer.prepare()
            isPrepared = true
            durationMs = mediaPlayer.duration
            mediaPlayer.setOnCompletionListener {
                isPlaying = false
                positionMs = 0
            }
        } catch (_: Exception) {
            isPrepared = false
        }
        onDispose {
            try { mediaPlayer.release() } catch (_: Exception) {}
        }
    }

    LaunchedEffect(isPlaying, isPrepared) {
        if (isPrepared) {
            if (isPlaying) {
                try { AudioPlaybackCoordinator.play(mediaPlayer) } catch (_: Exception) {}
            } else {
                try { AudioPlaybackCoordinator.pause(mediaPlayer) } catch (_: Exception) {}
            }
        }
    }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            try { positionMs = mediaPlayer.currentPosition } catch (_: Exception) {}
            delay(250)
        }
    }

    val progress = remember(positionMs, durationMs) {
        if (durationMs > 0) positionMs.toFloat() / durationMs else 0f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconButton(
            onClick = { if (isPrepared) isPlaying = !isPlaying },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        // Seek slider
        Slider(
            value = progress,
            onValueChange = { value ->
                if (durationMs > 0) {
                    positionMs = (durationMs * value).toInt()
                    try { mediaPlayer.seekTo(positionMs) } catch (_: Exception) {}
                }
            },
            modifier = Modifier.width(64.dp),
            valueRange = 0f..1f
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "${formatMs(positionMs)} / ${formatMs(durationMs)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun formatMs(ms: Int): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}

@Composable
private fun TitleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = MaterialTheme.typography.headlineSmall.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.title),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun DescriptionTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (value.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.description),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun FormatToolbar(
    onBold: () -> Unit,
    onItalic: () -> Unit,
    onChecklist: () -> Unit,
    onBullet: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            FormatButton(
                icon = Icons.Default.FormatBold,
                contentDescription = "Bold",
                onClick = onBold
            )
            FormatButton(
                icon = Icons.Default.FormatItalic,
                contentDescription = "Italic",
                onClick = onItalic
            )
            FormatButton(
                icon = Icons.Default.Checklist,
                contentDescription = "Checklist",
                onClick = onChecklist
            )
            FormatButton(
                icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                contentDescription = "Bullet list",
                onClick = onBullet
            )
        }
    }
}

@Composable
private fun FormatButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun NoteTypeSelection(isTask: Boolean, onIsTaskChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilterChip(
            selected = !isTask,
            onClick = { onIsTaskChange(false) },
            label = {
                Text(
                    stringResource(R.string.note),
                    fontWeight = FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.AutoMirrored.Filled.Notes,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
        FilterChip(
            selected = isTask,
            onClick = { onIsTaskChange(true) },
            label = {
                Text(
                    stringResource(R.string.task),
                    fontWeight = FontWeight.Medium
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Task,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        )
    }
}

// Keep existing TaskOptions, PrioritySelector, and DateSelector functions unchanged
@Composable
private fun TaskOptions(
    isCompleted: Boolean, onIsCompletedChange: (Boolean) -> Unit,
    priority: Int, onPriorityChange: (Int) -> Unit,
    dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onIsCompletedChange(!isCompleted) }
            ) {
                Checkbox(checked = isCompleted, onCheckedChange = onIsCompletedChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.completed))
            }
            Spacer(modifier = Modifier.height(16.dp))
            PrioritySelector(priority = priority, onPriorityChange = onPriorityChange)
            Spacer(modifier = Modifier.height(16.dp))
            DateSelector(dueDateMillis = dueDateMillis, onDueDateChange = onDueDateChange)
        }
    }
}

@Composable
private fun PrioritySelector(priority: Int, onPriorityChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(stringResource(R.string.priority))
        Spacer(modifier = Modifier.width(8.dp))
        (1..5).forEach { index ->
            Icon(
                imageVector = if (index <= priority) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                modifier = Modifier.clickable { onPriorityChange(index) },
                tint = if (index <= priority) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DateSelector(dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    dueDateMillis?.let { calendar.timeInMillis = it }

    val dateFormat = SimpleDateFormat.getDateInstance()
    val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            onDueDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            onDueDateChange(calendar.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = { datePickerDialog.show() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = dueDateMillis?.let { dateFormat.format(it) } ?: stringResource(R.string.select_date))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { timePickerDialog.show() },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(text = dueDateMillis?.let { timeFormat.format(it) } ?: stringResource(R.string.select_time))
        }
        if (dueDateMillis != null) {
            IconButton(onClick = { onDueDateChange(null) }) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear_date))
            }
        }
    }
}

@Composable
private fun TagsSection(
     tags: SnapshotStateList<String>,
     newTag: String,
     onNewTagChange: (String) -> Unit,
     onAddTag: () -> Unit,
     onRemoveTag: (String) -> Unit,
     enabled: Boolean = true
 ) {
     Card(
         modifier = Modifier.fillMaxWidth(),
         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
         colors = CardDefaults.cardColors(
             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
         )
     ) {
         Column(modifier = Modifier.padding(16.dp)) {
             Text(
                 text = "Etiquetas",
                 style = MaterialTheme.typography.titleMedium,
                 fontWeight = FontWeight.SemiBold,
                 color = MaterialTheme.colorScheme.onSurface
             )
             
             Spacer(modifier = Modifier.height(8.dp))
             
             // Input field for new tag
             Row(
                 verticalAlignment = Alignment.CenterVertically,
                 modifier = Modifier.fillMaxWidth()
             ) {
                 BasicTextField(
                     value = newTag,
                     onValueChange = onNewTagChange,
                     enabled = enabled,
                     textStyle = MaterialTheme.typography.bodyMedium.copy(
                         color = MaterialTheme.colorScheme.onSurface
                     ),
                     cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                     modifier = Modifier
                         .weight(1f)
                         .background(
                             MaterialTheme.colorScheme.surface,
                             RoundedCornerShape(8.dp)
                         )
                         .padding(12.dp),
                     decorationBox = { innerTextField ->
                         if (newTag.isEmpty()) {
                             Text(
                                 text = "Agregar etiqueta...",
                                 style = MaterialTheme.typography.bodyMedium.copy(
                                     color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                 )
                             )
                         }
                         innerTextField()
                     }
                 )
                 
                 Spacer(modifier = Modifier.width(8.dp))
                 
                 IconButton(
                     onClick = onAddTag,
                     enabled = enabled && newTag.isNotBlank(),
                     modifier = Modifier
                         .background(
                             if (enabled && newTag.isNotBlank()) 
                                 MaterialTheme.colorScheme.primary 
                             else 
                                 MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                             CircleShape
                         )
                 ) {
                     Icon(
                         Icons.Default.Add,
                         contentDescription = "Agregar etiqueta",
                         tint = Color.White,
                         modifier = Modifier.size(20.dp)
                     )
                 }
             }
             
             // Display existing tags
             if (tags.isNotEmpty()) {
                 Spacer(modifier = Modifier.height(12.dp))
                 LazyRow(
                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                 ) {
                     items(tags.toList()) { tag ->
                         TagChip(
                             tag = tag,
                             onRemove = { onRemoveTag(tag) },
                             enabled = enabled
                         )
                     }
                 }
             }
         }
     }
 }
 
 @Composable
 private fun TagChip(
     tag: String,
     onRemove: () -> Unit,
     enabled: Boolean = true
 ) {
     Card(
         modifier = Modifier,
         elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
         colors = CardDefaults.cardColors(
             containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
         )
     ) {
         Row(
             verticalAlignment = Alignment.CenterVertically,
             modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
         ) {
             Text(
                 text = tag,
                 style = MaterialTheme.typography.labelMedium,
                 color = MaterialTheme.colorScheme.primary,
                 fontWeight = FontWeight.Medium
             )
             
             if (enabled) {
                 Spacer(modifier = Modifier.width(4.dp))
                 IconButton(
                     onClick = onRemove,
                     modifier = Modifier.size(20.dp)
                 ) {
                     Icon(
                         Icons.Default.Clear,
                         contentDescription = "Eliminar etiqueta",
                         tint = MaterialTheme.colorScheme.primary,
                         modifier = Modifier.size(14.dp)
                     )
                 }
             }
         }
     }
 }
 
 @Composable
 private fun FavoriteToggle(
     isFavorite: Boolean,
     onToggle: (Boolean) -> Unit,
     enabled: Boolean = true
 ) {
     Card(
         modifier = Modifier.fillMaxWidth(),
         elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
         colors = CardDefaults.cardColors(
             containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
         )
     ) {
         Row(
             verticalAlignment = Alignment.CenterVertically,
             modifier = Modifier
                 .fillMaxWidth()
                 .clickable(enabled = enabled) { onToggle(!isFavorite) }
                 .padding(16.dp)
         ) {
             Icon(
                 imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                 contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                 tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurface,
                 modifier = Modifier.size(24.dp)
             )
             
             Spacer(modifier = Modifier.width(12.dp))
             
             Text(
                 text = if (isFavorite) "Favorito" else "Marcar como favorito",
                 style = MaterialTheme.typography.bodyLarge,
                 color = MaterialTheme.colorScheme.onSurface,
                 fontWeight = if (isFavorite) FontWeight.SemiBold else FontWeight.Normal
             )
         }
     }
 }
