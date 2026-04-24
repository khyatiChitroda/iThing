package com.ithing.mobile.domain.repository

import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem

interface DashboardRepository {
    suspend fun getIndustries(): Result<List<Industry>>
    suspend fun getOems(industry: String? = null): Result<List<Oem>>
    suspend fun getCustomers(oemId: String? = null): Result<List<Customer>>
    suspend fun getDevices(customerId: String? = null): Result<List<Device>>
    suspend fun getDashboardWidgets(
        customerId: String,
        deviceId: String
    ): Result<List<DashboardWidget>>
}
