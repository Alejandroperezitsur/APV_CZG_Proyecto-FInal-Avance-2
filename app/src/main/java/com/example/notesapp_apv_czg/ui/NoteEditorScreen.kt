package com.example.notesapp_apv_czg.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    val context = LocalContext.current

    val toSave = {
        onSave(
            Note(
                id = note?.id ?: 0,
                title = title,
                description = description,
                isTask = isTask,
                isCompleted = isCompleted,
                priority = priority,
                dueDateMillis = dueDateMillis
            )
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val breakpoint = 600.dp
        if (maxWidth > breakpoint) {
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
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
                Column(modifier = Modifier.width(IntrinsicSize.Min)) {
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
                    Spacer(modifier = Modifier.weight(1f))
                    Attachments(note)
                    EditorButtons(onSave = toSave, onCancel = onCancel, modifier = Modifier.fillMaxWidth())
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
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
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
                Attachments(note)
                Spacer(modifier = Modifier.weight(1f))
                EditorButtons(onSave = toSave, onCancel = onCancel, modifier = Modifier.fillMaxWidth())
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
    onDueDateChange: (Long) -> Unit
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

            Button(onClick = {
                val calendar = Calendar.getInstance()
                dueDateMillis?.let { calendar.timeInMillis = it }
                TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        onDueDateChange(calendar.timeInMillis)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
                ).show()
            }, modifier = Modifier.padding(top = 8.dp)) {
                Text(text = dueDateMillis?.let { "Due: ${java.text.SimpleDateFormat.getDateTimeInstance().format(it)}" } ?: stringResource(id = R.string.set_due_date))
            }
        }
    }
}

@Composable
private fun Attachments(note: Note?) {
    // Attachments preview row
    Row(modifier = Modifier.padding(top = 8.dp)) {
        val attachments = note?.attachmentUris ?: emptyList()
        for (uri in attachments) {
            Image(
                painter = rememberAsyncImagePainter(model = uri),
                contentDescription = null,
                modifier = Modifier
                    .size(64.dp)
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
private fun EditorButtons(onSave: () -> Unit, onCancel: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        // TODO: Hook up actual attachment picker via ActivityResultLauncher
        Button(onClick = onSave, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)) { Text(stringResource(id = R.string.save)) }
        Button(onClick = onCancel, modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)) { Text(stringResource(id = R.string.cancel)) }
    }
}
