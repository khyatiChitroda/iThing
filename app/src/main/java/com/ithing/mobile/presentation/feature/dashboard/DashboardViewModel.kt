package com.ithing.mobile.presentation.feature.dashboard

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
    private val dashboardRepository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadFilters()
    }

    fun loadFilters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            loadIndustries()
            loadOems()
            loadCustomers()
            loadDevices()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun loadIndustries() {
        dashboardRepository.getIndustries()
            .onSuccess { industries ->
                _uiState.update { it.copy(industries = industries) }
            }
            .onFailure { ex ->
                _uiState.update { it.copy(errorMessage = ex.message ?: "Failed to load industries") }
            }
    }

    private suspend fun loadOems() {
        dashboardRepository.getOems()
            .onSuccess { oems ->
                val filtered = _uiState.value.selectedIndustry?.let { ind ->
                    oems.filter { it.industry == ind.name }
                } ?: oems
                _uiState.update {
                    it.copy(oems = filtered, selectedOem = null, selectedCustomer = null, selectedDevice = null)
                }
            }
            .onFailure { ex->
                _uiState.update { it.copy(errorMessage = ex.message ?: "Failed to load OEMs") }
            }
    }

    private suspend fun loadCustomers() {
        dashboardRepository.getCustomers()
            .onSuccess { customers ->
                val filtered = _uiState.value.selectedOem?.let { oem ->
                    customers.filter { it.oemId == oem.id }
                } ?: customers
                _uiState.update {
                    it.copy(customers = filtered, selectedCustomer = null, selectedDevice = null)
                }
            }
            .onFailure { ex ->
                _uiState.update { it.copy(errorMessage = ex.message ?: "Failed to load customers") }
            }
    }

    private suspend fun loadDevices() {
        dashboardRepository.getDevices()
            .onSuccess { devices ->
                val filtered = _uiState.value.selectedCustomer?.let { customer ->
                    devices.filter { it.customerId == customer.id }
                } ?: devices
                _uiState.update { it.copy(devices = filtered, selectedDevice = null) }
            }
            .onFailure { ex ->
                _uiState.update { it.copy(errorMessage = ex.message ?: "Failed to load devices") }
            }
    }

    fun onIndustrySelected(industry: Industry?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(selectedIndustry = industry, selectedOem = null, selectedCustomer = null, selectedDevice = null)
            }
            loadOems()
            loadCustomers()
            loadDevices()
        }
    }

    fun onOemSelected(oem: Oem?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(selectedOem = oem, selectedCustomer = null, selectedDevice = null)
            }
            loadCustomers()
            loadDevices()
        }
    }

    fun onCustomerSelected(customer: Customer?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(selectedCustomer = customer, selectedDevice = null)
            }
            loadDevices()
        }
    }

    fun onDeviceSelected(device: Device?) {
        _uiState.update { it.copy(selectedDevice = device) }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val customerId = _uiState.value.selectedCustomer?.id
            if (customerId != null) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
                dashboardRepository.getDashboardWidgets(customerId)
                    .onSuccess { widgets ->
                        _uiState.update { it.copy(widgets = widgets, isRefreshing = false) }
                    }
                    .onFailure { ex ->
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
}