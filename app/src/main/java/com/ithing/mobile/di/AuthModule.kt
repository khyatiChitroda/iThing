package com.ithing.mobile.di

import com.ithing.mobile.core.auth.AuthTokenProvider
import com.ithing.mobile.core.auth.StaticAuthTokenProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthTokenProvider(
        impl: StaticAuthTokenProvider
    ): AuthTokenProvider
}
