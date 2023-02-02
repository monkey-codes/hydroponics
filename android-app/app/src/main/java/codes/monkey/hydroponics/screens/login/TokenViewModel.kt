package codes.monkey.hydroponics.screens.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.network.LoginResult
import codes.monkey.hydroponics.repository.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenManager: TokenManager,
): ViewModel()  {
    val initialized = mutableStateOf(false)
    val accessToken = mutableStateOf<String?>(null)
    val refreshToken = mutableStateOf<String?>(null)
    val logoutEvent = tokenManager.logoutEvent

    init {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.getAccessToken().collect {
                withContext(Dispatchers.Main) {
                    accessToken.value = it
                    initialized.value = true
                }
            }
        }
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.getRefreshToken().collect {
                withContext(Dispatchers.Main) {
                    refreshToken.value = it
                }
            }
        }
    }

    fun saveTokens(authResult: LoginResult.AuthenticationResult) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.saveAccessToken(authResult.accessToken)
            tokenManager.saveRefreshToken(authResult.refreshToken)
        }
    }

    fun deleteTokens() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenManager.logout()
//            tokenManager.deleteRefreshToken()
//            tokenManager.deleteAccessToken()
//            withContext(Dispatchers.Main) {
//                logoutEvent.emit(true)
//            }
        }
    }

}