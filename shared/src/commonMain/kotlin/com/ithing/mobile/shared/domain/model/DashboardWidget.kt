package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class DashboardWidgetSource(
    val fields: List<String> = emptyList(),
    val minValue: Double? = null,
    val maxValue: Double? = null
) {
    val field: String
        get() = fields.firstOrNull().orEmpty()
}

@Serializable
data class DashboardWidgetPoint(
    val timestamp: Long,
    val label: String,
    val value: Double
)

@Serializable
data class DashboardWidgetSeries(
    val label: String,
    val points: List<DashboardWidgetPoint>
)

@Serializable
data class DashboardWidget(
    val id: String,
    val title: String,
    val type: String,
    val subType: String? = null,
    val deviceId: String?,
    val dashboardName: String?,
    val unit: String? = null,
    val index: Int? = null,
    val sources: List<DashboardWidgetSource> = emptyList(),
    val currentValue: Double? = null,
    val currentValueLabel: String? = null,
    val chartSeries: List<DashboardWidgetSeries> = emptyList()
)
