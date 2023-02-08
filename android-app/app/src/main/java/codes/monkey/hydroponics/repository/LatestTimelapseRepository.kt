package codes.monkey.hydroponics.repository

import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.network.DeviceAPI
import codes.monkey.hydroponics.utils.apiRequestFlow
import kotlinx.coroutines.flow.Flow
import retrofit2.Response
import javax.inject.Inject

class LatestTimelapseRepository @Inject constructor(
    private val deviceAPI: DeviceAPI
) {

    fun cameras(): Flow<ApiResponse<List<Camera>>> = apiRequestFlow {
        deviceAPI.devices()
            .let {
                if (!it.isSuccessful) return@apiRequestFlow Response.error(
                    it.code(),
                    it.errorBody()!!
                )

                val y = it.body()!!.flatMap { device ->
                    deviceAPI.cameras(device.id)
                        .let { listCamerasResponse ->
                            if (!listCamerasResponse.isSuccessful) return@apiRequestFlow Response.error(
                                listCamerasResponse.code(),
                                listCamerasResponse.errorBody()!!
                            )
                            listCamerasResponse.body()!!
                        }

                }
                Response.success(y)
            }
    }

    fun requestDownloadUrl(camera: Camera) = apiRequestFlow {
        deviceAPI.requestLatestTimelapseDownloadUrl(deviceId = camera.deviceId, cameraId = camera.id)
    }


}