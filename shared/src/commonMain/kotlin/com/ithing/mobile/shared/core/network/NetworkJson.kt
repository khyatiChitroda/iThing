package com.ithing.mobile.shared.core.network

import kotlinx.serialization.json.Json

val iThingNetworkJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}
