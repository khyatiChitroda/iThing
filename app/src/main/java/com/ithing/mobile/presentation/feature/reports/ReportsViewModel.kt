package com.ithing.mobile.presentation.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ithing.mobile.domain.model.Device
import com.ithing.mobile.domain.model.DeviceOwnerDetails
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
    val devices: List<Device> = emptyList(),
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

    init {
        loadReports()
    }

    fun loadReports() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val devicesResult = dashboardRepository.getDevices()
            val devices = devicesResult.getOrDefault(emptyList())
            val selectedDevice = _uiState.value.selectedDevice ?: devices.firstOrNull()

            _uiState.update {
                it.copy(
                    devices = devices,
                    selectedDevice = selectedDevice
                )
            }

            if (selectedDevice == null) {
                _uiState.update {
                    it.copy(
                        schedules = emptyList(),
                        deviceOwnerDetails = null,
                        isLoading = false,
                        errorMessage = devicesResult.exceptionOrNull()?.message
                    )
                }
                return@launch
            }

            loadSelectedDevice(selectedDevice.id, false)
        }
    }

    fun onDeviceSelected(device: Device?) {
        _uiState.update {
            it.copy(
                selectedDevice = device,
                errorMessage = null
            )
        }

        if (device == null) {
            _uiState.update {
                it.copy(
                    schedules = emptyList(),
                    deviceOwnerDetails = null
                )
            }
            return
        }

        viewModelScope.launch {
            loadSelectedDevice(device.id, true)
        }
    }

    fun refresh() {
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
}
