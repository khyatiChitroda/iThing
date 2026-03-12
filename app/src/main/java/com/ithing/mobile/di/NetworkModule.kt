package com.ithing.mobile.di

import com.ithing.mobile.core.network.AuthInterceptor
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.api.DeviceApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Named
import jakarta.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
        }

        return Retrofit.Builder()
            .baseUrl("https://xqs6mwcmgfu4pfazmdzyqswfye0qxvvt.lambda-url.ap-south-1.on.aws/")
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(
        retrofit: Retrofit
    ): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDashboardApiService(
        retrofit: Retrofit
    ): DashboardApi {
        return retrofit.create(DashboardApi::class.java)
    }

    private const val DEVICE_BASE_URL = "https://n4lg3qtnlgsp2h5zfba4uujlfi0zuycm.lambda-url.ap-south-1.on.aws/"

    // Add new provider (after provideRetrofit)
    @Provides
    @Singleton
    @Named("DeviceRetrofit")
    fun provideDeviceRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
        }
        return Retrofit.Builder()
            .baseUrl(DEVICE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
    // Add new provider for device API
    @Provides
    @Singleton
    fun provideDeviceApiService(
        @Named("DeviceRetrofit") retrofit: Retrofit
    ): DeviceApi {
        return retrofit.create(DeviceApi::class.java)
    }


}