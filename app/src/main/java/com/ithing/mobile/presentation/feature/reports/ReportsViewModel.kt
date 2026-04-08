package com.ithing.mobile.presentation.feature.reports

import com.ithing.mobile.core.session.SessionManager
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
    val currentPage: Int = 1,
    val pageSize: Int = 10,
    val totalCount: Int = 0,
    val totalPages: Int = 0,
    val availableAnalyticsFields: List<String> = emptyList(),
    val availableSummaryFields: List<String> = emptyList(),
    val availableScheduleFields: List<String> = emptyList(),
    val isAnalyticsDialogVisible: Boolean = false,
    val analyticsDialogMessage: String? = null,
    val isSummaryDialogVisible: Boolean = false,
    val isSummaryFieldsLoading: Boolean = false,
    val summaryDialogMessage: String? = null,
    val summaryEmail: String = "",
    val summarySubject: String = "",
    val summaryBody: String = "Report is attached. Please download the attachment. Thank you.",
    val summaryTimeSpanStart: Long? = null,
    val summaryTimeSpanEnd: Long? = null,
    val summaryTimeSpanLabel: String = "",
    val summaryTimeSpanError: String? = null,
    val summarySelectedPreset: AnalyticsDatePreset = AnalyticsDatePreset.TODAY,
    val summarySelectedFields: Set<String> = emptySet(),
    val summarySearchQuery: String = "",
    val isScheduleDialogVisible: Boolean = false,
    val isScheduleFieldsLoading: Boolean = false,
    val scheduleDialogMessage: String? = null,
    val scheduleEmail: String = "",
    val scheduleSubject: String = "",
    val scheduleBody: String = "Report is attached. Please download the attachment. Thank you.",
    val scheduleFrequency: ScheduleDeliveryFrequency? = null,
    val scheduleFrequencyError: String? = null,
    val scheduleSelectedFields: Set<String> = emptySet(),
    val scheduleSearchQuery: String = "",
    val analyticsTimeSpanStart: Long? = null,
    val analyticsTimeSpanEnd: Long? = null,
    val analyticsTimeSpanLabel: String = "",
    val analyticsTimeSpanError: String? = null,
    val analyticsSelectedPreset: AnalyticsDatePreset = AnalyticsDatePreset.TODAY,
    val analyticsChartRows: List<AnalyticsChartConfigUi> = emptyList(),
    val deviceOwnerDetails: DeviceOwnerDetails? = null,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository,
    private val reportsRepository: ReportsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val fallbackAnalyticsFields = listOf(
        "Date",
        "Time",
        "Device Id",
        "Industry",
        "Customer Name"
    )

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    private var allIndustries: List<Industry> = emptyList()
    private var allOems: List<Oem> = emptyList()
    private var allCustomers: List<Customer> = emptyList()
    private var allDevices: List<Device> = emptyList()

    init {
        loadReports()
        resetSummaryState()
        resetAnalyticsState()
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
                currentPage = 1,
                totalCount = 0,
                totalPages = 0,
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
                currentPage = 1,
                totalCount = 0,
                totalPages = 0,
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
                currentPage = 1,
                totalCount = 0,
                totalPages = 0,
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
                currentPage = 1,
                totalCount = 0,
                totalPages = 0,
                availableAnalyticsFields = emptyList(),
                availableSummaryFields = emptyList(),
                availableScheduleFields = emptyList(),
                deviceOwnerDetails = null,
                errorMessage = null
            )
        }

        if (device == null) {
            return
        }

        viewModelScope.launch {
            loadSelectedDevice(device.id, page = 1, refreshing = true)
        }
    }

    fun refreshReports() {
        val deviceId = _uiState.value.selectedDevice?.id ?: return
        viewModelScope.launch {
            loadSelectedDevice(deviceId, page = _uiState.value.currentPage, refreshing = true)
        }
    }

    fun goToPage(page: Int) {
        val state = _uiState.value
        val deviceId = state.selectedDevice?.id ?: return
        if (page < 1 || page > state.totalPages || page == state.currentPage) return

        viewModelScope.launch {
            loadSelectedDevice(deviceId, page = page, refreshing = true)
        }
    }

    fun onAnalyticReportClick() {
        val state = _uiState.value
        if (state.selectedDevice == null) {
            _uiState.update {
                it.copy(analyticsDialogMessage = "Select industry, OEM, customer, and device first.")
            }
            return
        }

        val availableFields = state.schedules
            .flatMap { it.fields }
            .distinct()
            .ifEmpty { fallbackAnalyticsFields }

        if (state.isAnalyticsDialogVisible) return

        _uiState.update {
            it.copy(
                isAnalyticsDialogVisible = true,
                analyticsDialogMessage = null,
                availableAnalyticsFields = availableFields
            )
        }
    }

    fun onSummaryReportClick() {
        val state = _uiState.value
        val selectedDevice = state.selectedDevice
        if (selectedDevice == null) {
            _uiState.update {
                it.copy(summaryDialogMessage = "Select industry, OEM, customer, and device first.")
            }
            return
        }

        viewModelScope.launch {
            val defaultRange = analyticsRangeForPreset(AnalyticsDatePreset.TODAY)
                ?: (System.currentTimeMillis() to System.currentTimeMillis())
            val email = sessionManager.getUserId().orEmpty()

            _uiState.update {
                it.copy(
                    isSummaryDialogVisible = true,
                    isSummaryFieldsLoading = true,
                    summaryDialogMessage = null,
                    summaryEmail = email,
                    summarySubject = "Summary Report generate for - ${selectedDevice.id}",
                    summaryBody = "Report is attached. Please download the attachment. Thank you.",
                    summaryTimeSpanStart = defaultRange.first,
                    summaryTimeSpanEnd = defaultRange.second,
                    summaryTimeSpanLabel = analyticsDateRangeLabel(defaultRange.first, defaultRange.second),
                    summaryTimeSpanError = null,
                    summarySelectedPreset = AnalyticsDatePreset.TODAY,
                    summarySelectedFields = emptySet(),
                    summarySearchQuery = ""
                )
            }

            val mappingResult = reportsRepository.getDeviceMappingFields(selectedDevice.id)
            _uiState.update {
                it.copy(
                    availableSummaryFields = mappingResult.getOrDefault(emptyList()),
                    isSummaryFieldsLoading = false,
                    summaryDialogMessage = mappingResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onScheduleReportClick() {
        val state = _uiState.value
        val selectedDevice = state.selectedDevice
        if (selectedDevice == null) {
            _uiState.update {
                it.copy(scheduleDialogMessage = "Select industry, OEM, customer, and device first.")
            }
            return
        }

        viewModelScope.launch {
            val email = sessionManager.getUserId().orEmpty()
            _uiState.update {
                it.copy(
                    isScheduleDialogVisible = true,
                    isScheduleFieldsLoading = true,
                    scheduleDialogMessage = null,
                    scheduleEmail = email,
                    scheduleSubject = "System Report Scheduled for ...",
                    scheduleBody = "Report is attached. Please download the attachment. Thank you.",
                    scheduleFrequency = null,
                    scheduleFrequencyError = null,
                    scheduleSelectedFields = emptySet(),
                    scheduleSearchQuery = ""
                )
            }

            val mappingResult = reportsRepository.getDeviceMappingFields(selectedDevice.id)
            _uiState.update {
                it.copy(
                    availableScheduleFields = mappingResult.getOrDefault(emptyList()),
                    isScheduleFieldsLoading = false,
                    scheduleDialogMessage = mappingResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun dismissAnalyticsDialog() {
        _uiState.update {
            it.copy(isAnalyticsDialogVisible = false)
        }
        resetAnalyticsState()
    }

    fun dismissAnalyticsMessage() {
        _uiState.update { it.copy(analyticsDialogMessage = null) }
    }

    fun dismissSummaryDialog() {
        _uiState.update {
            it.copy(isSummaryDialogVisible = false)
        }
        resetSummaryState()
    }

    fun dismissSummaryMessage() {
        _uiState.update { it.copy(summaryDialogMessage = null) }
    }

    fun dismissScheduleDialog() {
        _uiState.update { it.copy(isScheduleDialogVisible = false) }
        resetScheduleState()
    }

    fun dismissScheduleMessage() {
        _uiState.update { it.copy(scheduleDialogMessage = null) }
    }

    fun onSummaryEmailChanged(value: String) {
        _uiState.update { it.copy(summaryEmail = value) }
    }

    fun onSummarySubjectChanged(value: String) {
        _uiState.update { it.copy(summarySubject = value) }
    }

    fun onSummaryBodyChanged(value: String) {
        _uiState.update { it.copy(summaryBody = value) }
    }

    fun onSummaryTimeSpanPresetSelected(preset: AnalyticsDatePreset) {
        if (preset == AnalyticsDatePreset.CUSTOM) {
            _uiState.update {
                it.copy(
                    summarySelectedPreset = preset,
                    summaryTimeSpanError = null
                )
            }
            return
        }

        val range = analyticsRangeForPreset(preset) ?: return
        _uiState.update {
            it.copy(
                summarySelectedPreset = preset,
                summaryTimeSpanStart = range.first,
                summaryTimeSpanEnd = range.second,
                summaryTimeSpanLabel = analyticsDateRangeLabel(range.first, range.second),
                summaryTimeSpanError = null
            )
        }
    }

    fun onSummaryCustomDateRangeSelected(startMillis: Long, endMillis: Long) {
        val normalizedStart = analyticsNormalizeStartOfDay(startMillis)
        val normalizedEnd = analyticsNormalizeEndOfDay(endMillis)

        _uiState.update {
            it.copy(
                summarySelectedPreset = AnalyticsDatePreset.CUSTOM,
                summaryTimeSpanStart = normalizedStart,
                summaryTimeSpanEnd = normalizedEnd,
                summaryTimeSpanLabel = analyticsDateRangeLabel(normalizedStart, normalizedEnd),
                summaryTimeSpanError = if (analyticsIsRangeWithin15Days(normalizedStart, normalizedEnd)) null
                else "Date range cannot exceed 15 days."
            )
        }
    }

    fun onSummarySearchQueryChanged(value: String) {
        _uiState.update { it.copy(summarySearchQuery = value) }
    }

    fun onSummaryFieldToggled(field: String) {
        _uiState.update { state ->
            val updated = state.summarySelectedFields.toMutableSet()
            if (!updated.add(field)) {
                updated.remove(field)
            }
            state.copy(summarySelectedFields = updated)
        }
    }

    fun onSummarySelectAllToggled() {
        _uiState.update { state ->
            val filtered = filteredSummaryFields(state)
            val shouldSelectAll = filtered.any { it !in state.summarySelectedFields }
            state.copy(
                summarySelectedFields = if (shouldSelectAll) {
                    state.summarySelectedFields + filtered
                } else {
                    state.summarySelectedFields - filtered.toSet()
                }
            )
        }
    }

    fun onSummarySaveClick() {
        _uiState.update {
            it.copy(summaryDialogMessage = "Summary report save will be wired in the next step.")
        }
    }

    fun onScheduleEmailChanged(value: String) {
        _uiState.update { it.copy(scheduleEmail = value) }
    }

    fun onScheduleSubjectChanged(value: String) {
        _uiState.update { it.copy(scheduleSubject = value) }
    }

    fun onScheduleBodyChanged(value: String) {
        _uiState.update { it.copy(scheduleBody = value) }
    }

    fun onScheduleFrequencyChanged(value: ScheduleDeliveryFrequency?) {
        _uiState.update {
            it.copy(
                scheduleFrequency = value,
                scheduleFrequencyError = null,
                scheduleSubject = if (value != null) {
                    "System Report Scheduled for - ${value.label.uppercase()}"
                } else {
                    "System Report Scheduled for ..."
                }
            )
        }
    }

    fun onScheduleSearchQueryChanged(value: String) {
        _uiState.update { it.copy(scheduleSearchQuery = value) }
    }

    fun onScheduleFieldToggled(field: String) {
        _uiState.update { state ->
            val updated = state.scheduleSelectedFields.toMutableSet()
            if (!updated.add(field)) {
                updated.remove(field)
            }
            state.copy(scheduleSelectedFields = updated)
        }
    }

    fun onScheduleSelectAllToggled() {
        _uiState.update { state ->
            val filtered = filteredScheduleFields(state)
            val shouldSelectAll = filtered.any { it !in state.scheduleSelectedFields }
            state.copy(
                scheduleSelectedFields = if (shouldSelectAll) {
                    state.scheduleSelectedFields + filtered
                } else {
                    state.scheduleSelectedFields - filtered.toSet()
                }
            )
        }
    }

    fun onScheduleSaveClick() {
        val frequencyError = if (_uiState.value.scheduleFrequency == null) {
            "Delivery Frequency field is required."
        } else {
            null
        }
        _uiState.update {
            it.copy(
                scheduleFrequencyError = frequencyError,
                scheduleDialogMessage = if (frequencyError == null) {
                    "Schedule report save will be wired in the next step."
                } else {
                    it.scheduleDialogMessage
                }
            )
        }
    }

    fun onAnalyticsTimeSpanPresetSelected(preset: AnalyticsDatePreset) {
        if (preset == AnalyticsDatePreset.CUSTOM) {
            _uiState.update {
                it.copy(
                    analyticsSelectedPreset = preset,
                    analyticsTimeSpanError = null
                )
            }
            return
        }

        val range = analyticsRangeForPreset(preset) ?: return
        _uiState.update {
            it.copy(
                analyticsSelectedPreset = preset,
                analyticsTimeSpanStart = range.first,
                analyticsTimeSpanEnd = range.second,
                analyticsTimeSpanLabel = analyticsDateRangeLabel(range.first, range.second),
                analyticsTimeSpanError = null
            )
        }
    }

    fun onAnalyticsCustomDateRangeSelected(startMillis: Long, endMillis: Long) {
        val normalizedStart = analyticsNormalizeStartOfDay(startMillis)
        val normalizedEnd = analyticsNormalizeEndOfDay(endMillis)

        _uiState.update {
            it.copy(
                analyticsSelectedPreset = AnalyticsDatePreset.CUSTOM,
                analyticsTimeSpanStart = normalizedStart,
                analyticsTimeSpanEnd = normalizedEnd,
                analyticsTimeSpanLabel = analyticsDateRangeLabel(normalizedStart, normalizedEnd),
                analyticsTimeSpanError = if (analyticsIsRangeWithin15Days(normalizedStart, normalizedEnd)) {
                    null
                } else {
                    "Date range cannot exceed 15 days."
                }
            )
        }
    }

    fun onAnalyticsRowTitleChanged(rowId: String, value: String) {
        updateAnalyticsRow(rowId) { it.copy(title = value, titleError = null) }
    }

    fun onAnalyticsRowChartTypeChanged(rowId: String, type: AnalyticsChartType?) {
        updateAnalyticsRow(rowId) { it.copy(chartType = type, chartTypeError = null) }
    }

    fun onAnalyticsRowFieldChanged(rowId: String, field: String?) {
        updateAnalyticsRow(rowId) { it.copy(selectedField = field, fieldError = null) }
    }

    fun onAnalyticsRowFrequencyChanged(rowId: String, frequency: AnalyticsFrequency?) {
        updateAnalyticsRow(rowId) { it.copy(frequency = frequency, frequencyError = null) }
    }

    fun addAnalyticsRow() {
        _uiState.update {
            it.copy(analyticsChartRows = it.analyticsChartRows + AnalyticsChartConfigUi())
        }
    }

    fun removeAnalyticsRow(rowId: String) {
        _uiState.update { state ->
            if (state.analyticsChartRows.size <= 1) {
                state
            } else {
                state.copy(analyticsChartRows = state.analyticsChartRows.filterNot { it.id == rowId })
            }
        }
    }

    fun onAnalyticsSaveViewClick() {
        if (validateAnalyticsForm()) {
            _uiState.update {
                it.copy(analyticsDialogMessage = "Save view will be wired in the next step.")
            }
        }
    }

    fun onAnalyticsGeneratePdfClick() {
        if (validateAnalyticsForm()) {
            _uiState.update {
                it.copy(analyticsDialogMessage = "Generate PDF will be wired after API URL is added.")
            }
        }
    }

    private suspend fun loadSelectedDevice(deviceId: String, page: Int, refreshing: Boolean) {
        _uiState.update {
            it.copy(
                isLoading = !refreshing,
                isRefreshing = refreshing,
                errorMessage = null
            )
        }

        val schedulesResult = reportsRepository.getReportSchedules(
            deviceId = deviceId,
            page = page,
            pageSize = _uiState.value.pageSize
        )

        _uiState.update {
            val schedulesPage = schedulesResult.getOrNull()
            val availableFields = schedulesPage?.schedules
                ?.flatMap { schedule -> schedule.fields }
                ?.distinct()
                ?.ifEmpty { fallbackAnalyticsFields }
                ?: fallbackAnalyticsFields
            it.copy(
                schedules = schedulesPage?.schedules.orEmpty(),
                currentPage = schedulesPage?.currentPage ?: it.currentPage,
                pageSize = schedulesPage?.pageSize ?: it.pageSize,
                totalCount = schedulesPage?.totalCount ?: 0,
                totalPages = schedulesPage?.totalPages ?: 0,
                availableAnalyticsFields = availableFields,
                deviceOwnerDetails = null,
                isLoading = false,
                isRefreshing = false,
                errorMessage = schedulesResult.exceptionOrNull()?.message
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

    private fun resetAnalyticsState() {
        val defaultRange = analyticsRangeForPreset(AnalyticsDatePreset.TODAY)
            ?: (System.currentTimeMillis() to System.currentTimeMillis())
        _uiState.update {
            it.copy(
                isAnalyticsDialogVisible = false,
                analyticsDialogMessage = null,
                analyticsTimeSpanStart = defaultRange.first,
                analyticsTimeSpanEnd = defaultRange.second,
                analyticsTimeSpanLabel = analyticsDateRangeLabel(defaultRange.first, defaultRange.second),
                analyticsTimeSpanError = null,
                analyticsSelectedPreset = AnalyticsDatePreset.TODAY,
                analyticsChartRows = listOf(AnalyticsChartConfigUi())
            )
        }
    }

    private fun resetSummaryState() {
        val defaultRange = analyticsRangeForPreset(AnalyticsDatePreset.TODAY)
            ?: (System.currentTimeMillis() to System.currentTimeMillis())
        _uiState.update {
            it.copy(
                isSummaryDialogVisible = false,
                isSummaryFieldsLoading = false,
                summaryDialogMessage = null,
                summaryEmail = "",
                summarySubject = "",
                summaryBody = "Report is attached. Please download the attachment. Thank you.",
                summaryTimeSpanStart = defaultRange.first,
                summaryTimeSpanEnd = defaultRange.second,
                summaryTimeSpanLabel = analyticsDateRangeLabel(defaultRange.first, defaultRange.second),
                summaryTimeSpanError = null,
                summarySelectedPreset = AnalyticsDatePreset.TODAY,
                summarySelectedFields = emptySet(),
                summarySearchQuery = ""
            )
        }
    }

    private fun resetScheduleState() {
        _uiState.update {
            it.copy(
                isScheduleDialogVisible = false,
                isScheduleFieldsLoading = false,
                scheduleDialogMessage = null,
                scheduleEmail = "",
                scheduleSubject = "",
                scheduleBody = "Report is attached. Please download the attachment. Thank you.",
                scheduleFrequency = null,
                scheduleFrequencyError = null,
                scheduleSelectedFields = emptySet(),
                scheduleSearchQuery = ""
            )
        }
    }

    fun filteredSummaryFields(state: ReportsUiState = _uiState.value): List<String> {
        val query = state.summarySearchQuery.trim()
        return if (query.isBlank()) {
            state.availableSummaryFields
        } else {
            state.availableSummaryFields.filter { it.contains(query, ignoreCase = true) }
        }
    }

    fun filteredScheduleFields(state: ReportsUiState = _uiState.value): List<String> {
        val query = state.scheduleSearchQuery.trim()
        return if (query.isBlank()) {
            state.availableScheduleFields
        } else {
            state.availableScheduleFields.filter { it.contains(query, ignoreCase = true) }
        }
    }

    private fun updateAnalyticsRow(
        rowId: String,
        transform: (AnalyticsChartConfigUi) -> AnalyticsChartConfigUi
    ) {
        _uiState.update { state ->
            state.copy(
                analyticsChartRows = state.analyticsChartRows.map { row ->
                    if (row.id == rowId) transform(row) else row
                }
            )
        }
    }

    private fun validateAnalyticsForm(): Boolean {
        val state = _uiState.value
        val start = state.analyticsTimeSpanStart
        val end = state.analyticsTimeSpanEnd
        val timeSpanError = if (start == null || end == null || !analyticsIsRangeWithin15Days(start, end)) {
            "Date range cannot exceed 15 days."
        } else {
            null
        }

        val validatedRows = state.analyticsChartRows.map { row ->
            row.copy(
                chartTypeError = if (row.chartType == null) "Chart Type field is required." else null,
                fieldError = if (row.selectedField.isNullOrBlank()) "Field is required." else null,
                frequencyError = if (row.frequency == null) "Chart frequency field required." else null
            )
        }

        _uiState.update {
            it.copy(
                analyticsTimeSpanError = timeSpanError,
                analyticsChartRows = validatedRows
            )
        }

        return timeSpanError == null && validatedRows.all { row ->
            row.chartTypeError == null && row.fieldError == null && row.frequencyError == null
        }
    }
}
