package codes.monkey.hydroponics.screens.login

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.network.LoginResult
import codes.monkey.hydroponics.repository.AuthenticationRepository
import codes.monkey.hydroponics.repository.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
//    private val _loading =  MutableLiveData(false)
//    val loading: LiveData<Boolean> = _loading
    val authState = mutableStateOf(authenticationRepository.loginResponse)
    val loading = mutableStateOf(false)

    // var authState by mutableStateOf(Response<LoginResult>())
//        private set

    fun login(email: String, password: String,successCallback: (authState: MutableState<Response<LoginResult>>) -> Unit = {}) = viewModelScope.launch {
        loading.value = true
        authState.value = authenticationRepository.login(email, password)
        loading.value = false
        if(authState.value.result?.authenticationResult?.accessToken != null) {
            successCallback(authState)
        }
    }
}