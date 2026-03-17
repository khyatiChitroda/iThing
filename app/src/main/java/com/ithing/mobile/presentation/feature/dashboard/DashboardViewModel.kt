package com.ithing.mobile.presentation.feature.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import com.ithing.mobile.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val industries: List<Industry> = emptyList(),
    val oems: List<Oem> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val devices: List<Device> = emptyList(),
    val selectedIndustry: Industry? = null,
    val selectedOem: Oem? = null,
    val selectedCustomer: Customer? = null,
    val selectedDevice: Device? = null,
    val widgets: List<com.ithing.mobile.domain.model.DashboardWidget> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val dashboardRepository: DashboardRepository,
    private val sessionManager: com.ithing.mobile.core.session.SessionManager
) : ViewModel() {
    companion object {
        private const val TAG = "DashboardViewModel"
    }

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var allIndustries: List<Industry> = emptyList()
    private var allOems: List<Oem> = emptyList()
    private var allCustomers: List<Customer> = emptyList()
    private var allDevices: List<Device> = emptyList()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            Log.d(TAG, "loadFilters start")

            val token = sessionManager.getToken()
            Log.d(
                TAG,
                "loadFilters tokenPresent=${!token.isNullOrBlank()} token=${token?.take(16)}..."
            )

            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Session not ready. Please login again."
                    )
                }
                Log.e(TAG, "loadFilters aborted: session token missing")
                return@launch
            }

            val industriesResult = dashboardRepository.getIndustries()
            val oemsResult = dashboardRepository.getOems()
            val customersResult = dashboardRepository.getCustomers()
            val devicesResult = dashboardRepository.getDevices()

            val errorMessage = listOf(
                industriesResult.exceptionOrNull(),
                oemsResult.exceptionOrNull(),
                customersResult.exceptionOrNull(),
                devicesResult.exceptionOrNull()
            ).firstOrNull()?.message

            allIndustries = industriesResult.getOrDefault(emptyList())
            allOems = oemsResult.getOrDefault(emptyList())
            allCustomers = customersResult.getOrDefault(emptyList())
            allDevices = devicesResult.getOrDefault(emptyList())
            Log.d(
                TAG,
                "loadFilters results industries=${allIndustries.size} oems=${allOems.size} customers=${allCustomers.size} devices=${allDevices.size} error=$errorMessage"
            )

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
                widgets = emptyList(),
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
                widgets = emptyList(),
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
                widgets = emptyList(),
                errorMessage = null
            )
        }
        applyFilters()
    }

    fun onDeviceSelected(device: Device?) {
        _uiState.update {
            it.copy(
                selectedDevice = device,
                widgets = emptyList(),
                errorMessage = null
            )
        }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val customerId = _uiState.value.selectedCustomer?.id
            val deviceId = _uiState.value.selectedDevice?.id
            Log.d(
                TAG,
                "refreshDashboard customerId=$customerId deviceId=$deviceId"
            )
            if (customerId != null && deviceId != null) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
                dashboardRepository.getDashboardWidgets(customerId, deviceId)
                    .onSuccess { widgets ->
                        Log.d(TAG, "refreshDashboard widgetsLoaded=${widgets.size}")
                        _uiState.update { it.copy(widgets = widgets, isRefreshing = false) }
                    }
                    .onFailure { ex ->
                        Log.e(TAG, "refreshDashboard failed", ex)
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                errorMessage = ex.message ?: "Failed to load widgets"
                            )
                        }
                    }
            } else {
                _uiState.update { it.copy(widgets = emptyList(), errorMessage = "Select a customer to load widgets") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun logout(onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onLoggedOut()
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
