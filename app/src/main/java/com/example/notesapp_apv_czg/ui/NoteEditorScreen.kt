package com.example.notesapp_apv_czg.ui

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier,
    onSave: (Note) -> Unit = {},
    onCancel: () -> Unit = {}
) {
    val editorState by viewModel.editorState.collectAsState()
    val context = LocalContext.current

    val audioRecorder = remember { AudioRecorder(context) }
    var isRecording by remember { mutableStateOf(false) }

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                viewModel.onAttachmentAdded(uri.toString())
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    var tempMediaUri by remember { mutableStateOf<Uri?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempMediaUri?.let { viewModel.onAttachmentAdded(it.toString()) }
        }
    }

    val recordVideoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            tempMediaUri?.let { viewModel.onAttachmentAdded(it.toString()) }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if(allGranted) {
            // If permissions are granted, we decide which launcher to use based on the temp URI
            tempMediaUri?.let { uri ->
                if (uri.toString().endsWith(".jpg")) {
                    takePictureLauncher.launch(uri)
                } else if (uri.toString().endsWith(".mp4")) {
                    recordVideoLauncher.launch(uri)
                }
            }
        }
    }

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            audioRecorder.startRecording()
            isRecording = true
        }
    }

    val toSave = {
        viewModel.saveNote(onSave)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                }
            })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = toSave) {
                Icon(Icons.Default.Save, contentDescription = stringResource(R.string.save))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            NoteTypeSelection(isTask = editorState.isTask, onIsTaskChange = viewModel::onIsTaskChange)

            EditorTextField(
                value = editorState.title,
                onValueChange = viewModel::onTitleChange,
                placeholder = stringResource(R.string.title),
                textStyle = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            EditorTextField(
                value = editorState.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = stringResource(R.string.description),
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxHeight()
            )

            if (editorState.isTask) {
                TaskOptions(
                    isCompleted = editorState.isCompleted,
                    onIsCompletedChange = viewModel::onIsCompletedChange,
                    priority = editorState.priority,
                    onPriorityChange = viewModel::onPriorityChange,
                    dueDateMillis = editorState.dueDateMillis,
                    onDueDateChange = viewModel::onDueDateChange,
                    reminders = editorState.reminders,
                    onReminderAdded = viewModel::onReminderAdded,
                    onReminderRemoved = viewModel::onReminderRemoved
                )
            }

            AttachmentsSection(
                attachmentUris = editorState.attachmentUris,
                onAddImage = { mediaPickerLauncher.launch(arrayOf("image/*")) },
                onAddAudio = { mediaPickerLauncher.launch(arrayOf("audio/*")) },
                onAddVideo = { mediaPickerLauncher.launch(arrayOf("video/*")) },
                onTakePicture = {
                    val uri = createTempUri(context, "jpg")
                    tempMediaUri = uri
                    cameraPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
                },
                onRecordVideo = {
                    val uri = createTempUri(context, "mp4")
                    tempMediaUri = uri
                    cameraPermissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO))
                },
                isRecording = isRecording,
                onRecordAudio = {
                    if (isRecording) {
                        val uri = audioRecorder.stopRecording()
                        isRecording = false
                        uri?.let { viewModel.onAttachmentAdded(it.toString()) }
                    } else {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onRemoveUri = viewModel::onAttachmentRemoved
            )
            Spacer(modifier = Modifier.height(80.dp)) // Spacer for FAB
        }
    }
}

private fun createTempUri(context: Context, extension: String): Uri {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val file = File.createTempFile(
        "NOTE_APP_${timeStamp}_",
        ".$extension",
        context.cacheDir
    )
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

@Composable
private fun EditorTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, textStyle: TextStyle, modifier: Modifier = Modifier) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onBackground),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(text = placeholder, style = textStyle, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                innerTextField()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteTypeSelection(isTask: Boolean, onIsTaskChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = !isTask,
            onClick = { onIsTaskChange(false) },
            label = { Text(stringResource(R.string.note)) },
            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
        FilterChip(
            selected = isTask,
            onClick = { onIsTaskChange(true) },
            label = { Text(stringResource(R.string.task)) },
            leadingIcon = { Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
    }
}

@Composable
private fun TaskOptions(
    isCompleted: Boolean, onIsCompletedChange: (Boolean) -> Unit,
    priority: Int, onPriorityChange: (Int) -> Unit,
    dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit,
    reminders: List<Long>,
    onReminderAdded: (Long) -> Unit,
    onReminderRemoved: (Long) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onIsCompletedChange(!isCompleted) }) {
            Checkbox(checked = isCompleted, onCheckedChange = { onIsCompletedChange(it) })
            Text(stringResource(R.string.completed))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.priority), style = MaterialTheme.typography.titleMedium)
        PriorityChips(selectedPriority = priority, onPriorityChange = onPriorityChange)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.due_date_time), style = MaterialTheme.typography.titleMedium)
        DueDateSelector(dueDateMillis = dueDateMillis, onDueDateChange = onDueDateChange)
        RemindersSection(
            reminders = reminders,
            onReminderAdded = onReminderAdded,
            onReminderRemoved = onReminderRemoved
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityChips(selectedPriority: Int, onPriorityChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
        val priorities = listOf(stringResource(R.string.low), stringResource(R.string.medium), stringResource(R.string.high))
        priorities.forEachIndexed { index, priority ->
            val colors = when(index) {
                0 -> FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.3f))
                1 -> FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFF9800).copy(alpha = 0.3f))
                else -> FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.3f))
            }
            FilterChip(
                selected = selectedPriority == index,
                onClick = { onPriorityChange(index) },
                label = { Text(priority) },
                colors = colors
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateSelector(dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit) {
    val context = LocalContext.current
    val formattedDate = remember(dueDateMillis) {
        dueDateMillis?.let { SimpleDateFormat.getDateTimeInstance().format(Date(it)) }
    }

    AssistChip(
        modifier = Modifier.padding(top = 8.dp),
        onClick = {
            showDatePicker(context, dueDateMillis) { newDate ->
                showTimePicker(context, newDate, onDueDateChange)
            }
        },
        label = { Text(formattedDate ?: stringResource(R.string.set_due_date)) },
        leadingIcon = { Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(18.dp)) },
        trailingIcon = if (dueDateMillis != null) {
            {
                IconButton(onClick = { onDueDateChange(null) }) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_attachment), modifier = Modifier.size(18.dp))
                }
            }
        } else { null }
    )
}

