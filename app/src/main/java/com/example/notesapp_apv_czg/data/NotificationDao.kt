package com.example.notesapp_apv_czg.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: Notification): Long

    @Query("SELECT * FROM notifications WHERE noteId = :noteId")
    suspend fun getByNoteId(noteId: Long): List<Notification>
}
