package com.ithing.mobile.shared.core.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createIThingHttpClient(
    apiConfig: IThingApiConfig = IThingApiConfig(),
    tokenProvider: AuthTokenProvider = NoAuthTokenProvider,
    json: Json = iThingNetworkJson
): HttpClient = createPlatformHttpClient {
    expectSuccess = false

    install(ContentNegotiation) {
        json(json)
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectTimeoutMillis = 30_000
        socketTimeoutMillis = 30_000
    }

    install(Logging) {
        logger = Logger.DEFAULT
        level = LogLevel.BODY
    }

    defaultRequest {
        url(apiConfig.baseUrl)
        contentType(ContentType.Application.Json)
        tokenProvider.accessToken()
            ?.takeIf { it.isNotBlank() }
            ?.let { token -> header(HttpHeaders.Authorization, token) }
    }
}

expect fun createPlatformHttpClient(
    config: io.ktor.client.HttpClientConfig<*>.() -> Unit
): HttpClient
