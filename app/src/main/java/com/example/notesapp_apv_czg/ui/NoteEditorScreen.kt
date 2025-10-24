package com.example.notesapp_apv_czg.ui

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note
import java.util.*

@Composable
fun NoteEditorScreen(note: Note? = null, onSave: (Note) -> Unit = {}, onCancel: () -> Unit = {}) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var description by remember { mutableStateOf(note?.description ?: "") }
    var isTask by remember { mutableStateOf(note?.isTask ?: false) }
    var isCompleted by remember { mutableStateOf(note?.isCompleted ?: false) }
    var priority by remember { mutableIntStateOf(note?.priority ?: 0) }
    var dueDateMillis by remember { mutableStateOf(note?.dueDateMillis) }
    var attachmentUris by remember { mutableStateOf(note?.attachmentUris ?: emptyList()) }

    val context = LocalContext.current

    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                attachmentUris = attachmentUris + uri.toString()
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    val toSave = {
        onSave(
            Note(
                id = note?.id ?: 0,
                title = title,
                description = description,
                isTask = isTask,
                isCompleted = isCompleted,
                priority = priority,
                dueDateMillis = dueDateMillis,
                attachmentUris = attachmentUris
            )
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = 600.dp
        val isWideScreen = maxWidth > breakpoint

        val editorContent = @Composable { modifier: Modifier ->
            Column(
                modifier = modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(id = R.string.description)) },
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth().defaultMinSize(minHeight = if(isWideScreen) 200.dp else 120.dp)
                )
                if (!isWideScreen) {
                    TaskSpecificsAndAttachments(
                        isTask, { isTask = it }, isCompleted, { isCompleted = it }, priority, { priority = it },
                        dueDateMillis, { dueDateMillis = it }, attachmentUris, { attachmentUris = it }, mediaPickerLauncher
                    )
                }
                if(!isWideScreen) Spacer(modifier = Modifier.weight(1f))
                EditorButtons(
                    onSave = toSave,
                    onCancel = onCancel,
                    showAddButtons = !isWideScreen,
                    onAddImage = { mediaPickerLauncher.launch("image/*") },
                    onAddAudio = { mediaPickerLauncher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isWideScreen) {
            Row(modifier = Modifier.fillMaxSize()) {
                editorContent(Modifier.weight(1f))
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.width(IntrinsicSize.Min).padding(end = 16.dp, top = 16.dp).verticalScroll(rememberScrollState())) {
                    TaskSpecificsAndAttachments(
                        isTask, { isTask = it }, isCompleted, { isCompleted = it }, priority, { priority = it },
                        dueDateMillis, { dueDateMillis = it }, attachmentUris, { attachmentUris = it }, mediaPickerLauncher
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    EditorButtons(
                        onSave = toSave,
                        onCancel = onCancel,
                        showAddButtons = true,
                        onAddImage = { mediaPickerLauncher.launch("image/*") },
                        onAddAudio = { mediaPickerLauncher.launch("audio/*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            editorContent(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun TaskSpecificsAndAttachments(
    isTask: Boolean, onIsTaskChange: (Boolean) -> Unit,
    isCompleted: Boolean, onIsCompletedChange: (Boolean) -> Unit,
    priority: Int, onPriorityChange: (Int) -> Unit,
    dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit,
    attachmentUris: List<String>, onAttachmentUrisChange: (List<String>) -> Unit,
    mediaPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>
) {
    TaskSpecifics(isTask, onIsTaskChange, isCompleted, onIsCompletedChange, priority, onPriorityChange, dueDateMillis, onDueDateChange)
    Attachments(attachmentUris, onRemoveUri = { uri -> onAttachmentUrisChange(attachmentUris - uri) })
}


@Composable
private fun TaskSpecifics(
    isTask: Boolean, onIsTaskChange: (Boolean) -> Unit, isCompleted: Boolean, onIsCompletedChange: (Boolean) -> Unit,
    priority: Int, onPriorityChange: (Int) -> Unit, dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Checkbox(checked = isTask, onCheckedChange = onIsTaskChange)
        Text(stringResource(id = R.string.is_task))
    }
    if (isTask) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isCompleted, onCheckedChange = onIsCompletedChange)
                Text(stringResource(id = R.string.completed))
            }
            Text(stringResource(id = R.string.priority), modifier = Modifier.padding(top = 8.dp))
            Row {
                (0..2).forEach { priorityValue ->
                    RadioButton(selected = priority == priorityValue, onClick = { onPriorityChange(priorityValue) })
                    Text(text = when (priorityValue) {
                        0 -> stringResource(id = R.string.low)
                        1 -> stringResource(id = R.string.medium)
                        else -> stringResource(id = R.string.high)
                    })
                }
            }
            Row(modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = { showDatePicker(context, dueDateMillis, onDueDateChange) }) { Text(stringResource(R.string.set_date)) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { showTimePicker(context, dueDateMillis, onDueDateChange) }) { Text(stringResource(R.string.set_time)) }
            }
            if (dueDateMillis != null) {
                Text(text = stringResource(R.string.due_date, java.text.SimpleDateFormat.getDateTimeInstance().format(dueDateMillis)))
            }
        }
    }
}

private fun showDatePicker(context: Context, dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit) {
    val calendar = Calendar.getInstance()
    dueDateMillis?.let { calendar.timeInMillis = it }
    DatePickerDialog( context, { _, year, month, dayOfMonth ->
            val newCal = Calendar.getInstance().apply {
                timeInMillis = dueDateMillis ?: System.currentTimeMillis()
                set(year, month, dayOfMonth)
            }
            onDueDateChange(newCal.timeInMillis)
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun showTimePicker(context: Context, dueDateMillis: Long?, onDueDateChange: (Long?) -> Unit) {
    val calendar = Calendar.getInstance()
    dueDateMillis?.let { calendar.timeInMillis = it }
    TimePickerDialog( context, { _, hourOfDay, minute ->
            val newCal = Calendar.getInstance().apply {
                timeInMillis = dueDateMillis ?: System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            onDueDateChange(newCal.timeInMillis)
        },
        calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false
    ).show()
}

@Composable
private fun Attachments(attachmentUris: List<String>, onRemoveUri: (String) -> Unit) {
    val context = LocalContext.current
    Row(modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState())) {
        attachmentUris.forEach { uriString ->
            val isAudio = context.contentResolver.getType(Uri.parse(uriString))?.startsWith("audio/") == true
            Box(modifier = Modifier.padding(end = 8.dp)) {
                if (isAudio) {
                    Box(modifier = Modifier.size(80.dp).background(Color.LightGray), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Audiotrack, contentDescription = "Audio file", modifier = Modifier.size(40.dp))
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(model = Uri.parse(uriString)),
                        contentDescription = null,
                        modifier = Modifier.size(80.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.remove_attachment),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .clickable { onRemoveUri(uriString) }
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .padding(4.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun EditorButtons(
    onSave: () -> Unit, onCancel: () -> Unit, showAddButtons: Boolean,
    onAddImage: () -> Unit, onAddAudio: () -> Unit, modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (showAddButtons) {
            Row(Modifier.fillMaxWidth()) {
                Button(onClick = onAddImage, modifier = Modifier.weight(1f).padding(end = 4.dp)) { Text(stringResource(id = R.string.add_attachment)) }
                Button(onClick = onAddAudio, modifier = Modifier.weight(1f).padding(start = 4.dp)) { Text(stringResource(id = R.string.add_audio)) }
            }
        }
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(stringResource(id = R.string.save)) }
        Button(onClick = onCancel, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(stringResource(id = R.string.cancel)) }
    }
}
