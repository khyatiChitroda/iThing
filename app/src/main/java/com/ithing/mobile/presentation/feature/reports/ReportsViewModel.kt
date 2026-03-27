package com.ithing.mobile.presentation.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.DeviceOwnerDetails
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.model.ReportSchedule
import com.ithing.mobile.domain.repository.DashboardRepository
import com.ithing.mobile.domain.repository.ReportsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReportsUiState(
    val industries: List<Industry> = emptyList(),
    val oems: List<Oem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val devices: List<Device> = emptyList(),
    val selectedIndustry: Industry? = null,
    val selectedOem: Oem? = null,
    val selectedCustomer: Customer? = null,
    val selectedDevice: Device? = null,
    val schedules: List<ReportSchedule> = emptyList(),
    val deviceOwnerDetails: DeviceOwnerDetails? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val reportsRepository: ReportsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var allIndustries: List<Industry> = emptyList()
    private var allOems: List<Oem> = emptyList()
    private var allCustomers: List<Customer> = emptyList()
    private var allDevices: List<Device> = emptyList()

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val industriesResult = dashboardRepository.getIndustries()
            val oemsResult = dashboardRepository.getOems()
            val customersResult = dashboardRepository.getCustomers()
            val devicesResult = dashboardRepository.getDevices()

            allIndustries = industriesResult.getOrDefault(emptyList())
            allOems = oemsResult.getOrDefault(emptyList())
            allCustomers = customersResult.getOrDefault(emptyList())
            allDevices = devicesResult.getOrDefault(emptyList())

            val errorMessage = listOf(
                industriesResult.exceptionOrNull(),
                oemsResult.exceptionOrNull(),
                customersResult.exceptionOrNull(),
                devicesResult.exceptionOrNull()
            ).firstOrNull()?.message

            _uiState.update {
                it.copy(
                    industries = allIndustries,
                    errorMessage = errorMessage
                )
            }
            applyFilters()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onIndustrySelected(industry: Industry?) {
        _uiState.update {
            it.copy(
                selectedIndustry = industry,
                selectedOem = null,
                selectedCustomer = null,
                selectedDevice = null,
                schedules = emptyList(),
                deviceOwnerDetails = null,
                errorMessage = null
            )
        }
        applyFilters()
    }

    fun onOemSelected(oem: Oem?) {
        _uiState.update {
            it.copy(
                selectedOem = oem,
                selectedCustomer = null,
                selectedDevice = null,
                schedules = emptyList(),
                deviceOwnerDetails = null,
                errorMessage = null
            )
        }
        applyFilters()
    }

    fun onCustomerSelected(customer: Customer?) {
        _uiState.update {
            it.copy(
                selectedCustomer = customer,
                selectedDevice = null,
                schedules = emptyList(),
                deviceOwnerDetails = null,
                errorMessage = null
            )
        }
        applyFilters()
    }

    fun onDeviceSelected(device: Device?) {
        _uiState.update {
            it.copy(
                selectedDevice = device,
                schedules = emptyList(),
                deviceOwnerDetails = null,
                errorMessage = null
            )
        }

        if (device == null) {
            return
        }

        viewModelScope.launch {
            loadSelectedDevice(device.id, true)
        }
    }

    fun refreshReports() {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch {
            loadSelectedDevice(deviceId, true)
        }
    }

    private suspend fun loadSelectedDevice(deviceId: String, refreshing: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = !refreshing,
                isRefreshing = refreshing,
                errorMessage = null
            )
        }

        val schedulesResult = reportsRepository.getReportSchedules(deviceId)
        val ownerDetailsResult = reportsRepository.getDeviceOwnerDetails(deviceId)

        _uiState.update {
            it.copy(
                schedules = schedulesResult.getOrDefault(emptyList()),
                deviceOwnerDetails = ownerDetailsResult.getOrNull(),
                isLoading = false,
                isRefreshing = false,
                errorMessage = listOf(
                    schedulesResult.exceptionOrNull(),
                    ownerDetailsResult.exceptionOrNull()
                ).firstOrNull()?.message
            )
        }
    }

    private fun applyFilters() {
        val state = _uiState.value

        val filteredOems = state.selectedIndustry?.let { industry ->
            allOems.filter { it.industry == industry.name }
        } ?: allOems

        val filteredCustomers = allCustomers.filter { customer ->
            val matchesIndustry = state.selectedIndustry?.name?.let { customer.industry == it } ?: true
            val matchesOem = state.selectedOem?.id?.let { customer.oemId == it } ?: true
            matchesIndustry && matchesOem
        }

        val filteredDevices = allDevices.filter { device ->
            val matchesIndustry = state.selectedIndustry?.name?.let { device.industry == it } ?: true
            val matchesOem = state.selectedOem?.id?.let { device.oemId == it } ?: true
            val matchesCustomer = state.selectedCustomer?.id?.let { device.customerId == it } ?: true
            matchesIndustry && matchesOem && matchesCustomer
        }

        _uiState.update {
            it.copy(
                industries = allIndustries,
                oems = filteredOems,
                customers = filteredCustomers,
                devices = filteredDevices
            )
        }
    }
}
