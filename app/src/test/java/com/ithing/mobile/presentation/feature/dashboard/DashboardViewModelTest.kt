package com.ithing.mobile.presentation.feature.dashboard

import com.ithing.mobile.core.session.SessionManager
import com.ithing.mobile.domain.model.Customer
import com.ithing.mobile.domain.model.DashboardWidget
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.Industry
import com.ithing.mobile.domain.model.Oem
import com.ithing.mobile.domain.repository.DashboardRepository
import com.ithing.mobile.domain.usecase.LogoutUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
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
    fun `loadFilters populates all filter lists`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.industries.size)
        assertTrue(state.oems.isEmpty())
        assertTrue(state.customers.isEmpty())
        assertTrue(state.devices.isEmpty())
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

        viewModel.onDeviceSelected(state.devices.first())

        viewModel.refreshDashboard()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals(listOf("Throughput"), state.widgets.map { it.title })
        assertEquals("customer-b", dashboardRepository.lastRequestedCustomerId)
        assertEquals("device-c", dashboardRepository.lastRequestedDeviceId)
    }

    @Test
    fun `refresh without customer shows validation error`() = runTest {
        val viewModel = DashboardViewModel(
            logoutUseCase = logoutUseCase,
            dashboardRepository = dashboardRepository,
            sessionManager = sessionManager
        )
        advanceUntilIdle()

        viewModel.refreshDashboard()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.widgets.isEmpty())
        assertEquals("Select a customer and device to load widgets", viewModel.uiState.value.errorMessage)
    }

    private class FakeDashboardRepository : DashboardRepository {

        var lastRequestedCustomerId: String? = null
        var lastRequestedDeviceId: String? = null

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

            lastRequestedCustomerId = customerId
            lastRequestedDeviceId = deviceId

            return Result.success(fakeWidgets)
        }
    }
}
