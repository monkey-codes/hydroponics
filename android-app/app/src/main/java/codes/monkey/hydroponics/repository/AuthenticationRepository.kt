package codes.monkey.hydroponics.repository

import codes.monkey.hydroponics.network.AuthAPI
import codes.monkey.hydroponics.network.LoginRequest
import codes.monkey.hydroponics.network.LoginResult
import javax.inject.Inject
data class Response<T>(
    var result: T? = null,
    var loading: Boolean = false,
    var exception: Exception? = null
)
class AuthenticationRepository @Inject constructor(private val authApi: AuthAPI) {

    private val loginResponse: Response<LoginResult> = Response()

    suspend fun login(email: String, password: String): Response<LoginResult> {
        try {
            loginResponse.loading = true

            loginResponse.result = authApi.login(LoginRequest(
                authParameters = LoginRequest.AuthParameters(
                    username = email,
                    password = password
                )
            ))
            loginResponse.loading = false
        } catch (e: Exception) {
            loginResponse.exception = e
            loginResponse.loading = false
        }
        return loginResponse
    }

}