package codes.monkey.hydroponics.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.LoginResult
import codes.monkey.hydroponics.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<ApiResponse<LoginResult>?>(null)
    val loading = _authState.map { it is ApiResponse.Loading }
    val authState = _authState.asStateFlow()
    fun login(email: String, password: String, successCallback: () -> Unit = {}) =
        viewModelScope.launch {
            authenticationRepository.login(email, password).collect {
                _authState.emit(it)
                if (it is ApiResponse.Success) successCallback()
            }
        }
}