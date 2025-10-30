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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notesapp_apv_czg.broadcastreceivers.NotificationReceiver
import com.example.notesapp_apv_czg.data.Note
import com.example.notesapp_apv_czg.ui.AppScreen
import com.example.notesapp_apv_czg.ui.NoteFilter
import com.example.notesapp_apv_czg.ui.NoteListScreen
import com.example.notesapp_apv_czg.ui.NoteViewModel
import com.example.notesapp_apv_czg.ui.theme.NotesAppAPVCZGTheme

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Handle the permission result if needed
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        createNotificationChannel()
        requestNotificationPermission()

        setContent {
            NotesAppAPVCZGTheme {
                val windowSize = calculateWindowSizeClass(this)
                val vm: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
                AppScreen(windowSize = windowSize.widthSizeClass, viewModel = vm) {
                    scheduleNotification(it)
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
        } ?: run {
            // If due date is null, cancel any existing alarm for this note
            cancelNotification(note)
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotesAppAPVCZGTheme {
        NoteListScreen(
            notes = emptyList(),
            filter = NoteFilter.ALL,
            onFilterChange = {},
            onAdd = {},
            onOpen = {},
            onDelete = {}
        )
    }
}
