package codes.monkey.hydroponics.network

import com.google.gson.annotations.SerializedName
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import javax.inject.Singleton

data class RefreshTokenRequest(
    @SerializedName("AuthParameters")
    val authParameters: AuthParameters,
    @SerializedName("AuthFlow")
    val authFlow: String = "REFRESH_TOKEN_AUTH",
    @SerializedName("ClientId")
    val clientId: String = "6rqo7etej5hgt985p6m6h8ekpv"
) {
    data class AuthParameters(
        @SerializedName("REFRESH_TOKEN")
        val refreshToken: String
    )
}

data class RefreshTokenResult(
    @SerializedName("AuthenticationResult")
    val authenticationResult: AuthenticationResult,
    @SerializedName("ChallengeParameters")
    val challengeParameters: MutableMap<String, String>
) {
    data class AuthenticationResult(
        @SerializedName("AccessToken")
        val accessToken: String,
        @SerializedName("ExpiresIn")
        val expiresIn: Int,
        @SerializedName("IdToken")
        val idToken: String,
        @SerializedName("TokenType")
        val tokenType: String,
    )
}

data class LoginRequest(
    @SerializedName("AuthParameters")
    val authParameters: AuthParameters,
    @SerializedName("AuthFlow")
    val authFlow: String = "USER_PASSWORD_AUTH",
    @SerializedName("ClientId")
    val clientId: String = "6rqo7etej5hgt985p6m6h8ekpv"
) {
    data class AuthParameters(
        @SerializedName("USERNAME")
        val username: String,
        @SerializedName("PASSWORD")
        val password: String
    )
}

data class LoginResult(
    @SerializedName("AuthenticationResult")
    val authenticationResult: AuthenticationResult,
    @SerializedName("ChallengeParameters")
    val challengeParameters: MutableMap<String, String>
) {
    data class AuthenticationResult(
        @SerializedName("AccessToken")
        val accessToken: String,
        @SerializedName("ExpiresIn")
        val expiresIn: Int,
        @SerializedName("IdToken")
        val idToken: String,
        @SerializedName("RefreshToken")
        val refreshToken: String,
        @SerializedName("TokenType")
        val tokenType: String,
    )
}

@Singleton
interface AuthAPI {

    @Headers(
        "Content-Type: application/x-amz-json-1.1",
        "X-Amz-Target: AWSCognitoIdentityProviderService.InitiateAuth"
    )
    @POST("/")
    suspend fun login(@Body loginRequest: LoginRequest): Response<LoginResult>

    @Headers(
        "Content-Type: application/x-amz-json-1.1",
        "X-Amz-Target: AWSCognitoIdentityProviderService.InitiateAuth"
    )
    @POST("/")
    suspend fun refreshToken(@Body loginRequest: RefreshTokenRequest): Response<RefreshTokenResult>
}