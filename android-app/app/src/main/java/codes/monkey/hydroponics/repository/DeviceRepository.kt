package codes.monkey.hydroponics.repository

import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Device
import codes.monkey.hydroponics.network.DeviceAPI
import codes.monkey.hydroponics.utils.apiRequestFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeviceRepository@Inject constructor(
    private val deviceAPI: DeviceAPI
) {

    fun devices(): Flow<ApiResponse<List<Device>>> = apiRequestFlow {
        deviceAPI.devices()
    }
}