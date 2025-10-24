package com.example.notesapp_apv_czg

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesapp_apv_czg.broadcastreceivers.NotificationReceiver
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.data.NoteRepository
import com.example.notesapp_apv_czg.ui.NoteEditorScreen
import com.example.notesapp_apv_czg.ui.NoteListScreen
import com.example.notesapp_apv_czg.ui.NoteViewModel
import com.example.notesapp_apv_czg.ui.theme.NotesAppAPVCZGTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle the permission result if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()
        requestNotificationPermission()

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
                            NoteEditorScreen(
                                onSave = { note ->
                                    vm.insert(note) { id -> scheduleNotification(note.copy(id = id)) }
                                    nav.popBackStack()
                                },
                                onCancel = { nav.popBackStack() }
                            )
                        }
                        composable("edit/{id}") { backStack ->
                            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            val note = vm.notes.collectAsState().value.find { it.id == id }
                            LaunchedEffect(note) {
                                if (note == null && id != 0L) {
                                    // Note might be loading, or invalid id.
                                }
                            }
                            if (note != null) {
                                NoteEditorScreen(
                                    note = note,
                                    onSave = { updatedNote ->
                                        vm.update(updatedNote)
                                        scheduleNotification(updatedNote)
                                        nav.popBackStack()
                                    },
                                    onCancel = { nav.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NotificationReceiver.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun scheduleNotification(note: Note) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.TITLE, note.title)
            putExtra(NotificationReceiver.DESCRIPTION, note.description)
            putExtra(NotificationReceiver.NOTIFICATION_ID, note.id.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, note.id.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        note.dueDateMillis?.let {
            if (it > System.currentTimeMillis()) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                        // App cannot schedule exact alarms. Maybe navigate to settings.
                        return
                    }
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, it, pendingIntent)
                } catch (e: SecurityException) {
                    // Handle case where permission is denied
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
