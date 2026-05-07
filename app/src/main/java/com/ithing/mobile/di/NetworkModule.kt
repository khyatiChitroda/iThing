package com.ithing.mobile.di

import com.ithing.mobile.core.network.AuthInterceptor
import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.data.remote.api.AuthApiService
import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.api.ReportsApi
import com.ithing.mobile.shared.core.network.AuthTokenProvider
import com.ithing.mobile.shared.core.network.IThingApiConfig
import com.ithing.mobile.shared.core.network.createIThingHttpClient
import com.ithing.mobile.shared.data.remote.datasource.AuthRemoteDataSource
import com.ithing.mobile.shared.data.remote.datasource.KtorAuthRemoteDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import jakarta.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideSharedAuthTokenProvider(
        sessionManager: SessionManager
    ): AuthTokenProvider {
        return AuthTokenProvider {
            runBlocking {
                sessionManager.getToken()
                    ?.removePrefix("Bearer ")
                    ?.trim()
            }
        }
    }

    @Provides
    @Singleton
    fun provideIThingHttpClient(
        authTokenProvider: AuthTokenProvider
    ): HttpClient {
        return createIThingHttpClient(
            apiConfig = IThingApiConfig(),
            tokenProvider = authTokenProvider
        )
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        httpClient: HttpClient
    ): AuthRemoteDataSource {
        return KtorAuthRemoteDataSource(httpClient)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
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

    @Provides
    @Singleton
    fun provideReportsApiService(
        retrofit: Retrofit
    ): ReportsApi {
        return retrofit.create(ReportsApi::class.java)
    }
}
