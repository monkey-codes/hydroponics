package codes.monkey.hydroponics.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import codes.monkey.hydroponics.di.dataStore
import codes.monkey.hydroponics.network.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    val accessToken = Token(context, ACCESS_TOKEN_KEY)
    val refreshToken = Token(context, REFRESH_TOKEN_KEY)
    val loggedIn = accessToken.getToken().map { it != null }

    suspend fun updateTokens(authResult: LoginResult.AuthenticationResult) {
        accessToken.saveToken(authResult.accessToken)
        refreshToken.saveToken(authResult.refreshToken)
    }

    suspend fun updateAccessToken(accessToken: String) {
        this.accessToken.saveToken(accessToken)
    }

    suspend fun deleteTokens() {
        accessToken.deleteToken()
        refreshToken.deleteToken()
    }

    class Token(private val context: Context, private val key: Preferences.Key<String>) {

        fun getToken(): Flow<String?> {
            return context.dataStore.data.map { preferences ->
                preferences[key]
            }
        }

        suspend fun saveToken(token: String) {
            context.dataStore.edit { preferences ->
                preferences[key] = token
            }
        }

        suspend fun deleteToken() {
            context.dataStore.edit { preferences ->
                preferences.remove(key)
            }
        }
    }
}
