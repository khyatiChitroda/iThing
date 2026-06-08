package com.ithing.mobile.domain.model

data class DashboardWidgetSource(
    val fields: List<String> = emptyList(),
    val icons: List<String> = emptyList(),
    val units: List<String> = emptyList(),
    val minValues: List<Double?> = emptyList(),
    val maxValues: List<Double?> = emptyList(),
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val bgColor: String? = null,
    val valueInputMode: String? = null,
    val bitSelection: Int? = null,
    val colorValues: DashboardWidgetColorValues? = null
) {
    val field: String
        get() = fields.firstOrNull().orEmpty()
}

data class DashboardWidgetColorValues(
    val green: Double? = null,
    val red: Double? = null,
    val yellow: Double? = null
)

data class DashboardWidgetPoint(
    val timestamp: Long,
    val label: String,
    val value: Double
)

data class DashboardWidgetSeries(
    val label: String,
    val points: List<DashboardWidgetPoint>
)

data class DashboardTelemetryResult(
    val widgets: List<DashboardWidget>,
    val lastUpdatedAt: Long? = null
)

data class DashboardWidget(
    val id: String,
    val title: String,
    val type: String,
    val subType: String? = null,
    val icon: String? = null,
    val deviceId: String?,
    val dashboardName: String?,
    val unit: String? = null,
    val index: Int? = null,
    val sources: List<DashboardWidgetSource> = emptyList(),
    val valuesByField: Map<String, Double> = emptyMap(),
    val currentValue: Double? = null,
    val currentValueLabel: String? = null,
    val chartSeries: List<DashboardWidgetSeries> = emptyList()
)
