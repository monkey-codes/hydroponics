package codes.monkey.hydroponics.screens.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) : ViewModel() {
    private val _loading =  MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    fun login(email: String, password: String,successCallback: () -> Unit = {}) = viewModelScope.launch {
        _loading.value = true
        val loginState = authenticationRepository.login(email, password)
        _loading.value = false
        if(loginState.result?.authenticationResult?.accessToken != null) {
            successCallback()
        }
    }
}