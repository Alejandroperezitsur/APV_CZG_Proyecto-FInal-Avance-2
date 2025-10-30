package com.example.notesapp_apv_czg.ui

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesapp_apv_czg.data.Note

@Composable
fun AppScreen(
    windowSize: WindowWidthSizeClass,
    viewModel: NoteViewModel,
    onScheduleNotification: (Note) -> Unit
) {
    val navController = rememberNavController()
    val notes by viewModel.notes.collectAsState()
    val filter by viewModel.filter.collectAsState()

    when (windowSize) {
        WindowWidthSizeClass.Compact -> {
            NavHost(navController = navController, startDestination = "list") {
                composable("list") {
                    NoteListScreen(
                        notes = notes,
                        filter = filter,
                        onFilterChange = viewModel::onFilterChange,
                        onAdd = {
                            viewModel.prepareNewNote()
                            navController.navigate("edit")
                        },
                        onOpen = { id -> navController.navigate("edit/$id") },
                        onDelete = viewModel::delete
                    )
                }
                composable("edit") {
                    NoteEditorScreen(
                        viewModel = viewModel,
                        onSave = {
                            onScheduleNotification(it)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
                composable("edit/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                    LaunchedEffect(id) {
                        if (id != 0L) {
                            viewModel.loadNote(id)
                        }
                    }
                    NoteEditorScreen(
                        viewModel = viewModel,
                        onSave = {
                            onScheduleNotification(it)
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }
        }
        else -> {
            TwoPaneLayout(
                viewModel = viewModel,
                notes = notes,
                filter = filter,
                onScheduleNotification = onScheduleNotification
            )
        }
    }
}
