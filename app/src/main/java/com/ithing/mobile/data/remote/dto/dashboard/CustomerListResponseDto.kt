package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class CustomerListResponseDto(
    val data: CustomerListDataDto
)

@Serializable
data class CustomerListDataDto(
    val totalCount: Int,
    val totalPages: Int,
    val list: List<CustomerDto>,
    val currentPage: Int,
    val pageSize: Int
)