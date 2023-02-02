package codes.monkey.hydroponics.repository

import android.util.Log
import codes.monkey.hydroponics.network.AuthAPI
import codes.monkey.hydroponics.network.LoginRequest
import codes.monkey.hydroponics.network.LoginResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import javax.inject.Inject
data class Response<T>(
    var result: T? = null,
    var loading: Boolean = false,
    var exception: Exception? = null
)
class AuthenticationRepository @Inject constructor(private val authApi: AuthAPI,
private val tokenManager: TokenManager) {

    val loginResponse: Response<LoginResult> = Response()

    suspend fun login(email: String, password: String): Response<LoginResult> {
        try {
            loginResponse.loading = true
//            tokenManager.getToken().collect {
//                Log.i("TOKEN", it ?: "NA")
//            }
//            delay(5000)
            loginResponse.result = authApi.login(LoginRequest(
                authParameters = LoginRequest.AuthParameters(
                    username = email,
                    password = password
                )
            ))
//            loginResponse.result?.let {
//                tokenManager.saveToken(it.authenticationResult.accessToken)
//            }
            loginResponse.loading = false
        } catch (e: Exception) {
            loginResponse.exception = e
            loginResponse.loading = false
        }
        return loginResponse
    }

}