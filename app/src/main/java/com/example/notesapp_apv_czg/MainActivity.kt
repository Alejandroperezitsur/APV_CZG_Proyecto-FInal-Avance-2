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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notesapp_apv_czg.broadcastreceivers.NotificationReceiver
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.Multimedia
import com.example.notesapp_apv_czg.data.MultimediaDao
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.data.NoteRepository
import com.example.notesapp_apv_czg.ui.NoteEditorScreen
import com.example.notesapp_apv_czg.ui.NoteListScreen
import com.example.notesapp_apv_czg.ui.NoteViewModel
import com.example.notesapp_apv_czg.ui.theme.NotesAppAPVCZGTheme

class MainActivity : ComponentActivity() {
    private lateinit var appDb: AppDatabase
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        // Handle the permission results if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()
        requestPermissions()

        appDb = AppDatabase.getInstance(applicationContext)
        val repo = NoteRepository(appDb.noteDao())

        setContent {
            NotesAppAPVCZGTheme {
                val nav = rememberNavController()
                val vm: NoteViewModel = viewModel(factory = NoteViewModelFactory(repo, appDb.multimediaDao()))
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(navController = nav, startDestination = "list", modifier = Modifier.padding(innerPadding)) {
                        composable("list") {
                            NoteListScreen(
                                notes = vm.notes.collectAsState().value,
                                onAdd = {
                                    vm.clearCurrentNote()
                                    nav.navigate("edit/0") // Navigate with a new note ID
                                },
                                onOpen = { id -> nav.navigate("edit/$id") },
                                onDelete = {
                                    vm.delete(it)
                                    cancelNotification(it)
                                }
                            )
                        }
                        composable("edit/{id}") { backStack ->
                            val id = backStack.arguments?.getString("id")?.toLongOrNull() ?: 0L
                            val noteId = if (id == 0L) null else id
                            
                            NoteEditorScreen(
                                noteId = noteId,
                                viewModel = vm,
                                onCancel = { nav.popBackStack() },
                                onSave = { savedNote ->
                                    if (savedNote.isTask && savedNote.dueDateMillis != null) {
                                        scheduleNotification(savedNote)
                                    }
                                    // Sincronizar adjuntos con la tabla multimedia
                                    syncAttachmentsToDb(savedNote)
                                    nav.popBackStack()
                                }
                            )
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

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, it, pendingIntent)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, it, pendingIntent)
                    }
                } catch (e: SecurityException) {
                    // Handle case where permission is denied
                }
            }
        } ?: run {
            // If due date is null, cancel any existing alarm for this note
            cancelNotification(note)
        }
    }

    private fun syncAttachmentsToDb(note: Note) {
        // Insertar adjuntos en la tabla Multimedia con tipo detectado
        val dao = appDb.multimediaDao()
        val attachments = note.attachmentUris
        if (attachments.isEmpty()) return
        val typeFor = { uri: String ->
            val lower = uri.lowercase()
            if (lower.endsWith(".mp3") || lower.endsWith(".m4a") || lower.endsWith(".wav") || lower.endsWith(".3gp") || lower.contains("audio")) "audio" else "image"
        }
        // Ejecutar en segundo plano
        lifecycleScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val existing = dao.getByNoteId(note.id)
                val existingUris = existing.map { it.uri }.toSet()
                attachments.filter { it !in existingUris }.forEach { uri ->
                    try {
                        dao.insert(com.example.notesapp_apv_czg.data.Multimedia(
                            noteId = note.id,
                            uri = uri,
                            type = typeFor(uri)
                        ))
                    } catch (_: Exception) { /* omit */ }
                }
            } catch (_: Exception) { /* omit */ }
        }
    }

    private fun cancelNotification(note: Note) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, note.id.toInt(), intent, PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }
}

class NoteViewModelFactory(private val repo: NoteRepository, private val multimediaDao: MultimediaDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NoteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NoteViewModel(repo, multimediaDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotesAppAPVCZGTheme {
        NoteListScreen(
            notes = emptyList(),
            onAdd = {},
            onOpen = {},
            onDelete = {}
        )
    }
}
