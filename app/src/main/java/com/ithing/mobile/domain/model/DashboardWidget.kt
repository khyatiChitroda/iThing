package com.ithing.mobile.domain.model

data class DashboardWidgetSource(
    val field: String,
    val minValue: Double? = null,
    val maxValue: Double? = null
)

data class DashboardWidget(
    val id: String,
    val title: String,
    val type: String,
    val subType: String? = null,
    val deviceId: String?,
    val dashboardName: String?,
    val unit: String? = null,
    val index: Int? = null,
    val sources: List<DashboardWidgetSource> = emptyList()
)
