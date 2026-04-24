package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.dashboard.CustomerListResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.FetchEventsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.FetchEventsResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.DashboardWidgetsResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.DeviceListResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.IndustryListResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import com.ithing.mobile.data.remote.dto.dashboard.OemListResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface DashboardApi {

    @POST("industry-list")
    suspend fun getIndustries(@Body request: ListRequestDto): IndustryListResponseDto

    @POST("oem-list")
    suspend fun getOems(@Body request: ListRequestDto): OemListResponseDto

    @POST("customer-list")
    suspend fun getCustomers(@Body request: ListRequestDto): CustomerListResponseDto

    @POST("dashboard-widgets-list")
    suspend fun getDashboardWidgets(@Body request: DashboardWidgetsRequestDto): DashboardWidgetsResponseDto

    @POST("device-list")
    suspend fun getDevices(@Body request: ListRequestDto): DeviceListResponseDto

    @POST
    suspend fun fetchEvents(
        @Url url: String,
        @Body request: FetchEventsRequestDto
    ): FetchEventsResponseDto
}
