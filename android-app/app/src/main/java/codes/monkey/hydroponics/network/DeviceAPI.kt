package codes.monkey.hydroponics.network

import retrofit2.Response
import retrofit2.http.GET
import javax.inject.Singleton

data class HelloResponse(val message: String)

@Singleton
interface DeviceAPI {

    @GET("/hello")
    suspend fun hello(): Response<HelloResponse>

}