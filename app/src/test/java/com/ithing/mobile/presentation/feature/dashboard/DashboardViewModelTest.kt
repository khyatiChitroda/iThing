package com.ithing.mobile.presentation.feature.dashboard

import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.data.remote.dto.dashboard.DashboardEventLogDto
import com.ithing.mobile.data.remote.dto.reports.DeviceMappingPayloadDto
import com.ithing.mobile.domain.model.DashboardTelemetryResult
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.DashboardWidgetPoint
import com.ithing.mobile.domain.model.DashboardWidgetSeries
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import com.ithing.mobile.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var dashboardRepository: FakeDashboardRepository
    private lateinit var logoutUseCase: LogoutUseCase
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dashboardRepository = FakeDashboardRepository()
        logoutUseCase = mock()
        sessionManager = mock()
        runTest {
            whenever(sessionManager.getToken()).thenReturn("test-token")
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadFilters auto selects first filter chain`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.industries.size)
        assertEquals("Food", state.selectedIndustry?.name)
        assertEquals("OEM Food", state.selectedOem?.name)
        assertEquals("Customer A", state.selectedCustomer?.name)
        assertEquals("Device 1", state.selectedDevice?.name)
        assertEquals(1, dashboardRepository.dashboardWidgetRequestCount)
        assertEquals(1, dashboardRepository.deviceMappingRequestCount)
        assertEquals(0, dashboardRepository.chartLogRequests.size)
    }

    @Test
    fun `selecting industry narrows oems customers and devices`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()

        viewModel.onIndustrySelected(Industry(id = "food", name = "Food"))
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(listOf("OEM Food"), state.oems.map { it.name })
        assertTrue(state.customers.isEmpty())
        assertTrue(state.devices.isEmpty())

        viewModel.onOemSelected(state.oems.single())
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(listOf("Customer A", "Customer B"), state.customers.map { it.name })
        assertTrue(state.devices.isEmpty())

        viewModel.onCustomerSelected(
            Customer(
                id = "customer-b",
                name = "Customer B",
                oemId = "oem-food",
                industry = "Food"
            )
        )
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(listOf("Device 3"), state.devices.map { it.name })
    }

    @Test
    fun `selecting customer narrows device list and refresh loads widgets`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()

        viewModel.onIndustrySelected(Industry(id = "food", name = "Food"))
        advanceUntilIdle()
        viewModel.onOemSelected(Oem(id = "oem-food", name = "OEM Food", industry = "Food"))
        advanceUntilIdle()
        viewModel.onCustomerSelected(
            Customer(
                id = "customer-b",
                name = "Customer B",
                oemId = "oem-food",
                industry = "Food"
            )
        )
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(listOf("Device 3"), state.devices.map { it.name })

        dashboardRepository.chartLogRequests.clear()
        viewModel.onDeviceSelected(state.devices.first())
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(listOf("Throughput"), state.widgets.map { it.title })
        assertEquals("customer-b", dashboardRepository.lastRequestedCustomerId)
        assertEquals("device-c", dashboardRepository.lastRequestedDeviceId)
        assertEquals(0, dashboardRepository.chartLogRequests.size)
    }

    @Test
    fun `refresh without customer shows validation error`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()

        viewModel.onCustomerSelected(null)
        advanceUntilIdle()

        viewModel.refreshDashboard()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.widgets.isEmpty())
        assertEquals("Select a customer and device to load widgets", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `cancelling filter edit restores state without persisting draft`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()
        val originalState = viewModel.uiState.value
        clearInvocations(sessionManager)

        viewModel.beginFilterEditing()
        viewModel.onIndustrySelected(Industry(id = "auto", name = "Automotive"))
        advanceUntilIdle()
        viewModel.cancelFilterEditing()
        runCurrent()
        viewModel.stopAutoRefresh()
        advanceUntilIdle()

        assertEquals(originalState.selectedIndustry, viewModel.uiState.value.selectedIndustry)
        assertEquals(originalState.selectedDevice, viewModel.uiState.value.selectedDevice)
        verify(sessionManager, never()).saveDashboardFilters(
            industryId = "auto",
            oemId = null,
            customerId = null,
            deviceId = null
        )
    }

    @Test
    fun `applying filter edit persists final selection`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()
        clearInvocations(sessionManager)

        viewModel.beginFilterEditing()
        viewModel.onIndustrySelected(Industry(id = "auto", name = "Automotive"))
        advanceUntilIdle()
        viewModel.applyFilterEditing()
        runCurrent()
        viewModel.stopAutoRefresh()
        advanceUntilIdle()

        verify(sessionManager).saveDashboardFilters(
            industryId = "auto",
            oemId = null,
            customerId = null,
            deviceId = null
        )
    }

    @Test
    fun `auto refresh does not repeat full api chain after initial failure`() = runTest {
        dashboardRepository.telemetryError = IllegalStateException("Telemetry unavailable")
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()

        assertEquals(1, dashboardRepository.dashboardWidgetRequestCount)
        assertEquals(1, dashboardRepository.deviceMappingRequestCount)

        viewModel.startAutoRefresh(intervalMs = 1_000L)
        advanceTimeBy(3_500L)
        runCurrent()
        viewModel.stopAutoRefresh()

        assertEquals(1, dashboardRepository.dashboardWidgetRequestCount)
        assertEquals(1, dashboardRepository.deviceMappingRequestCount)
        assertEquals(0, dashboardRepository.chartLogRequests.size)
    }

    @Test
    fun `slow history does not block first dashboard or automatic telemetry`() = runTest {
        dashboardRepository.hasChartWidgets = true
        dashboardRepository.chartLogsDelayMs = 60_000L
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        runCurrent()

        assertEquals(1, viewModel.uiState.value.widgets.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(1, dashboardRepository.chartLogRequests.size)
        assertEquals(1, dashboardRepository.latestEventsRequestCount)

        viewModel.startAutoRefresh(intervalMs = 1_000L)
        advanceTimeBy(3_500L)
        runCurrent()

        assertEquals(4, dashboardRepository.latestEventsRequestCount)
        assertEquals(1, dashboardRepository.dashboardWidgetRequestCount)
        assertEquals(1, dashboardRepository.chartLogRequests.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
        viewModel.stopDashboardWork()
        advanceUntilIdle()
        assertTrue(dashboardRepository.lastAppliedChartLogs.isEmpty())
    }

    @Test
    fun `paused history restarts on resume without reloading widget configuration`() = runTest {
        dashboardRepository.hasChartWidgets = true
        dashboardRepository.chartLogsDelayMs = 60_000L
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        runCurrent()
        viewModel.stopDashboardWork()
        runCurrent()

        viewModel.startAutoRefresh()
        runCurrent()

        assertEquals(2, dashboardRepository.chartLogRequests.size)
        assertEquals(1, dashboardRepository.dashboardWidgetRequestCount)
        assertEquals(1, viewModel.uiState.value.widgets.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
        viewModel.stopDashboardWork()
        advanceUntilIdle()
    }

    @Test
    fun `history timeout leaves current dashboard visible`() = runTest {
        dashboardRepository.hasChartWidgets = true
        dashboardRepository.chartLogsDelayMs = 120_000L
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.widgets.size)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Chart history timed out. Please try refreshing.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `history failure leaves first dashboard visible`() = runTest {
        dashboardRepository.hasChartWidgets = true
        dashboardRepository.chartLogsError = IllegalStateException("History unavailable")
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.widgets.size)
        assertEquals(1, dashboardRepository.latestEventsRequestCount)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals("Unable to load chart history. Please try refreshing.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `late history updates only charts and retains newer current readings`() = runTest {
        dashboardRepository.hasChartWidgets = true
        dashboardRepository.chartLogsDelayMs = 5_000L
        dashboardRepository.chartLogs = listOf(DashboardEventLogDto(timeStamp = System.currentTimeMillis()))
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        runCurrent()
        val current = viewModel.uiState.value.widgets.single().copy(currentValue = 22.0)
        dashboardRepository.telemetryResult = DashboardTelemetryResult(listOf(current), 123L)

        viewModel.refreshDashboard()
        runCurrent()
        advanceUntilIdle()

        assertEquals(22.0, viewModel.uiState.value.widgets.single().currentValue)
        assertEquals(123L, viewModel.uiState.value.lastUpdatedAt)
        assertTrue(viewModel.uiState.value.widgets.single().chartSeries.isNotEmpty())
    }

    @Test
    fun `initial chart batch is included in telemetry`() = runTest {
        dashboardRepository.hasChartWidgets = true
        val log = DashboardEventLogDto(timeStamp = System.currentTimeMillis(), data = mapOf("D1" to "18"))
        dashboardRepository.chartLogs = listOf(log)
        DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()

        assertEquals(listOf(log), dashboardRepository.lastAppliedChartLogs)
    }

    @Test
    fun `failed refresh keeps displayed readings and timestamp`() = runTest {
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()
        val reading = viewModel.uiState.value.widgets.single().copy(
            valuesByField = mapOf("Temperature" to 18.0), currentValue = 18.0
        )
        dashboardRepository.telemetryResult = DashboardTelemetryResult(listOf(reading), 123L)
        viewModel.refreshDashboard()
        advanceUntilIdle()
        dashboardRepository.latestEventsError = IllegalStateException("Offline")

        viewModel.refreshDashboard()
        advanceUntilIdle()

        assertEquals(listOf(reading), viewModel.uiState.value.widgets)
        assertEquals(123L, viewModel.uiState.value.lastUpdatedAt)
        assertEquals("Unable to refresh dashboard. Please try again.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `empty refresh passes previous readings to telemetry and keeps timestamp`() = runTest {
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()
        val reading = viewModel.uiState.value.widgets.single().copy(
            valuesByField = mapOf("Temperature" to 18.0), currentValue = 18.0
        )
        dashboardRepository.telemetryResult = DashboardTelemetryResult(listOf(reading), 123L)
        viewModel.refreshDashboard()
        advanceUntilIdle()
        dashboardRepository.telemetryResult = null

        viewModel.startAutoRefresh(intervalMs = 1_000L)
        advanceTimeBy(1_000L)
        runCurrent()
        viewModel.stopDashboardWork()

        assertEquals(18.0, viewModel.uiState.value.widgets.single().currentValue)
        assertEquals(123L, viewModel.uiState.value.lastUpdatedAt)
    }

    @Test
    fun `manual refresh does not overlap automatic request and pause cancels it`() = runTest {
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()
        dashboardRepository.latestEventsDelayMs = 5_000L
        val requestsBefore = dashboardRepository.latestEventsRequestCount
        viewModel.startAutoRefresh(intervalMs = 1_000L)
        advanceTimeBy(1_000L)
        runCurrent()
        assertTrue(viewModel.uiState.value.isRefreshing)

        viewModel.refreshDashboard()
        runCurrent()
        assertEquals(requestsBefore + 1, dashboardRepository.latestEventsRequestCount)
        viewModel.stopDashboardWork()
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(requestsBefore + 1, dashboardRepository.latestEventsRequestCount)
    }

    @Test
    fun `device change does not reuse previous device readings`() = runTest {
        val viewModel = DashboardViewModel(logoutUseCase, dashboardRepository, sessionManager)
        advanceUntilIdle()
        val reading = viewModel.uiState.value.widgets.single().copy(currentValue = 18.0)
        dashboardRepository.telemetryResult = DashboardTelemetryResult(listOf(reading), 123L)
        viewModel.refreshDashboard()
        advanceUntilIdle()
        dashboardRepository.telemetryResult = null

        viewModel.onDeviceSelected(viewModel.uiState.value.devices.last())
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.widgets.single().currentValue)
        assertEquals(null, viewModel.uiState.value.lastUpdatedAt)
    }

    private class FakeDashboardRepository : DashboardRepository {

        var lastRequestedCustomerId: String? = null
        var lastRequestedDeviceId: String? = null
        var dashboardWidgetRequestCount: Int = 0
        var deviceMappingRequestCount: Int = 0
        var telemetryError: Throwable? = null
        var telemetryResult: DashboardTelemetryResult? = null
        var latestEventsError: Throwable? = null
        var latestEventsDelayMs: Long = 0L
        var latestEventsRequestCount: Int = 0
        var hasChartWidgets: Boolean = false
        var chartLogsDelayMs: Long = 0L
        var chartLogsError: Throwable? = null
        var chartLogs: List<DashboardEventLogDto> = emptyList()
        var lastAppliedChartLogs: List<DashboardEventLogDto> = emptyList()
        val chartLogRequests = mutableListOf<ChartLogRequest>()

        private val fakeIndustries = listOf(
            Industry(id = "food", name = "Food"),
            Industry(id = "auto", name = "Automotive")
        )
        private val fakeOems = listOf(
            Oem(id = "oem-food", name = "OEM Food", industry = "Food"),
            Oem(id = "oem-auto", name = "OEM Auto", industry = "Automotive")
        )

        private val fakeCustomers = listOf(
            Customer(id = "customer-a", name = "Customer A", oemId = "oem-food", industry = "Food"),
            Customer(id = "customer-b", name = "Customer B", oemId = "oem-food", industry = "Food"),
            Customer(id = "customer-c", name = "Customer C", oemId = "oem-auto", industry = "Automotive")
        )

        private val fakeDevices = listOf(
            Device(id = "device-a", name = "Device 1", customerId = "customer-a", oemId = "oem-food", industry = "Food"),
            Device(id = "device-b", name = "Device 2", customerId = "customer-a", oemId = "oem-food", industry = "Food"),
            Device(id = "device-c", name = "Device 3", customerId = "customer-b", oemId = "oem-food", industry = "Food"),
            Device(id = "device-d", name = "Device 4", customerId = "customer-c", oemId = "oem-auto", industry = "Automotive")
        )

        private val fakeWidgets = listOf(
            DashboardWidget(
                id = "widget-1",
                title = "Throughput",
                type = "metric",
                deviceId = "device-c",
                dashboardName = "Main"
            )
        )


        override suspend fun getIndustries(): Result<List<Industry>> {
            return Result.success(fakeIndustries)
        }

        override suspend fun getOems(industry: String?): Result<List<Oem>> {
            return Result.success(
                fakeOems.filter { oem ->
                    industry.isNullOrBlank() || oem.industry == industry
                }
            )
        }

        override suspend fun getCustomers(oemId: String?): Result<List<Customer>> {
            return Result.success(
                fakeCustomers.filter { customer ->
                    oemId.isNullOrBlank() || customer.oemId == oemId
                }
            )
        }

        override suspend fun getDevices(customerId: String?): Result<List<Device>> {
            return Result.success(
                fakeDevices.filter { device ->
                    customerId.isNullOrBlank() || device.customerId == customerId
                }
            )
        }

        override suspend fun getDashboardWidgets(
            customerId: String,
            deviceId: String
        ): Result<List<DashboardWidget>> {
            dashboardWidgetRequestCount++
            lastRequestedCustomerId = customerId
            lastRequestedDeviceId = deviceId

            return Result.success(fakeWidgets.map { if (hasChartWidgets) it.copy(type = "charts") else it })
        }

        override suspend fun getDeviceMapping(deviceId: String): Result<DeviceMappingPayloadDto> {
            deviceMappingRequestCount++
            return Result.success(DeviceMappingPayloadDto())
        }

        override suspend fun getLatestEvents(
            deviceId: String,
            lastTimeStamp: Long
        ): Result<List<DashboardEventLogDto>> {
            latestEventsRequestCount++
            delay(latestEventsDelayMs)
            return latestEventsError?.let(Result.Companion::failure) ?: Result.success(emptyList())
        }

        override suspend fun getLogsAfter(
            deviceId: String,
            timestamp: Long,
            limit: Int
        ): Result<List<DashboardEventLogDto>> {
            chartLogRequests += ChartLogRequest(
                deviceId = deviceId,
                timestamp = timestamp,
                limit = limit
            )
            delay(chartLogsDelayMs)
            return chartLogsError?.let(Result.Companion::failure) ?: Result.success(chartLogs)
        }

        override suspend fun applyDashboardTelemetry(
            widgets: List<DashboardWidget>,
            mappingPayload: DeviceMappingPayloadDto,
            latestLogs: List<DashboardEventLogDto>,
            chartLogs: List<DashboardEventLogDto>
        ): Result<DashboardTelemetryResult> {
            lastAppliedChartLogs = chartLogs
            if (chartLogs.isNotEmpty()) {
                return Result.success(DashboardTelemetryResult(widgets.map { widget ->
                    widget.copy(chartSeries = listOf(DashboardWidgetSeries(
                        "History", chartLogs.map { DashboardWidgetPoint(it.timeStamp, "sample", 18.0) }
                    )))
                }))
            }
            return telemetryError?.let(Result.Companion::failure)
                ?: Result.success(telemetryResult ?: DashboardTelemetryResult(widgets = widgets))
        }

        data class ChartLogRequest(
            val deviceId: String,
            val timestamp: Long,
            val limit: Int
        )
    }
}
