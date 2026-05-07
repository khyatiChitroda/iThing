package com.ithing.mobile.shared.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PdfViewChartConfig(
    val title: String,
    val chartType: String,
    val fields: List<String>,
    val step: String
)

@Serializable
data class ReportDataRequest(
    val deviceId: String,
    val from: String,
    val to: String,
    val step: Long,
    val fields: List<String>
)
