package codes.monkey.hydroponics.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.network.TimelapseDownloadInfo
import codes.monkey.hydroponics.repository.LatestTimelapseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel  @Inject constructor(
    private val latestTimelapseRepository: LatestTimelapseRepository
) : ViewModel() {

    private val _cameras = MutableStateFlow<ApiResponse<List<Camera>>?>(null)
    private val _timelapseDownloadInfo = MutableStateFlow<ApiResponse<TimelapseDownloadInfo>?>(null)

    val loading = merge( _cameras, _timelapseDownloadInfo).map { it is ApiResponse.Loading }
    val cameraList = _cameras
        .filter { it is ApiResponse.Success }
        .map { (it as ApiResponse.Success).data }

    val downloadInfo = _timelapseDownloadInfo
        .filter { it is ApiResponse.Success }
        .map { (it as ApiResponse.Success).data }
        .distinctUntilChanged()
        .onEach {
            _timelapseDownloadInfo.emit(null)
        }

    init {
        viewModelScope.launch { listTimelapse() }
    }

    fun listTimelapse() =
        viewModelScope.launch {
            latestTimelapseRepository.cameras().collect {
                _cameras.emit(it)
            }
        }

    fun onCameraSelected(camera: Camera) {
        viewModelScope.launch {
            latestTimelapseRepository.requestDownloadUrl(camera).collect {
                _timelapseDownloadInfo.emit(it)
            }
        }
    }
}