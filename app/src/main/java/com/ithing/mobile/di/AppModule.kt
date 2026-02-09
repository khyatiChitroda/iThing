package com.ithing.mobile.di

import android.content.Context
import com.ithing.mobile.data.local.datastore.AuthDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import com.ithing.mobile.data.repository.AuthRepositoryImpl
import com.ithing.mobile.domain.repository.AuthRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideApplicationContext(
        @ApplicationContext context: Context
    ): Context = context

    @Provides
    @Singleton
    fun provideAuthDataStore(
        authDataStore: AuthDataStore
    ): AuthDataStore = authDataStore

    @Provides
    @Singleton
    fun provideAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository = authRepositoryImpl
}