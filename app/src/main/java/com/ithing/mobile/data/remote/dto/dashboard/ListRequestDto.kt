package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class ListRequestDto(
    val page: Int = 1,
    val pageSize: Int = -1,
    val sort: String = "asc",
    val sortField: String? = null,
    val filter: Map<String, String> = emptyMap()
)