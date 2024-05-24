package com.tajayajaya.sportracjaya.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)

@TypeConverters(Converters::class)
abstract class RunningDatabase : RoomDatabase() {
    abstract fun getRunDao(): RunDao
}
/*
As we are using TypeConverters for the RoomDatabase we also somehow tell room that we had to use
TypeConverters and the also the location.
 */