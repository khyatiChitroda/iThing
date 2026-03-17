package com.ithing.mobile.domain.model

data class DashboardWidget(
    val id: String,
    val title: String,
    val type: String,
    val deviceId: String?,
    val dashboardName: String?
)