package com.example.notesapp_apv_czg

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import com.example.notesapp_apv_czg.broadcastreceivers.NotificationReceiver.Companion.CHANNEL_ID
import com.example.notesapp_apv_czg.data.AppDatabase
import com.example.notesapp_apv_czg.data.NotesRepository
import com.example.notesapp_apv_czg.data.OfflineNotesRepository

/**
 * Custom Application class for the app. This is the central place for dependency injection.
 */
class NotesApplication : Application(), ImageLoaderFactory {

    /**
     * The application-wide repository instance.
     */
    lateinit var container: NotesRepository

    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        container = OfflineNotesRepository(database.noteDao())
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Sets up a custom ImageLoader for Coil that can handle video frames.
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
    }
}
