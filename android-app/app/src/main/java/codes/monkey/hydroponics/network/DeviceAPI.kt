package codes.monkey.hydroponics.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import javax.inject.Singleton

data class HelloResponse(val message: String)

data class Device(val id: String, val created: Long, val pk: String, val sk: String)
data class Camera(val id: String, val deviceId: String, val created: Long, val pk: String, val sk: String)

data class TimelapseDownloadInfo(val deviceId: String, val cameraId: String, val timelapseDownloadUrl: String)
@Singleton
interface DeviceAPI {

    @GET("/hello")
    suspend fun hello(): Response<HelloResponse>

    @GET("/devices")
    suspend fun devices(): Response<List<Device>>

    @GET("/devices/{deviceId}/cameras")
    suspend fun cameras(@Path("deviceId") deviceId: String): Response<List<Camera>>

    @POST("/devices/{deviceId}/cameras/{cameraId}/latest-timelapse-download-request")
    suspend fun requestLatestTimelapseDownloadUrl(@Path("deviceId") deviceId: String,
                                                  @Path("cameraId") cameraId: String): Response<TimelapseDownloadInfo>
}