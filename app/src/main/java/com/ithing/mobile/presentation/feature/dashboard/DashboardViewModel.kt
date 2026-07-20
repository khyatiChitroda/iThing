package com.ithing.mobile.presentation.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto
import com.ithing.mobile.core.session.DashboardFilterIds
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import com.ithing.mobile.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import java.time.LocalDate
import java.time.ZoneId

private const val DASHBOARD_CHART_LOG_LIMIT = 10
private const val ONE_HOUR_MS = 60 * 60 * 1_000L

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
    private var dashboardRefreshGeneration: Long = 0L
    private var cachedWidgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget> = emptyList()
    private var cachedMapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto? = null
    private var cachedChartLogs: List<com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto> = emptyList()
    private var lastFullRefreshAt: Long = 0L
    private var lastConfigurationRefreshAt: Long = 0L
    private var filterEditSnapshot: FilterEditSnapshot? = null
    private var restoredFilterIds: DashboardFilterIds? = null
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
            persistDashboardFilters()
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
            persistDashboardFilters()
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
            persistDashboardFilters()
            loadDevices(customer?.id, autoSelectChildren = autoSelectChildren)
        }
    }

    fun onDeviceSelected(device: Device?) {
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = null
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
        persistDashboardFilters()

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
        if (dashboardRefreshJob?.isActive == true) return
        val refreshGeneration = ++dashboardRefreshGeneration
        dashboardRefreshJob = viewModelScope.launch {
            val customerId = _uiState.value.selectedCustomer?.id
            val deviceId = _uiState.value.selectedDevice?.id
            if (customerId != null && deviceId != null) {
                _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }

                try {
                    val telemetry = withTimeout(DASHBOARD_REFRESH_TIMEOUT_MS) {
                        val now = System.currentTimeMillis()
                        val shouldReloadConfiguration =
                            cachedWidgetsConfig.isEmpty() ||
                                cachedMapping == null ||
                                now - lastConfigurationRefreshAt >= CONFIGURATION_REFRESH_INTERVAL_MS

                        val widgetsConfig = if (shouldReloadConfiguration) {
                            dashboardRepository.getDashboardWidgets(customerId, deviceId).getOrThrow()
                        } else {
                            cachedWidgetsConfig
                        }
                        cachedWidgetsConfig = widgetsConfig

                        val mapping = if (shouldReloadConfiguration) {
                            dashboardRepository.getDeviceMapping(deviceId).getOrThrow()
                        } else {
                            requireNotNull(cachedMapping)
                        }
                        cachedMapping = mapping
                        if (shouldReloadConfiguration) lastConfigurationRefreshAt = now

                        cachedChartLogs = fetchChartLogs(deviceId)

                        val latestLogs = dashboardRepository.getLatestEvents(
                            deviceId = deviceId,
                            lastTimeStamp = System.currentTimeMillis()
                        ).getOrDefault(emptyList())

                        val refreshedTelemetry = dashboardRepository.applyDashboardTelemetry(
                            widgets = widgetsConfig,
                            mappingPayload = mapping,
                            latestLogs = latestLogs,
                            chartLogs = cachedChartLogs
                        ).getOrThrow()

                        refreshedTelemetry
                    }
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
                            errorMessage = null
                        )
                    }
                    lastFullRefreshAt = System.currentTimeMillis()
                } catch (ex: TimeoutCancellationException) {
                    _uiState.update {
                        it.copy(
                            errorMessage = "Dashboard refresh timed out. Please try again."
                        )
                    }
                } catch (ex: CancellationException) {
                    throw ex
                } catch (ex: Exception) {
                    _uiState.update {
                        it.copy(errorMessage = ex.message ?: "Failed to load widgets")
                    }
                } finally {
                    if (refreshGeneration == dashboardRefreshGeneration) {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        widgets = emptyList(),
                        availableGroups = listOf("All"),
                        selectedGroup = "All",
                        lastUpdatedAt = null,
                        isRefreshing = false,
                        errorMessage = "Select a customer and device to load widgets"
                    )
                }
            }
        }
    }

    fun startAutoRefresh(intervalMs: Long = 10_000L) {
        stopAutoRefresh()
        val currentState = _uiState.value
        val needsInitialLoad =
            currentState.selectedCustomer != null &&
                currentState.selectedDevice != null &&
                (cachedWidgetsConfig.isEmpty() || cachedMapping == null)
        if (needsInitialLoad && dashboardRefreshJob?.isActive != true) {
            refreshDashboard()
        }
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

                val fullRefreshIsDue =
                    lastFullRefreshAt > 0L &&
                        System.currentTimeMillis() - lastFullRefreshAt >= FULL_REFRESH_INTERVAL_MS
                if (canRefresh && fullRefreshIsDue) {
                    refreshDashboard()
                } else if (canRefresh) {
                    refreshTelemetryOnly(
                        deviceId = deviceId!!,
                        mapping = mapping,
                        widgetsConfig = widgetsConfig
                    )
                }
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    fun stopDashboardWork() {
        stopAutoRefresh()
        dashboardRefreshJob?.cancel()
        dashboardRefreshJob = null
        _uiState.update { state ->
            if (state.isRefreshing) state.copy(isRefreshing = false) else state
        }
    }

    private suspend fun refreshTelemetryOnly(
        deviceId: String,
        mapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto,
        widgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget>
    ) {
        val latestLogs = dashboardRepository.getLatestEvents(
            deviceId = deviceId,
            lastTimeStamp = System.currentTimeMillis()
        ).getOrDefault(emptyList())

        dashboardRepository.applyDashboardTelemetry(
            widgets = widgetsConfig,
            mappingPayload = mapping,
            latestLogs = latestLogs,
            chartLogs = emptyList()
        ).onSuccess { telemetry ->
            val currentWidgetsById = _uiState.value.widgets.associateBy { it.id }
            val widgetsForDisplay = telemetry.widgets.map { refreshedWidget ->
                val currentWidget = currentWidgetsById[refreshedWidget.id]
                when {
                    refreshedWidget.type.equals("charts", ignoreCase = true) ->
                        currentWidget ?: refreshedWidget
                    currentWidget != null &&
                        currentWidget.valuesByField == refreshedWidget.valuesByField &&
                        currentWidget.currentValue == refreshedWidget.currentValue -> currentWidget
                    else -> refreshedWidget
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

    private suspend fun fetchChartLogs(deviceId: String): List<DashboardEventLogDto> {
        val newLogs = if (cachedChartLogs.isEmpty()) {
            fetchHourlyChartLogs(deviceId)
        } else {
            dashboardRepository.getLogsAfter(
                deviceId = deviceId,
                timestamp = cachedChartLogs.last().timeStamp + 1L,
                limit = DASHBOARD_CHART_LOG_LIMIT
            ).getOrDefault(emptyList())
        }

        return (cachedChartLogs + newLogs)
            .filter { it.timeStamp >= startOfDayMillis() }
            .distinctBy { it.timeStamp to it.data }
            .sortedBy { it.timeStamp }
    }

    private suspend fun fetchHourlyChartLogs(deviceId: String): List<DashboardEventLogDto> {
        val start = startOfDayMillis()
        val firstBatch = dashboardRepository.getLogsAfter(
            deviceId = deviceId,
            timestamp = start,
            limit = DASHBOARD_CHART_LOG_LIMIT
        ).getOrDefault(emptyList())
        val firstTimestamp = firstBatch.firstOrNull()?.timeStamp ?: return emptyList()
        val end = System.currentTimeMillis()
        var slot = firstTimestamp - (firstTimestamp % ONE_HOUR_MS) + (ONE_HOUR_MS / 2L)
        val hourlyLogs = mutableListOf<DashboardEventLogDto>()

        while (slot < end) {
            val hourlyBatch = dashboardRepository.getLogsAfter(
                deviceId = deviceId,
                timestamp = slot,
                limit = DASHBOARD_CHART_LOG_LIMIT
            ).getOrDefault(emptyList())
            hourlyLogs.addAll(hourlyBatch)
            slot += ONE_HOUR_MS
        }
        return hourlyLogs
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

            restoredFilterIds = sessionManager.getDashboardFilters()
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
                        val selected = industries.firstOrNull { it.id == restoredFilterIds?.industryId }
                            ?: industries.first()
                        onIndustrySelected(selected, autoSelectChildren = true)
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
                    val selected = oems.firstOrNull { it.id == restoredFilterIds?.oemId } ?: oems.first()
                    onOemSelected(selected, autoSelectChildren = true)
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
                    val selected = customers.firstOrNull { it.id == restoredFilterIds?.customerId }
                        ?: customers.first()
                    onCustomerSelected(selected, autoSelectChildren = true)
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
                    val selected = devices.firstOrNull { it.id == restoredFilterIds?.deviceId }
                        ?: devices.first()
                    onDeviceSelected(selected)
                    restoredFilterIds = null
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

    private fun persistDashboardFilters() {
        if (restoredFilterIds != null) return
        val state = _uiState.value
        viewModelScope.launch {
            sessionManager.saveDashboardFilters(
                industryId = state.selectedIndustry?.id,
                oemId = state.selectedOem?.id,
                customerId = state.selectedCustomer?.id,
                deviceId = state.selectedDevice?.id
            )
        }
    }

    private companion object {
        const val DASHBOARD_REFRESH_TIMEOUT_MS = 90_000L
        const val FULL_REFRESH_INTERVAL_MS = 5 * 60_000L
        const val CONFIGURATION_REFRESH_INTERVAL_MS = 5 * 60_000L
    }

    private data class FilterEditSnapshot(
        val uiState: DashboardUiState,
        val widgetsConfig: List<com.ithing.mobile.domain.model.DashboardWidget>,
        val mapping: com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto?,
        val chartLogs: List<com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto>
    )
}
