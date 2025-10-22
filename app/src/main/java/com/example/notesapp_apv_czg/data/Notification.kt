package com.example.notesapp_apv_czg.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val noteId: Long,
    val triggerAtMillis: Long
)
