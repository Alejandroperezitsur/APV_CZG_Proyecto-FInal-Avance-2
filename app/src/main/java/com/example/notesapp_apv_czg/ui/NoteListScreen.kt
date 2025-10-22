package com.example.notesapp_apv_czg.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note

@Composable
fun NoteListScreen(notes: List<Note>, onAdd: () -> Unit = {}, onOpen: (Long) -> Unit = {}, onSearch: (String) -> Unit = {}) {
    var query by remember { mutableStateOf("") }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxW = maxWidth
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it; onSearch(it) }, label = { Text(stringResource(id = R.string.search)) })
            Button(onClick = onAdd, modifier = Modifier.padding(top = 8.dp)) { Text(stringResource(id = R.string.new_note_task)) }
            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                items(notes) { note ->
                    NoteRow(note = note, onClick = { onOpen(note.id) })
                }
            }
        }
    }
}

@Composable
fun NoteRow(note: Note, onClick: () -> Unit) {
    Column(modifier = Modifier
        .clickable { onClick() }
        .padding(8.dp)) {
        Text(text = note.title)
        note.description?.let { Text(text = it) }
    }
}
