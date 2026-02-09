package com.ithing.mobile.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ithing.mobile.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class IThingDatabase : RoomDatabase()