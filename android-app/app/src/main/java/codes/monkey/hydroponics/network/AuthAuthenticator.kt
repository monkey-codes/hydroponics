package codes.monkey.hydroponics.network

import codes.monkey.hydroponics.repository.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenManager: TokenManager,
    private val authAPI: AuthAPI
) : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        val refreshToken = runBlocking {
            tokenManager.refreshToken.getToken().first()
        }
        return runBlocking {
            if(refreshToken == null) {
                tokenManager.deleteTokens()
                return@runBlocking null
            }
            val newTokenResponse = getNewToken(refreshToken)

            if (!newTokenResponse.isSuccessful || newTokenResponse.body() == null) {
                //Couldn't refresh the token, so restart the login process
                tokenManager.deleteTokens()
            }

            newTokenResponse.body()?.let {
                val accessToken = it.authenticationResult.accessToken
                tokenManager.updateAccessToken(accessToken)
                response.request.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            }
        }
    }

    private suspend fun getNewToken(refreshToken: String): retrofit2.Response<RefreshTokenResult> {
        return authAPI.refreshToken(
            RefreshTokenRequest(
                RefreshTokenRequest.AuthParameters(refreshToken = refreshToken)
            )
        )
    }
}