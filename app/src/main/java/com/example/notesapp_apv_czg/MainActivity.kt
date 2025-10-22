package com.example.notesapp_apv_czg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.NoteRepository
import com.example.notesapp_apv_czg.ui.NoteEditorScreen
import com.example.notesapp_apv_czg.ui.NoteListScreen
import com.example.notesapp_apv_czg.ui.NoteViewModel
import com.example.notesapp_apv_czg.ui.theme.NotesAppAPVCZGTheme
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(applicationContext)
        val repo = NoteRepository(db.noteDao())

        setContent {
            NotesAppAPVCZGTheme {
                val nav = rememberNavController()
                val vm: NoteViewModel = viewModel(factory = NoteViewModelFactory(repo))
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = nav, startDestination = "list", modifier = Modifier.padding(innerPadding)) {
                        composable("list") {
                            NoteListScreen(
                                notes = vm.notes.collectAsState().value,
                                onAdd = { nav.navigate("edit") },
                                onOpen = { id -> nav.navigate("edit/$id") },
                                onSearch = { q -> vm.search(q) }
                            )
                        }
                        composable("edit") {
                            NoteEditorScreen(onSave = { note -> vm.insert(note) ; nav.popBackStack() }, onCancel = { nav.popBackStack() })
                        }
                        composable("edit/{id}") { backStack ->
                            val idStr = backStack.arguments?.getString("id")
                            val id = idStr?.toLongOrNull() ?: 0L
                            val note = vm.notes.value.find { it.id == id }
                            NoteEditorScreen(note = note, onSave = { updatedNote -> vm.update(updatedNote); nav.popBackStack() }, onCancel = { nav.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}

class NoteViewModelFactory(private val repo: NoteRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotesAppAPVCZGTheme {
        NoteListScreen(notes = emptyList())
    }
}