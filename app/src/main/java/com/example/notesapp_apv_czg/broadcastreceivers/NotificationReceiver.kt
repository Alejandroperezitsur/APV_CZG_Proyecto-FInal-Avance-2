package com.example.notesapp_apv_czg.broadcastreceivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.notesapp_apv_czg.MainActivity
import com.example.notesapp_apv_czg.R

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        const val NOTIFICATION_ID = "notificationId"
        const val NOTE_ID = "noteId"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CHANNEL_ID = "task_reminders_channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(TITLE) ?: context.getString(R.string.task_reminder)
        val description = intent.getStringExtra(DESCRIPTION) ?: context.getString(R.string.task_due_notification)
        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val noteId = intent.getLongExtra(NOTE_ID, -1)

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            putExtra(NOTE_ID, noteId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
