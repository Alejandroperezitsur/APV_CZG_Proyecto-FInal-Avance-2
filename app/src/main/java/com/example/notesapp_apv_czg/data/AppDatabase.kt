package com.example.notesapp_apv_czg.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Note::class, Notification::class, Multimedia::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun notificationDao(): NotificationDao
    abstract fun multimediaDao(): MultimediaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "notes_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = inst
                inst
            }
        }
    }
}
