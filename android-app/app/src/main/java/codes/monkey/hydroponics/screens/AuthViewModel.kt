package codes.monkey.hydroponics.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.repository.AuthenticationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
) : ViewModel() {

    private val _loggedIn = MutableStateFlow(false)

    val loggedIn = _loggedIn.asStateFlow()

    init {
        viewModelScope.launch {
            authenticationRepository.loggedIn.collect {
                withContext(Dispatchers.Main) {
                    Log.i("LOGIN", "emitting on loggedIn $it")
                    _loggedIn.emit(it)
                }
            }
        }
    }


    fun logout() = viewModelScope.launch {
        authenticationRepository.logout()
    }
}