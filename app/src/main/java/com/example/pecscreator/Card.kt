package com.example.pecscreator

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Blob

@Entity(tableName = "cards")
data class Card (
    val description: String,
    val imageUri: Bitmap?
){
    @PrimaryKey(autoGenerate = true) var id: Int = 0
}