package codes.monkey.hydroponics.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Device
import codes.monkey.hydroponics.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesScreenViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository
) : ViewModel() {

    private val _devices = MutableStateFlow<ApiResponse<List<Device>>?>(null)
    val loading = _devices.map { it is ApiResponse.Loading }
    val deviceList = _devices
        .filter { it is ApiResponse.Success }
        .map { (it as ApiResponse.Success).data }

    fun listDevices() =
        viewModelScope.launch {
            deviceRepository.devices().collect {
                _devices.emit(it)
            }
        }

    init {
        viewModelScope.launch { listDevices() }
    }

}