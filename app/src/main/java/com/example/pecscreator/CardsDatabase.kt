package com.example.pecscreator

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Card::class], version = 1)
@TypeConverters(Converters::class)
abstract class CardsDatabase : RoomDatabase() {
    abstract fun cardsDao(): CardDao

    companion object {
        private var instance: CardsDatabase? = null
        fun getInstance(context: Context): CardsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context,CardsDatabase::class.java,"cards.db")
                    .allowMainThreadQueries()
                    .build()
            }
            return instance as CardsDatabase
        }
    }

}