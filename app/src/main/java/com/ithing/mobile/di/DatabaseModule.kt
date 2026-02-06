package com.ithing.mobile.di

import android.content.Context
import androidx.room.Room
import com.ithing.mobile.data.local.database.IThingDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    private const val DATABASE_NAME = "ithing.db"

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): IThingDatabase =
        Room.databaseBuilder(
            context,
            IThingDatabase::class.java,
            DATABASE_NAME
        ).build()
}