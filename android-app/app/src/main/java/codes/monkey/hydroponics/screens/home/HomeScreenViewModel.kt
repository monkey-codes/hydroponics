package codes.monkey.hydroponics.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.network.DeviceAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel  @Inject constructor(
    private val deviceAPI: DeviceAPI
) : ViewModel() {

    val helloState = mutableStateOf("")

    init {
        viewModelScope.launch {
            val response = deviceAPI.hello()
            if(response.isSuccessful){
                helloState.value = response.body()?.message ?: ""
            }
        }
    }
}