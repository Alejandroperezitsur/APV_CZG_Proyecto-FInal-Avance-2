package com.example.notesapp_apv_czg.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.notesapp_apv_czg.R
import com.example.notesapp_apv_czg.data.Note

@Composable
fun TwoPaneLayout(
    viewModel: NoteViewModel,
    notes: List<Note>,
    filter: NoteFilter,
    onScheduleNotification: (Note) -> Unit
) {
    Row {
        AppNavigationRail(
            currentFilter = filter,
            onFilterChange = viewModel::onFilterChange
        )

        NoteListScreen(
            notes = notes,
            filter = filter,
            onFilterChange = viewModel::onFilterChange,
            showFilterBar = false, // The rail handles filtering
            onAdd = { viewModel.prepareNewNote() },
            onOpen = { id -> viewModel.getNoteById(id) }, // Corrected to use getNoteById
            onDelete = viewModel::delete,
            modifier = Modifier.weight(1f)
        )

        NoteEditorScreen(
            viewModel = viewModel,
            onSave = { 
                onScheduleNotification(it)
                viewModel.prepareNewNote() // Reset editor after saving
            },
            onCancel = { viewModel.prepareNewNote() }, // Reset editor on cancel
            modifier = Modifier.weight(1.5f)
        )
    }
}

@Composable
private fun AppNavigationRail(
    currentFilter: NoteFilter,
    onFilterChange: (NoteFilter) -> Unit
) {
    NavigationRail {
        NavigationRailItem(
            selected = currentFilter == NoteFilter.ALL,
            onClick = { onFilterChange(NoteFilter.ALL) },
            icon = { Icon(Icons.Default.Inbox, contentDescription = null) },
            label = { Text(stringResource(R.string.all)) }
        )
        NavigationRailItem(
            selected = currentFilter == NoteFilter.NOTES,
            onClick = { onFilterChange(NoteFilter.NOTES) },
            icon = { Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null) },
            label = { Text(stringResource(R.string.notes)) }
        )
        NavigationRailItem(
            selected = currentFilter == NoteFilter.TASKS,
            onClick = { onFilterChange(NoteFilter.TASKS) },
            icon = { Icon(Icons.Default.Checklist, contentDescription = null) },
            label = { Text(stringResource(R.string.tasks)) }
        )
    }
}
