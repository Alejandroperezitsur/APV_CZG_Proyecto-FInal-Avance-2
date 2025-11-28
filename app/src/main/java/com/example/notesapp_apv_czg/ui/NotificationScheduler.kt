package com.example.notesapp_apv_czg.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.notesapp_apv_czg.broadcastreceivers.NotificationReceiver
import com.example.notesapp_apv_czg.data.Note

class NotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(note: Note) {
        if (!note.isTask) return

        note.reminders.forEachIndexed { index, reminderMillis ->
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra(NotificationReceiver.TITLE, note.title)
                putExtra(NotificationReceiver.DESCRIPTION, note.description)
                // Generate a unique ID for each notification
                val notificationId = "${note.id}_$index".hashCode()
                putExtra(NotificationReceiver.NOTIFICATION_ID, notificationId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                // Use the same unique ID for the PendingIntent request code
                "${note.id}_$index".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Schedule the alarm
            try {
                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    if(alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderMillis, pendingIntent)
                    }
                } else {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderMillis, pendingIntent)
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun cancel(note: Note) {
        if (!note.isTask) return

        note.reminders.forEachIndexed { index, _ ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                "${note.id}_$index".hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}
