package com.example.pecscreator

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.pecscreator.Card

@Dao
interface CardDao {

    @Query("SELECT * FROM cards")
    fun getAll(): List<Card>

    @Query("SELECT * FROM cards WHERE id=:id")
    fun getByID(id: Int): List<Card>

    @Insert
    fun insert(card : Card)

    @Delete
    fun delete(card: Card)
}