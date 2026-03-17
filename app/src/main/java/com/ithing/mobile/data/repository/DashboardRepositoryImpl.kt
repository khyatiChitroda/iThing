package com.ithing.mobile.data.repository

import android.util.Log
import com.ithing.mobile.data.remote.api.DashboardApi
import com.ithing.mobile.data.remote.dto.dashboard.CustomerDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.PaginationDto
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import javax.inject.Inject

class DashboardRepositoryImpl @Inject constructor(
    private val dashboardApi: DashboardApi,
) : DashboardRepository {
    companion object {
        private const val TAG = "DashboardRepository"
    }

    private val listRequest = ListRequestDto(page = 1, pageSize = -1, sort = "asc")

    override suspend fun getIndustries(): Result<List<Industry>> = runCatching {
        val response = dashboardApi.getIndustries(
            listRequest.copy(sortField = null)
        )
        response.data.list.mapIndexed { index, name ->
            Industry(id = name, name = name)
        }
    }.onFailure { error ->
        Log.e(TAG, "getIndustries failed", error)
    }

    override suspend fun getOems(): Result<List<Oem>> = runCatching {
        val response = dashboardApi.getOems(
            listRequest.copy(sortField = "name")
        )
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        Log.e(TAG, "getOems failed", error)
    }

    override suspend fun getCustomers(): Result<List<Customer>> = runCatching {
        val response = dashboardApi.getCustomers(listRequest)
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        Log.e(TAG, "getCustomers failed", error)
    }

    override suspend fun getDevices(): Result<List<Device>> = runCatching {
        val response = dashboardApi.getDevices(listRequest)  // use deviceApi instead of dashboardApi
        response.data.list.map { it.toDomain() }
    }.onFailure { error ->
        Log.e(TAG, "getDevices failed", error)
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
        Log.e(TAG, "getDashboardWidgets failed", error)
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
        deviceId = device,
        dashboardName = dashboardName
    )
}
