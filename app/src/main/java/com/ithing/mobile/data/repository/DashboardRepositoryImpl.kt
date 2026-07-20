package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.api.ReportsApi
import com.ithing.mobile.data.remote.dto.dashboard.CustomerDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.FetchEventsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.FetchLogsAfterRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.PaginationDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingFieldDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingRequestDto
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetColorValues
import com.ithing.mobile.domain.model.DashboardHeatMapLegendItem
import com.ithing.mobile.domain.model.DashboardWidgetPoint
import com.ithing.mobile.domain.model.DashboardWidgetSeries
import com.ithing.mobile.domain.model.DashboardWidgetSource
import com.ithing.mobile.domain.model.DashboardTelemetryResult
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import javax.inject.Inject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
    private val reportsApi: ReportsApi
) : DashboardRepository {
    private val listRequest = ListRequestDto(page = 1, pageSize = -1, sort = "asc")

    private var logsCache: LogsCacheEntry? = null
    private var latestEventsCache: LatestEventsCacheEntry? = null
    private data class LogsCacheEntry(
        val deviceId: String,
        val timestamp: Long,
        val limit: Int,
        val fetchedAtMillis: Long,
        val logs: List<DashboardEventLogDto>
    )

    private data class LatestEventsCacheEntry(
        val deviceId: String,
        val lastTimeStampBucket: Long,
        val fetchedAtMillis: Long,
        val logs: List<DashboardEventLogDto>
    )

    override suspend fun getIndustries(): Result<List<Industry>> = runCatching {
        val response = dashboardApi.getIndustries(
            listRequest.copy(sortField = null)
        )
        response.data.list.mapIndexed { index, name ->
            Industry(id = name, name = name)
        }
    }.onFailure { error ->
        println("DashboardRepository: getIndustries failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getOems(industry: String?): Result<List<Oem>> = runCatching {
        val response = dashboardApi.getOems(
            listRequest.copy(
                sortField = "name",
                filter = industry?.let { mapOf("industry" to it) } ?: emptyMap()
            )
        )
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        println("DashboardRepository: getOems failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getCustomers(oemId: String?): Result<List<Customer>> = runCatching {
        val response = dashboardApi.getCustomers(
            listRequest.copy(
                filter = oemId?.let { mapOf("oem" to it) } ?: emptyMap()
            )
        )
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        println("DashboardRepository: getCustomers failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getDevices(customerId: String?): Result<List<Device>> = runCatching {
        val response = dashboardApi.getDevices(
            listRequest.copy(
                filter = customerId?.let { mapOf("customer" to it) } ?: emptyMap()
            )
        )
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        println("DashboardRepository: getDevices failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getDeviceMapping(deviceId: String): Result<DeviceMappingPayloadDto> = runCatching {
        requireNotNull(
            reportsApi.getDeviceMapping(DeviceMappingRequestDto(id = deviceId)).data.data
        ) { "Device mapping not found" }
    }.onFailure { error ->
        println("DashboardRepository: getDeviceMapping failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getLogsAfter(
        deviceId: String,
        timestamp: Long,
        limit: Int
    ): Result<List<DashboardEventLogDto>> = runCatching {
        val now = System.currentTimeMillis()
        val cached = logsCache
        val isCacheValid =
            cached != null &&
                cached.deviceId == deviceId &&
                cached.timestamp == timestamp &&
                cached.limit == limit &&
                (now - cached.fetchedAtMillis) <= 30_000
        if (isCacheValid) return@runCatching cached!!.logs

        val response = dashboardApi.fetchLogsAfter(
            FetchLogsAfterRequestDto(
                device = deviceId,
                timestamp = timestamp,
                limit = limit
            )
        )
        val logs = response.data.data.orEmpty()
        logsCache = LogsCacheEntry(
            deviceId = deviceId,
            timestamp = timestamp,
            limit = limit,
            fetchedAtMillis = now,
            logs = logs
        )
        logs
    }.onFailure { error ->
        println("DashboardRepository: getLogsAfter failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getLatestEvents(
        deviceId: String,
        lastTimeStamp: Long
    ): Result<List<DashboardEventLogDto>> = runCatching {
        val now = System.currentTimeMillis()
        val bucket = lastTimeStamp / 10_000
        val cached = latestEventsCache
        val isCacheValid =
            cached != null &&
                cached.deviceId == deviceId &&
                cached.lastTimeStampBucket == bucket &&
                (now - cached.fetchedAtMillis) <= 10_000
        if (isCacheValid) return@runCatching cached!!.logs

        val response = dashboardApi.fetchEvents(
            url = FETCH_EVENTS_URL,
            request = FetchEventsRequestDto(
                device = deviceId,
                lastTimeStamp = lastTimeStamp
            )
        )
        val logs = response.data.logs
        latestEventsCache = LatestEventsCacheEntry(
            deviceId = deviceId,
            lastTimeStampBucket = bucket,
            fetchedAtMillis = now,
            logs = logs
        )
        logs
    }.onFailure { error ->
        println("DashboardRepository: getLatestEvents failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getDashboardWidgets(
        customerId: String,
        deviceId: String
    ): Result<List<DashboardWidget>> = runCatching {
        val response = dashboardApi.getDashboardWidgets(
            DashboardWidgetsRequestDto(
                customer = customerId,
                pagination = PaginationDto(
                    page = 1,
                    pageSize = -1,
                    sort = "asc",
                    sortField = "index",
                    filter = mapOf("device" to deviceId)
                )
            )
        )
        val widgets = response.data.list.map { it.toDomain() }
        widgets
    }.onFailure { error ->
        println("DashboardRepository: getDashboardWidgets failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun applyDashboardTelemetry(
        widgets: List<DashboardWidget>,
        mappingPayload: DeviceMappingPayloadDto,
        latestLogs: List<DashboardEventLogDto>,
        chartLogs: List<DashboardEventLogDto>
    ): Result<DashboardTelemetryResult> = withContext(Dispatchers.Default) {
        runCatching {
        val collatedLatestLogs = collateParsedEvents(latestLogs, mappingPayload)
        val latestLog = collatedLatestLogs.lastOrNull()

        val hasChartWidgets = widgets.any { it.type.equals("charts", ignoreCase = true) }
        val collatedChartLogs = if (hasChartWidgets && chartLogs.isNotEmpty()) {
            collateParsedEvents(logs = chartLogs, mappingPayload = mappingPayload)
        } else {
            emptyList()
        }
        val chartLogsForRendering = collatedChartLogs.evenlySampled(MAX_RENDERED_CHART_POINTS)
        val enriched = widgets.map { widget ->
            val allFields = widget.sources.flatMap { it.fields }
            val valuesByField = allFields
                .distinct()
                .mapNotNull { field ->
                    latestLog?.valueForField(field)?.let { value -> field to value }
                }
                .toMap()

            val chartSeries = if (widget.type.equals("charts", ignoreCase = true)) {
                widget.sources
                    .flatMap { source -> source.fields }
                    .distinct()
                    .map { field ->
                        DashboardWidgetSeries(
                            label = widget.unit?.takeIf { it.isNotBlank() }?.let { "$field ($it)" } ?: field,
                            points = chartLogsForRendering.mapNotNull { log ->
                                val value = log.valueForField(field) ?: return@mapNotNull null
                                DashboardWidgetPoint(
                                    timestamp = log.timestamp,
                                    label = log.label,
                                    value = value
                                )
                            }
                        )
                    }
                    .filter { it.points.isNotEmpty() }
            } else {
                emptyList()
            }

            val currentValue = allFields.firstOrNull()?.let { field -> valuesByField[field] }

            widget.copy(
                valuesByField = valuesByField,
                currentValue = currentValue,
                currentValueLabel = currentValue?.let { widget.formatValue(it) },
                chartSeries = chartSeries
            )
        }

            DashboardTelemetryResult(
                widgets = enriched,
                lastUpdatedAt = latestLog?.timestamp
            )
        }.onFailure { error ->
            println("DashboardRepository: applyDashboardTelemetry failed ${error.message}")
            error.printStackTrace()
        }
    }


    private fun com.ithing.mobile.data.remote.dto.dashboard.OemDto.toDomain() = Oem(
        id = id,
        name = name,
        industry = industry,
        logoUrl = logo
    )

    private fun CustomerDto.toDomain() = Customer(
        id = id,
        name = name,
        oemId = oem,
        industry = industry
    )

    private fun DeviceDto.toDomain() = Device(
        id = id,
        name = name,
        customerId = customer,
        oemId = oem,
        industry = industry
    )

    private fun DashboardWidgetDto.toDomain() = DashboardWidget(
        id = id,
        title = title,
        type = type,
        subType = subType,
        icon = icon,
        deviceId = device,
        dashboardName = dashboardName,
        unit = unit,
        index = index,
        sources = sources.orEmpty().mapNotNull { it.toDomainSource() }
    )

    private fun JsonObject.toDomainSource(): DashboardWidgetSource? {
        val fields = keys
            .filter { it.startsWith("field", ignoreCase = true) }
            .sorted()
            .flatMap { key -> get(key)?.toFieldNames().orEmpty() }
            .filter { it.isNotBlank() }
        if (fields.isEmpty()) return null

        val icons = get("icon").toStringListOrEmpty()
        val units = get("unit").toStringListOrEmpty()
        val minValues = get("minValue").toDoubleListOrEmpty()
        val maxValues = get("maxValue").toDoubleListOrEmpty()
        val minValue = get("minValue").toDoubleValueOrNull()
        val maxValue = get("maxValue").toDoubleValueOrNull()
        val bgColor = get("bgColor").toStringValueOrNull()
        val valueInputMode = get("valueInputMode").toStringValueOrNull()?.trim()?.lowercase()
        val bitSelection = get("bitSelection").toIntValueOrNull()
        val colorValues = get("colorValues").toColorValuesOrNull()
        val heatMapLegend = get("heatMapLegend").toHeatMapLegend()
        return DashboardWidgetSource(
            fields = fields,
            icons = icons,
            units = units,
            minValues = minValues,
            maxValues = maxValues,
            minValue = minValue,
            maxValue = maxValue,
            bgColor = bgColor,
            valueInputMode = valueInputMode,
            bitSelection = bitSelection,
            colorValues = colorValues,
            heatMapLegend = heatMapLegend
        )
    }

    private fun JsonElement?.toHeatMapLegend(): List<DashboardHeatMapLegendItem> {
        val legend = this as? JsonObject
        val defaults = listOf(
            Triple(listOf("gray", "idle"), "Idle", 0xFFAAAAAAL),
            Triple(listOf("blue", "preHeating"), "Pre Heating", 0xFF0000FFL),
            Triple(listOf("green", "cycleOn"), "Cycle On", 0xFF00FF00L),
            Triple(listOf("red", "error"), "Error", 0xFFFF0000L)
        )

        return defaults.mapIndexedNotNull { index, (keys, fallbackLabel, color) ->
            val entry = keys.firstNotNullOfOrNull { key -> legend?.get(key) }
            val entryObject = entry as? JsonObject
            val label = entryObject?.get("label").toStringValueOrNull()
                ?: (entry as? JsonPrimitive)?.contentOrNull
                ?: if (legend == null) fallbackLabel else return@mapIndexedNotNull null
            val value = entryObject?.get("value").toDoubleValueOrNull()
                ?: if (legend == null) index.toDouble() else return@mapIndexedNotNull null
            if (label.isBlank()) return@mapIndexedNotNull null
            DashboardHeatMapLegendItem(label = label, value = value, color = color)
        }
    }

    private fun JsonElement.toFieldNames(): List<String> =
        when (this) {
            is JsonArray -> mapNotNull { it.toStringValueOrNull() }
            is JsonPrimitive -> listOfNotNull(contentOrNull)
            else -> emptyList()
        }

    private fun JsonElement?.toStringValueOrNull(): String? =
        when (this) {
            null -> null
            is JsonArray -> firstOrNull().toStringValueOrNull()
            is JsonPrimitive -> contentOrNull
            else -> null
        }

    private fun JsonElement?.toStringListOrEmpty(): List<String> =
        when (this) {
            null -> emptyList()
            is JsonArray -> mapNotNull { it.toStringValueOrNull() }.filter { it.isNotBlank() }
            is JsonPrimitive -> listOfNotNull(contentOrNull).filter { it.isNotBlank() }
            else -> emptyList()
        }

    private fun JsonElement?.toDoubleListOrEmpty(): List<Double?> =
        when (this) {
            null -> emptyList()
            is JsonArray -> map { it.toDoubleOrNullFromAny() }
            is JsonPrimitive -> listOf(this.toDoubleOrNullFromAny())
            else -> emptyList()
        }

    private fun JsonElement.toDoubleOrNullFromAny(): Double? =
        when (this) {
            is JsonArray -> firstOrNull()?.toDoubleOrNullFromAny()
            is JsonPrimitive -> {
                val raw = contentOrNull?.trim().orEmpty()
                raw.toDoubleOrNull() ?: doubleOrNull
            }
            else -> null
        }

    private fun JsonElement?.toIntValueOrNull(): Int? {
        val raw = toStringValueOrNull()?.trim()
        return raw?.toIntOrNull()
            ?: when (this) {
                is JsonPrimitive -> doubleOrNull?.toInt()
                else -> null
            }
    }

    private fun JsonElement?.toColorValuesOrNull(): DashboardWidgetColorValues? {
        val obj = this as? JsonObject ?: return null
        return DashboardWidgetColorValues(
            green = obj["green"].toDoubleValueOrNull(),
            red = obj["red"].toDoubleValueOrNull(),
            yellow = obj["yellow"].toDoubleValueOrNull()
        )
    }

    private fun JsonElement?.toDoubleValueOrNull(): Double? =
        when (this) {
            null -> null
            is JsonArray -> firstOrNull().toDoubleValueOrNull()
            is JsonPrimitive -> doubleOrNull
            else -> null
        }

    private fun collateParsedEvents(
        logs: List<DashboardEventLogDto>,
        mappingPayload: DeviceMappingPayloadDto,
        collateNearby: Boolean = true
    ): List<ParsedDashboardLog> {
        val parsedLogs = logs
            .mapNotNull { log ->
                val translated = parseEvent(log.data, mappingPayload) ?: return@mapNotNull null
                val timestamp = deriveTimestamp(log.data) ?: log.timeStamp
                ParsedDashboardLog(
                    timestamp = timestamp,
                    label = deriveTimeLabel(log.data) ?: timestamp.toLabel(),
                    values = translated.filterValues { it.isFinite() }
                )
            }
            .sortedBy { it.timestamp }

        if (parsedLogs.isEmpty() || !collateNearby) return parsedLogs

        val collated = mutableListOf<ParsedDashboardLog>()
        parsedLogs.forEach { current ->
            val previous = collated.lastOrNull()
            if (previous != null && kotlin.math.abs(current.timestamp - previous.timestamp) < LOG_COLLATION_WINDOW_MS) {
                collated[collated.lastIndex] = previous.copy(
                    values = previous.values + current.values
                )
            } else {
                collated += current
            }
        }
        return collated
    }

    private fun parseEvent(
        rawData: Map<String, String>,
        mappingPayload: DeviceMappingPayloadDto
    ): Map<String, Double>? {
        val commType = rawData["Comm Type"].orEmpty()
        return if (commType.contains("can", ignoreCase = true)) {
            translateCan(mappingPayload.mapping, rawData)
        } else {
            translateModbus(mappingPayload, rawData)
        }
    }

    private fun translateCan(
        mapping: List<DeviceMappingFieldDto>,
        rawData: Map<String, String>
    ): Map<String, Double> {
        val out = mutableMapOf<String, Double>()
        mapping.forEach { field ->
            val registerName = field.registerName
            if (registerName.isBlank()) return@forEach

            val addresses = field.canAddress.mapNotNull { address ->
                val frame = address.c?.replace(Regex("[A-Z\\s]"), "Frame ") ?: return@mapNotNull null
                val byteIndex = address.b?.removePrefix("B")?.toIntOrNull()?.minus(1) ?: return@mapNotNull null
                frame to byteIndex
            }
            val rawValue = addresses.joinToString(separator = "") { (frame, _) -> rawData[frame].orEmpty() }
            val numeric = rawValue.toDoubleOrNull() ?: 0.0
            out.putRegisterValue(registerName, applyScaling(numeric, field))
        }
        return out
    }

    private fun translateModbus(
        mappingPayload: DeviceMappingPayloadDto,
        rawData: Map<String, String>
    ): Map<String, Double> {
        val out = mutableMapOf<String, Double>()
        val slaveIdNumber = rawData["ID"]
        val slaveId = mappingPayload.slaveConfig.firstOrNull { it.slaveIdNumber == slaveIdNumber }?.slaveId

        mappingPayload.mapping.forEach { field ->
            val registerName = field.registerName
            if (registerName.isBlank()) return@forEach
            if (!field.slaveId.isNullOrBlank() && slaveId != null && field.slaveId != slaveId) return@forEach

            val names = field.dName.filter { it.isNotBlank() }
            if (names.isEmpty()) return@forEach

            val value = when (field.dataType?.lowercase()) {
                "float" -> {
                    if (names.size < 2) null
                    else {
                        val lowWord = rawData[names[0]]?.toIntOrNull()
                        val highWord = rawData[names[1]]?.toIntOrNull()
                        if (lowWord == null || highWord == null) null else modbusRegistersToFloat(highWord, lowWord)
                    }
                }
                else -> {
                    val hex = names
                        .asReversed()
                        .mapNotNull { name -> rawData[name]?.toIntOrNull() }
                        .joinToString(separator = "") { it.toString(16).padStart(4, '0') }
                    if (hex.isBlank()) null else hex.toLong(16).toDouble()
                }
            } ?: return@forEach

            out.putRegisterValue(registerName, applyScaling(value, field))
        }

        return out
    }

    private fun MutableMap<String, Double>.putRegisterValue(
        registerName: String,
        value: Double
    ) {
        this[registerName] = value
        val trimmedRegisterName = registerName.trim()
        if (trimmedRegisterName.isNotEmpty() && trimmedRegisterName != registerName) {
            putIfAbsent(trimmedRegisterName, value)
        }
    }

    private fun ParsedDashboardLog.valueForField(field: String): Double? {
        values[field]?.let { return it }
        val normalizedField = field.trim()
        values[normalizedField]?.let { return it }
        return values.entries.firstOrNull { (name, _) ->
            name.trim().equals(normalizedField, ignoreCase = true)
        }?.value
    }

    private fun applyScaling(
        value: Double,
        field: DeviceMappingFieldDto
    ): Double {
        val withAddition = value + (field.additionFactor?.toDoubleOrNull() ?: 0.0)
        val divisor = field.dividingFactor?.toDoubleOrNull()
        return if (divisor == null || divisor == 0.0) withAddition else withAddition / divisor
    }

    private fun modbusRegistersToFloat(
        highWord: Int,
        lowWord: Int
    ): Double {
        val bytes = ByteBuffer.allocate(4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putShort(lowWord.toShort())
            .putShort(highWord.toShort())
            .array()
        return ByteBuffer.wrap(bytes)
            .order(ByteOrder.LITTLE_ENDIAN)
            .float
            .toDouble()
    }

    private fun deriveTimestamp(rawData: Map<String, String>): Long? {
        val date = rawData["Date"] ?: return null
        val time = rawData["Time"] ?: return null
        return runCatching {
            LocalDateTime.parse(
                "$date $time",
                DASHBOARD_TIMESTAMP_FORMATTER
            ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrNull()
    }

    private fun deriveTimeLabel(rawData: Map<String, String>): String? {
        val rawTime = rawData["Time"]?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return runCatching {
            val parsedTime = java.time.LocalTime.parse(rawTime, DASHBOARD_TIME_FORMATTER)
            DASHBOARD_TIME_LABEL_FORMATTER.format(parsedTime)
        }.getOrElse {
            rawTime
        }
    }

    private fun Long.toLabel(): String =
        java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            .format(java.util.Date(this))

    private fun Double.formatForWidget(): String =
        if (this % 1.0 == 0.0) this.toLong().toString() else String.format(java.util.Locale.US, "%.2f", this)

    private fun DashboardWidget.formatValue(value: Double): String {
        val formatted = value.formatForWidget()
        return unit?.takeIf { it.isNotBlank() }?.let { "$formatted $it" } ?: formatted
    }

    private data class ParsedDashboardLog(
        val timestamp: Long,
        val label: String,
        val values: Map<String, Double>
    )

    private companion object {
        private const val FETCH_EVENTS_URL =
            "https://o4jvg4ubjkowz6rurqqkndzelm0tuqsq.lambda-url.ap-south-1.on.aws/fetch-events"
        private const val LOG_COLLATION_WINDOW_MS = 100_000L
        private val DASHBOARD_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        private val DASHBOARD_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss")
        private val DASHBOARD_TIME_LABEL_FORMATTER = DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.US)
    }
}
