package com.ithing.mobile.data.remote.dto.dashboard

import kotlinx.serialization.Serializable

@Serializable
data class DashboardWidgetDto(
    val id: String,
    val title: String,
    val type: String,
    val subType: String? = null,
    val device: String? = null,
    val dashboardName: String? = null,
    val unit: String? = null,
    val index: Int? = null,
    val sources: List<kotlinx.serialization.json.JsonObject>? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)