@Composable
private fun RemindersSection(
    reminders: List<Long>,
    onReminderAdded: (Long) -> Unit,
    onReminderRemoved: (Long) -> Unit
) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recordatorios", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                showDatePicker(context, null) { newDate ->
                    showTimePicker(context, newDate) { finalDateTime ->
                        finalDateTime?.let { onReminderAdded(it) }
                    }
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir recordatorio")
            }
        }

        reminders.forEach { reminderMillis ->
            ReminderItem(
                reminderMillis = reminderMillis,
                onRemove = { onReminderRemoved(reminderMillis) }
            )
        }
    }
}

@Composable
private fun ReminderItem(reminderMillis: Long, onRemove: () -> Unit) {
    val formattedDate = remember(reminderMillis) {
        SimpleDateFormat.getDateTimeInstance().format(Date(reminderMillis))
    }
    Row(
        modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(formattedDate)
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Close, contentDescription = "Quitar recordatorio", modifier = Modifier.size(18.dp))
        }
    }
}

private fun showDatePicker(context: Context, initialMillis: Long?, onDateSet: (Long) -> Unit) {
    val calendar = Calendar.getInstance().apply { initialMillis?.let { timeInMillis = it } }
    DatePickerDialog(context, { _, year, month, dayOfMonth ->
        val newCal = Calendar.getInstance().apply {
            timeInMillis = initialMillis ?: System.currentTimeMillis()
            set(year, month, dayOfMonth)
        }
        onDateSet(newCal.timeInMillis)
    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
}

private fun showTimePicker(context: Context, initialMillis: Long, onTimeSet: (Long?) -> Unit) {
    val calendar = Calendar.getInstance().apply { timeInMillis = initialMillis }
    TimePickerDialog(context, { _, hourOfDay, minute ->
        val newCal = Calendar.getInstance().apply {
            timeInMillis = initialMillis
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }
        onTimeSet(newCal.timeInMillis)
    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
}

@Composable
private fun AttachmentsSection(
    attachmentUris: List<String>,
    onAddImage: () -> Unit,
    onAddAudio: () -> Unit,
    onAddVideo: () -> Unit,
    onTakePicture: () -> Unit,
    onRecordVideo: () -> Unit,
    onRecordAudio: () -> Unit,
    isRecording: Boolean,
    onRemoveUri: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.horizontalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.attachments), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onAddImage) { Icon(Icons.Default.Image, contentDescription = stringResource(R.string.add_image)) }
            IconButton(onClick = onAddAudio) { Icon(Icons.Default.Audiotrack, contentDescription = stringResource(R.string.add_audio)) }
            IconButton(onClick = onAddVideo) { Icon(Icons.Default.Videocam, contentDescription = stringResource(R.string.add_video)) }
            IconButton(onClick = onTakePicture) { Icon(Icons.Default.PhotoCamera, contentDescription = "Take Picture") }
            IconButton(onClick = onRecordVideo) { Icon(Icons.Default.Videocam, contentDescription = "Record Video") }
            IconButton(onClick = onRecordAudio) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop Recording" else "Record Audio",
                    tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        if (attachmentUris.isNotEmpty()) {
            Row(modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState())) {
                attachmentUris.forEach { uriString ->
                    AttachmentItem(uriString = uriString, onRemoveUri = onRemoveUri)
                }
            }
        }
    }
}

@Composable
private fun AttachmentItem(uriString: String, onRemoveUri: (String) -> Unit) {
    val context = LocalContext.current
    val uri = remember { uriString.toUri() }
    val mimeType = remember(uri) { context.contentResolver.getType(uri) }
    val isVideo = mimeType?.startsWith("video/") == true
    val isAudio = mimeType?.startsWith("audio/") == true

    val viewIntent = remember {
        Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(uri)
            .build()
    )

    Box(modifier = Modifier.padding(end = 8.dp)) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                    try {
                        context.startActivity(viewIntent)
                    } catch (e: Exception) {
                        // Could show a toast message here
                        e.printStackTrace()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Fallback icon logic
            if (painter.state !is AsyncImagePainter.State.Success) {
                when {
                    isVideo -> Icon(Icons.Default.Videocam, "Video", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    isAudio -> Icon(Icons.Default.Audiotrack, "Audio", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Image for video frame, album art, or actual image
            Image(
                painter = painter,
                contentDescription = "Attachment thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Remove button
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 8.dp, y = (-8).dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .clip(CircleShape)
                .clickable { onRemoveUri(uriString) },
        ) {
            Icon(
                Icons.Default.Cancel,
                contentDescription = stringResource(R.string.remove_attachment),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}
