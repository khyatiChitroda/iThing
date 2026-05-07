package com.ithing.mobile.domain.model

data class PdfViewChartConfig(
    val title: String,
    val chartType: String,
    val fields: List<String>,
    val step: String
)

data class ReportDataRequest(
    val deviceId: String,
    val from: String,
    val to: String,
    val step: Long,
    val fields: List<String>
)

