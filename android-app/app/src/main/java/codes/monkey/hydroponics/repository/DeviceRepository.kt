package codes.monkey.hydroponics.repository

import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Device
import codes.monkey.hydroponics.network.DeviceAPI
import codes.monkey.hydroponics.network.SensorResponse
import codes.monkey.hydroponics.utils.apiRequestFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val deviceAPI: DeviceAPI
) {

    fun devices(): Flow<ApiResponse<List<Device>>> = apiRequestFlow {
        deviceAPI.devices()
    }

    fun cameras(deviceId: String) = apiRequestFlow {
        deviceAPI.cameras(deviceId = deviceId)
    }

    fun sensorData(
        deviceId: String,
        aggFn: String,
        measureName: String,
        binTime: String,
        since: String
    ): Flow<ApiResponse<SensorResponse>> = apiRequestFlow {
        deviceAPI.sensorData(
            deviceId = deviceId,
            aggFn = aggFn,
            measureName = measureName,
            binTime = binTime,
            since = since
        )
    }
}