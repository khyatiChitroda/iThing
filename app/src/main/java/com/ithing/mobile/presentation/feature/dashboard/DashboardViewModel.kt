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
    val availableGroups: List<String> = listOf("All"),
    val selectedGroup: String = "All",
    val lastUpdatedAt: Long? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val logoutUseCase: LogoutUseCase,
    private val dashboardRepository: DashboardRepository,
    private val sessionManager: com.ithing.mobile.core.session.SessionManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadInitialFilters()
    }

    fun onIndustrySelected(industry: Industry?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedIndustry = industry,
                    selectedOem = null,
                    selectedCustomer = null,
                    selectedDevice = null,
                    oems = emptyList(),
                    customers = emptyList(),
                    devices = emptyList(),
                    widgets = emptyList(),
                    availableGroups = listOf("All"),
                    selectedGroup = "All",
                    lastUpdatedAt = null,
                    errorMessage = null,
                    isLoading = true
                )
            }
            loadOems(industry?.name)
        }
    }

    fun onOemSelected(oem: Oem?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedOem = oem,
                    selectedCustomer = null,
                    selectedDevice = null,
                    customers = emptyList(),
                    devices = emptyList(),
                    widgets = emptyList(),
                    availableGroups = listOf("All"),
                    selectedGroup = "All",
                    lastUpdatedAt = null,
                    errorMessage = null,
                    isLoading = true
                )
            }
            loadCustomers(oem?.id)
        }
    }

    fun onCustomerSelected(customer: Customer?) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    selectedCustomer = customer,
                    selectedDevice = null,
                    devices = emptyList(),
                    widgets = emptyList(),
                    availableGroups = listOf("All"),
                    selectedGroup = "All",
                    lastUpdatedAt = null,
                    errorMessage = null,
                    isLoading = true
                )
            }
            loadDevices(customer?.id)
        }
    }

    fun onDeviceSelected(device: Device?) {
        _uiState.update {
            it.copy(
                selectedDevice = device,
                widgets = emptyList(),
                availableGroups = listOf("All"),
                selectedGroup = "All",
                lastUpdatedAt = null,
                errorMessage = null
            )
        }
    }

    fun onGroupSelected(group: String) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            val customerId = _uiState.value.selectedCustomer?.id
            val deviceId = _uiState.value.selectedDevice?.id
            if (customerId != null && deviceId != null) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
                dashboardRepository.getDashboardWidgets(customerId, deviceId)
                    .onSuccess { widgets ->
                        val groups = listOf("All") + widgets.mapNotNull { it.dashboardName }
                            .distinct()
                            .sorted()
                        val selectedGroup = _uiState.value.selectedGroup
                            .takeIf { it in groups }
                            ?: "All"
                        _uiState.update {
                            it.copy(
                                widgets = widgets,
                                availableGroups = groups,
                                selectedGroup = selectedGroup,
                                lastUpdatedAt = System.currentTimeMillis(),
                                isRefreshing = false,
                                errorMessage = null
                            )
                        }
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
                _uiState.update {
                    it.copy(
                        widgets = emptyList(),
                        availableGroups = listOf("All"),
                        selectedGroup = "All",
                        lastUpdatedAt = null,
                        errorMessage = "Select a customer and device to load widgets"
                    )
                }
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

    private fun loadInitialFilters() {
        viewModelScope.launch {
            val token = sessionManager.getToken()
            if (token.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Session not ready. Please login again."
                    )
                }
                return@launch
            }

            dashboardRepository.getIndustries()
                .onSuccess { industries ->
                    _uiState.update {
                        it.copy(
                            industries = industries,
                            oems = emptyList(),
                            customers = emptyList(),
                            devices = emptyList(),
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load industries"
                        )
                    }
                }
        }
    }

    private suspend fun loadOems(industry: String?) {
        if (industry.isNullOrBlank()) {
            _uiState.update { it.copy(oems = emptyList(), customers = emptyList(), devices = emptyList(), isLoading = false) }
            return
        }

        dashboardRepository.getOems(industry)
            .onSuccess { oems ->
                _uiState.update {
                    it.copy(
                        oems = oems,
                        customers = emptyList(),
                        devices = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        oems = emptyList(),
                        customers = emptyList(),
                        devices = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load OEMs"
                    )
                }
            }
    }

    private suspend fun loadCustomers(oemId: String?) {
        if (oemId.isNullOrBlank()) {
            _uiState.update { it.copy(customers = emptyList(), devices = emptyList(), isLoading = false) }
            return
        }

        dashboardRepository.getCustomers(oemId)
            .onSuccess { customers ->
                _uiState.update {
                    it.copy(
                        customers = customers,
                        devices = emptyList(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        customers = emptyList(),
                        devices = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load customers"
                    )
                }
            }
    }

    private suspend fun loadDevices(customerId: String?) {
        if (customerId.isNullOrBlank()) {
            _uiState.update { it.copy(devices = emptyList(), isLoading = false) }
            return
        }

        dashboardRepository.getDevices(customerId)
            .onSuccess { devices ->
                _uiState.update {
                    it.copy(
                        devices = devices,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        devices = emptyList(),
                        isLoading = false,
                        errorMessage = error.message ?: "Failed to load devices"
                    )
                }
            }
    }
}
