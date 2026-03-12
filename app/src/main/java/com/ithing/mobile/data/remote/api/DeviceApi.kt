package com.ithing.mobile.data.remote.api

import com.ithing.mobile.data.remote.dto.dashboard.DeviceListResponseDto
import com.ithing.mobile.data.remote.dto.dashboard.ListRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApi {
    @POST("device-list")
    suspend fun getDevices(@Body request: ListRequestDto): DeviceListResponseDto
}