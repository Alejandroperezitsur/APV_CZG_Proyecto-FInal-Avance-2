package com.example.notesapp_apv_czg.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
                attachmentUris = attachmentUris + uri.toString()
            } catch (e: SecurityException) {
                // Handle error if permission cannot be taken
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
        if (maxWidth > breakpoint) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.title)) }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(stringResource(id = R.string.description)) },
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 200.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.width(IntrinsicSize.Min).verticalScroll(rememberScrollState())) {
                    TaskSpecifics(
                        isTask = isTask,
                        onIsTaskChange = { isTask = it },
                        isCompleted = isCompleted,
                        onIsCompletedChange = { isCompleted = it },
                        priority = priority,
                        onPriorityChange = { priority = it },
                        dueDateMillis = dueDateMillis,
                        onDueDateChange = { dueDateMillis = it }
                    )
                    Attachments(
                        attachmentUris = attachmentUris,
                        onRemoveUri = { uri -> attachmentUris = attachmentUris - uri }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    EditorButtons(
                        onSave = toSave,
                        onCancel = onCancel,
                        onAddAttachment = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.title)) }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(id = R.string.description)) },
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth()
                )
                TaskSpecifics(
                    isTask = isTask,
                    onIsTaskChange = { isTask = it },
                    isCompleted = isCompleted,
                    onIsCompletedChange = { isCompleted = it },
                    priority = priority,
                    onPriorityChange = { priority = it },
                    dueDateMillis = dueDateMillis,
                    onDueDateChange = { dueDateMillis = it }
                )
                Attachments(
                    attachmentUris = attachmentUris,
                    onRemoveUri = { uri -> attachmentUris = attachmentUris - uri }
                )
                Spacer(modifier = Modifier.weight(1f))
                EditorButtons(
                    onSave = toSave,
                    onCancel = onCancel,
                    onAddAttachment = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TaskSpecifics(
    isTask: Boolean,
    onIsTaskChange: (Boolean) -> Unit,
    isCompleted: Boolean,
    onIsCompletedChange: (Boolean) -> Unit,
    priority: Int,
    onPriorityChange: (Int) -> Unit,
    dueDateMillis: Long?,
    onDueDateChange: (Long?) -> Unit
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
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    dueDateMillis?.let { calendar.timeInMillis = it }
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = dueDateMillis ?: System.currentTimeMillis()
                                set(year, month, dayOfMonth)
                            }
                            onDueDateChange(newCal.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text(stringResource(R.string.set_date))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val calendar = Calendar.getInstance()
                    dueDateMillis?.let { calendar.timeInMillis = it }
                    TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            val newCal = Calendar.getInstance().apply {
                                timeInMillis = dueDateMillis ?: System.currentTimeMillis()
                                set(Calendar.HOUR_OF_DAY, hourOfDay)
                                set(Calendar.MINUTE, minute)
                            }
                            onDueDateChange(newCal.timeInMillis)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    ).show()
                }) {
                    Text(stringResource(R.string.set_time))
                }
            }
            if (dueDateMillis != null) {
                Text(text = stringResource(R.string.due_date, java.text.SimpleDateFormat.getDateTimeInstance().format(dueDateMillis)))
            }
        }
    }
}

@Composable
private fun Attachments(attachmentUris: List<String>, onRemoveUri: (String) -> Unit) {
    Row(modifier = Modifier.padding(top = 8.dp).horizontalScroll(rememberScrollState())) {
        attachmentUris.forEach { uriString ->
            Box(modifier = Modifier.padding(end = 8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(model = Uri.parse(uriString)),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp)
                )
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
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onAddAttachment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Button(
            onClick = onAddAttachment,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text(stringResource(id = R.string.add_attachment))
        }
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(id = R.string.save))
        }
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(id = R.string.cancel))
        }
    }
}
