package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class IndustryListResponseDto(
    val data: IndustryListDataDto
)

@Serializable
data class IndustryListDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<String>,
    val currentPage: Int,
    val pageSize: Int
)