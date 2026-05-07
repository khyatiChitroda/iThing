package com.ithing.mobile.shared.di

import com.ithing.mobile.shared.core.Platform
import com.ithing.mobile.shared.core.network.AuthTokenProvider
import com.ithing.mobile.shared.core.network.IThingApiConfig
import com.ithing.mobile.shared.core.network.NoAuthTokenProvider
import com.ithing.mobile.shared.core.network.createIThingHttpClient
import com.ithing.mobile.shared.core.network.iThingNetworkJson
import org.koin.dsl.module

val sharedCoreModule = module {
    single { Platform() }
    single { IThingApiConfig() }
    single { iThingNetworkJson }
    single<AuthTokenProvider> { NoAuthTokenProvider }
    single { createIThingHttpClient(get(), get(), get()) }
}
