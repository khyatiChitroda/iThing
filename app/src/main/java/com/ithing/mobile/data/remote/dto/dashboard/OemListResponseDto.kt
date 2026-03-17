package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class OemListResponseDto(
    val data: OemListDataDto
)

@Serializable
data class OemListDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<OemDto>,
    val currentPage: Int,
    val pageSize: Int
)