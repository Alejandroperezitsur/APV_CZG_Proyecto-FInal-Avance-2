package com.example.notesapp_apv_czg.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.OfflineNotesRepository
import com.example.notesapp_apv_czg.ui.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() { // recibe el boot del dispositivo
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            CoroutineScope(Dispatchers.IO).launch {
                val database = AppDatabase.getInstance(context)
                val repository = OfflineNotesRepository(database.noteDao())
                val notificationScheduler = NotificationScheduler(context)

                val notes = repository.getAllNotes().first()
                notes.forEach { note ->
                    if (note.isTask && !note.isCompleted) {
                        notificationScheduler.schedule(note)
                    }
                }
            }
        }
    }
}
