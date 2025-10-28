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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Task
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: Long?,
    viewModel: NoteViewModel,
    onCancel: () -> Unit,
    onSave: () -> Unit
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

    var isRecording by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var audioFile by remember { mutableStateOf<File?>(null) }
    val audioRecorder = remember { AudioRecorder(context) }

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
            attachmentUris = attachmentUris.toList()
        )

        if (isNewNote) {
            viewModel.insert(note)
        } else {
            viewModel.update(note)
        }
        onSave()
    }

    fun createImageUri(): Uri {
        val imageFile = File(context.cacheDir, "camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", imageFile)
        attachmentUris.add(uri.toString())
        return uri
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
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
                        scaffoldState.bottomSheetState.hide()
                        val uri = createImageUri()
                        cameraLauncher.launch(uri)
                    }
                },
                onGalleryClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.hide()
                        galleryLauncher.launch("image/*")
                    }
                },
                onAudioClick = {
                    scope.launch {
                        scaffoldState.bottomSheetState.hide()
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
                        scope.launch { scaffoldState.bottomSheetState.hide() }
                    } else {
                        File(context.cacheDir, "audio_${UUID.randomUUID()}.mp3").also {
                            audioRecorder.start(it)
                            audioFile = it
                            isRecording = true
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

                // Note type selection with improved design
                NoteTypeSelection(
                    isTask = isTask,
                    onIsTaskChange = { isTask = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title field with improved styling
                TitleTextField(
                    value = title,
                    onValueChange = { title = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Format toolbar with WhatsApp-like design
                FormatToolbar(
                    onBold = {
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
                    modifier = Modifier.weight(1f)
                )

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

                // Attachments with improved design
                AttachmentViewer(
                    attachmentUris = attachmentUris,
                    onRemoveAttachment = { uri -> attachmentUris.remove(uri) }
                )

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
                            if (scaffoldState.bottomSheetState.isVisible) {
                                scaffoldState.bottomSheetState.hide()
                            } else {
                                scaffoldState.bottomSheetState.expand()
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
private fun TitleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
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
    modifier: Modifier = Modifier
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
