package com.ithing.mobile.data.repository

import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.dto.dashboard.CustomerDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.PaginationDto
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetSource
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
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

    override suspend fun getOems(): Result<List<Oem>> = runCatching {
        val response = dashboardApi.getOems(
            listRequest.copy(sortField = "name")
        )
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        println("DashboardRepository: getOems failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getCustomers(): Result<List<Customer>> = runCatching {
        val response = dashboardApi.getCustomers(listRequest)
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        println("DashboardRepository: getCustomers failed ${error.message}")
        error.printStackTrace()
    }

    override suspend fun getDevices(): Result<List<Device>> = runCatching {
        val response = dashboardApi.getDevices(listRequest)  // use deviceApi instead of dashboardApi
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

        response.data.list.map { it.toDomain() }
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
        val field = get("field")?.toFieldName() ?: return null
        val minValue = get("minValue")?.jsonPrimitive?.doubleOrNull
        val maxValue = get("maxValue")?.jsonPrimitive?.doubleOrNull
        return DashboardWidgetSource(
            field = field,
            minValue = minValue,
            maxValue = maxValue 
        )
    }

    private fun JsonElement.toFieldName(): String? =
        when (this) {
            is JsonArray -> firstOrNull()?.jsonPrimitive?.contentOrNull
            else -> jsonPrimitive.contentOrNull
        }
}
