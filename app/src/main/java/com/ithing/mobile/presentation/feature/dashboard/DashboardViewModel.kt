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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

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
    private var autoRefreshJob: Job? = null
    private var dashboardRefreshJob: Job? = null
    private var cachedWidgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget> = emptyList()
    private var cachedMapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto? = null
    private var cachedChartLogs: List<com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto> = emptyList()
    private var lastFullRefreshAt: Long = 0L
    private var filterEditSnapshot: FilterEditSnapshot? = null
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadInitialFilters()
    }

    fun onIndustrySelected(industry: Industry?, autoSelectChildren: Boolean = false) {
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
            loadOems(industry?.name, autoSelectChildren = autoSelectChildren)
        }
    }

    fun onOemSelected(oem: Oem?, autoSelectChildren: Boolean = false) {
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
            loadCustomers(oem?.id, autoSelectChildren = autoSelectChildren)
        }
    }

    fun onCustomerSelected(customer: Customer?, autoSelectChildren: Boolean = false) {
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
            loadDevices(customer?.id, autoSelectChildren = autoSelectChildren)
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
        cachedWidgetsConfig = emptyList()
        cachedMapping = null
        cachedChartLogs = emptyList()

        val customerSelected = _uiState.value.selectedCustomer != null
        if (
            filterEditSnapshot == null &&
            device != null &&
            customerSelected &&
            !_uiState.value.isRefreshing
        ) {
            refreshDashboard()
        }
    }

    fun onGroupSelected(group: String) {
        _uiState.update { it.copy(selectedGroup = group) }
    }

    fun beginFilterEditing() {
        if (filterEditSnapshot != null) return
        stopAutoRefresh()
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = null
        val pausedState = _uiState.value.copy(isRefreshing = false)
        filterEditSnapshot = FilterEditSnapshot(
            uiState = pausedState,
            widgetsConfig = cachedWidgetsConfig,
            mapping = cachedMapping,
            chartLogs = cachedChartLogs
        )
        _uiState.value = pausedState
    }

    fun applyFilterEditing() {
        filterEditSnapshot = null
        val currentState = _uiState.value
        if (
            currentState.selectedCustomer != null &&
            currentState.selectedDevice != null
        ) {
            cachedWidgetsConfig = emptyList()
            cachedMapping = null
            cachedChartLogs = emptyList()
            _uiState.update {
                it.copy(
                    widgets = emptyList(),
                    availableGroups = listOf("All"),
                    selectedGroup = "All",
                    lastUpdatedAt = null,
                    isRefreshing = true,
                    errorMessage = null
                )
            }
            refreshDashboard()
        }
        startAutoRefresh()
    }

    fun cancelFilterEditing() {
        val snapshot = filterEditSnapshot ?: return
        dashboardRefreshJob?.cancel()
        cachedWidgetsConfig = snapshot.widgetsConfig
        cachedMapping = snapshot.mapping
        cachedChartLogs = snapshot.chartLogs
        _uiState.value = snapshot.uiState.copy(isRefreshing = false)
        filterEditSnapshot = null
        startAutoRefresh()
    }

    fun refreshDashboard() {
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = viewModelScope.launch {
            val customerId = _uiState.value.selectedCustomer?.id
            val deviceId = _uiState.value.selectedDevice?.id
            if (customerId != null && deviceId != null) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

                runCatching {
                    val widgetsConfig = dashboardRepository.getDashboardWidgets(customerId, deviceId).getOrThrow()
                    cachedWidgetsConfig = widgetsConfig

                    val mapping = dashboardRepository.getDeviceMapping(deviceId).getOrThrow()
                    cachedMapping = mapping

                    cachedChartLogs = dashboardRepository.getLogsAfter(
                        deviceId = deviceId,
                        timestamp = startOfDayMillis(),
                        limit = 500
                    ).getOrDefault(emptyList())

                    val latestLogs = dashboardRepository.getLatestEvents(
                        deviceId = deviceId,
                        lastTimeStamp = System.currentTimeMillis()
                    ).getOrDefault(emptyList())

                    val telemetry = dashboardRepository.applyDashboardTelemetry(
                        widgets = widgetsConfig,
                        mappingPayload = mapping,
                        latestLogs = latestLogs,
                        chartLogs = cachedChartLogs
                    ).getOrThrow()

                    telemetry
                }.onSuccess { telemetry ->
                    val groups = listOf("All") + telemetry.widgets.mapNotNull { it.dashboardName }
                        .distinct()
                        .sorted()
                    val selectedGroup = _uiState.value.selectedGroup
                        .takeIf { it in groups }
                        ?: "All"
                    _uiState.update {
                        it.copy(
                            widgets = telemetry.widgets,
                            availableGroups = groups,
                            selectedGroup = selectedGroup,
                            lastUpdatedAt = telemetry.lastUpdatedAt,
                            isRefreshing = false,
                            errorMessage = null
                        )
                    }
                    lastFullRefreshAt = System.currentTimeMillis()
                }.onFailure { ex ->
                    if (ex is CancellationException) return@onFailure
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

    fun startAutoRefresh(intervalMs: Long = 10_000L) {
        stopAutoRefresh()
        autoRefreshJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                val deviceId = _uiState.value.selectedDevice?.id
                val mapping = cachedMapping
                val widgetsConfig = cachedWidgetsConfig
                val canRefresh =
                    deviceId != null &&
                        mapping != null &&
                        widgetsConfig.isNotEmpty() &&
                        _uiState.value.selectedCustomer != null &&
                        !_uiState.value.isRefreshing

                if (canRefresh && System.currentTimeMillis() - lastFullRefreshAt >= FULL_REFRESH_INTERVAL_MS) {
                    refreshDashboard()
                } else if (canRefresh) {
                    refreshTelemetryOnly(
                        deviceId = deviceId!!,
                        mapping = mapping,
                        widgetsConfig = widgetsConfig,
                        chartLogs = cachedChartLogs
                    )
                }
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    private suspend fun refreshTelemetryOnly(
        deviceId: String,
        mapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto,
        widgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget>,
        chartLogs: List<com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto>
    ) {
        val latestLogs = dashboardRepository.getLatestEvents(
            deviceId = deviceId,
            lastTimeStamp = System.currentTimeMillis()
        ).getOrDefault(emptyList())

        dashboardRepository.applyDashboardTelemetry(
            widgets = widgetsConfig,
            mappingPayload = mapping,
            latestLogs = latestLogs,
            chartLogs = chartLogs
        ).onSuccess { telemetry ->
            val currentWidgetsById = _uiState.value.widgets.associateBy { it.id }
            val widgetsForDisplay = telemetry.widgets.map { refreshedWidget ->
                if (refreshedWidget.type.equals("charts", ignoreCase = true)) {
                    currentWidgetsById[refreshedWidget.id] ?: refreshedWidget
                } else {
                    refreshedWidget
                }
            }
            _uiState.update {
                it.copy(
                    widgets = widgetsForDisplay,
                    lastUpdatedAt = telemetry.lastUpdatedAt,
                    errorMessage = null
                )
            }
        }
    }

    private fun startOfDayMillis(): Long =
        LocalDate.now(ZoneId.systemDefault())
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

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
                    if (_uiState.value.selectedIndustry == null && industries.isNotEmpty()) {
                        onIndustrySelected(industries.first(), autoSelectChildren = true)
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

    private suspend fun loadOems(industry: String?, autoSelectChildren: Boolean = false) {
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
                if (autoSelectChildren && _uiState.value.selectedOem == null && oems.isNotEmpty()) {
                    onOemSelected(oems.first(), autoSelectChildren = true)
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

    private suspend fun loadCustomers(oemId: String?, autoSelectChildren: Boolean = false) {
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
                if (autoSelectChildren && _uiState.value.selectedCustomer == null && customers.isNotEmpty()) {
                    onCustomerSelected(customers.first(), autoSelectChildren = true)
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

    private suspend fun loadDevices(customerId: String?, autoSelectChildren: Boolean = false) {
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
                if (autoSelectChildren && _uiState.value.selectedDevice == null && devices.isNotEmpty()) {
                    onDeviceSelected(devices.first())
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

    private companion object {
        const val FULL_REFRESH_INTERVAL_MS = 60_000L
    }

    private data class FilterEditSnapshot(
        val uiState: DashboardUiState,
        val widgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget>,
        val mapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto?,
        val chartLogs: List<com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto>
    )
}
