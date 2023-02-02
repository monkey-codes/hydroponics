package codes.monkey.hydroponics.repository

import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.AuthAPI
import codes.monkey.hydroponics.network.LoginRequest
import codes.monkey.hydroponics.network.LoginResult
import codes.monkey.hydroponics.utils.apiRequestFlow
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

class AuthenticationRepository @Inject constructor(
    private val authApi: AuthAPI,
    private val tokenManager: TokenManager
) {

    val loggedIn = tokenManager.loggedIn
    fun login(email: String, password: String) = apiRequestFlow {
        val response = authApi.login(
            LoginRequest(
                authParameters = LoginRequest.AuthParameters(
                    username = email,
                    password = password
                )
            )
        )
        response
    }.onEach {
        if (it is ApiResponse.Success) {
            saveTokens(it.data)
        }
    }

    private suspend fun saveTokens(body: LoginResult) {
        tokenManager.updateTokens(body.authenticationResult)
    }

    suspend fun logout() {
        tokenManager.deleteTokens()
    }

}