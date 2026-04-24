package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.api.ReportsApi
import com.ithing.mobile.data.remote.dto.dashboard.CustomerDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.FetchEventsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.PaginationDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingFieldDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingRequestDto
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetPoint
import com.ithing.mobile.domain.model.DashboardWidgetSeries
import com.ithing.mobile.domain.model.DashboardWidgetSource
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
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
                    filter = mapOf("device" to deviceId)
                )
            )
        )
        val widgets = response.data.list.map { it.toDomain() }
        if (widgets.isEmpty()) {
            return@runCatching emptyList()
        }

        val mappingPayload = requireNotNull(
            reportsApi.getDeviceMapping(DeviceMappingRequestDto(id = deviceId)).data.data
        ) { "Device mapping not found" }

        val logs = dashboardApi.fetchEvents(
            url = FETCH_EVENTS_URL,
            request = FetchEventsRequestDto(
                device = deviceId,
                lastTimeStamp = System.currentTimeMillis()
            )
        ).data.logs

        enrichWidgetsWithTelemetry(
            widgets = widgets,
            mappingPayload = mappingPayload,
            logs = logs
        )
    }.onFailure { error ->
        println("DashboardRepository: getDashboardWidgets failed ${error.message}")
        error.printStackTrace()
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
            .mapNotNull { key -> get(key)?.toFieldName() }
            .filter { it.isNotBlank() }
        if (fields.isEmpty()) return null

        val minValue = get("minValue").toDoubleValueOrNull()
        val maxValue = get("maxValue").toDoubleValueOrNull()
        return DashboardWidgetSource(
            fields = fields,
            minValue = minValue,
            maxValue = maxValue 
        )
    }

    private fun JsonElement.toFieldName(): String? =
        when (this) {
            is JsonArray -> firstOrNull()?.toFieldName()
            is JsonPrimitive -> contentOrNull
            else -> null
        }

    private fun JsonElement?.toDoubleValueOrNull(): Double? =
        when (this) {
            null -> null
            is JsonArray -> firstOrNull().toDoubleValueOrNull()
            is JsonPrimitive -> doubleOrNull
            else -> null
        }

    private fun enrichWidgetsWithTelemetry(
        widgets: List<DashboardWidget>,
        mappingPayload: DeviceMappingPayloadDto,
        logs: List<DashboardEventLogDto>
    ): List<DashboardWidget> {
        val collatedLogs = collateParsedEvents(logs, mappingPayload)
        val latestLog = collatedLogs.lastOrNull()

        return widgets.map { widget ->
            val chartSeries = widget.sources
                .flatMap { source -> source.fields }
                .distinct()
                .map { field ->
                    DashboardWidgetSeries(
                        label = widget.unit?.takeIf { it.isNotBlank() }?.let { "$field ($it)" } ?: field,
                        points = collatedLogs.mapNotNull { log ->
                            val value = log.values[field] ?: return@mapNotNull null
                            DashboardWidgetPoint(
                                timestamp = log.timestamp,
                                label = log.label,
                                value = value
                            )
                        }
                    )
                }
                .filter { it.points.isNotEmpty() }

            val currentValue = widget.sources
                .asSequence()
                .flatMap { it.fields.asSequence() }
                .mapNotNull { field -> latestLog?.values?.get(field) }
                .firstOrNull()

            widget.copy(
                currentValue = currentValue,
                currentValueLabel = currentValue?.let { widget.formatValue(it) } ?: "--",
                chartSeries = chartSeries
            )
        }
    }

    private fun collateParsedEvents(
        logs: List<DashboardEventLogDto>,
        mappingPayload: DeviceMappingPayloadDto
    ): List<ParsedDashboardLog> {
        val parsedLogs = logs
            .mapNotNull { log ->
                val translated = parseEvent(log.data, mappingPayload) ?: return@mapNotNull null
                val timestamp = translated["timeStamp"] as? Long ?: deriveTimestamp(log.data) ?: log.timeStamp
                ParsedDashboardLog(
                    timestamp = timestamp,
                    label = timestamp.toLabel(),
                    values = translated.filterValues { it.isFinite() }
                )
            }
            .sortedBy { it.timestamp }

        if (parsedLogs.isEmpty()) return emptyList()

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
            val registerName = field.registerName.trim()
            if (registerName.isBlank()) return@forEach

            val addresses = field.canAddress.mapNotNull { address ->
                val frame = address.c?.replace(Regex("[A-Z\\s]"), "Frame ") ?: return@mapNotNull null
                val byteIndex = address.b?.removePrefix("B")?.toIntOrNull()?.minus(1) ?: return@mapNotNull null
                frame to byteIndex
            }
            val rawValue = addresses.joinToString(separator = "") { (frame, _) -> rawData[frame].orEmpty() }
            val numeric = rawValue.toDoubleOrNull() ?: 0.0
            out[registerName] = applyScaling(numeric, field)
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
            val registerName = field.registerName.trim()
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

            out[registerName] = applyScaling(value, field)
        }

        return out
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
    }
}
