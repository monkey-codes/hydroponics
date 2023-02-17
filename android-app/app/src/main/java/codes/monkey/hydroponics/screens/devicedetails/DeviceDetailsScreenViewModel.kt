package codes.monkey.hydroponics.screens.devicedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import codes.monkey.hydroponics.model.ApiResponse
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.network.SensorResponse
import codes.monkey.hydroponics.network.TimelapseDownloadInfo
import codes.monkey.hydroponics.repository.DeviceRepository
import codes.monkey.hydroponics.repository.LatestTimelapseRepository
import com.himanshoe.charty.line.model.LineData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class DeviceDetailsScreenViewModel @Inject constructor(
    private val deviceRepository: DeviceRepository,
    private val latestTimelapseRepository: LatestTimelapseRepository
) : ViewModel() {
    private val _sensorCpuPercent = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorCpuTemp = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorDiskUsagePercent = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorDiskUsageFree = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorDiskUsageUsed = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorLoadAvg1 = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorLoadAvg5 = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorLoadAvg15 = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _sensorMemPercent = MutableStateFlow<ApiResponse<SensorResponse>?>(null)
    private val _cameras = MutableStateFlow<ApiResponse<List<Camera>>?>(null)
    val cameras = _cameras
        .filter { it is ApiResponse.Success }
        .map { (it as ApiResponse.Success).data  }
    private val _timelapseDownloadInfo = MutableStateFlow<ApiResponse<TimelapseDownloadInfo>?>(null)
    val downloadInfo = _timelapseDownloadInfo
        .filter { it is ApiResponse.Success }
        .map { (it as ApiResponse.Success).data }
        .distinctUntilChanged()

    val loading = merge(
        _sensorCpuPercent,
        _sensorCpuTemp,
        _sensorDiskUsagePercent,
        _sensorDiskUsageFree,
        _sensorDiskUsageUsed,
        _sensorLoadAvg1,
        _sensorLoadAvg5,
        _sensorLoadAvg15,
        _sensorMemPercent,
        _cameras
    ).map { it is ApiResponse.Loading }

    val sensorCpuPercent = transform(_sensorCpuPercent)
    val sensorCpuTemp = transform( _sensorCpuTemp)
    val sensorDiskUsagePercent = transform( _sensorDiskUsagePercent)
    val sensorDiskUsageFree = transform( _sensorDiskUsageFree)
    val sensorDiskUsageUsed = transform( _sensorDiskUsageUsed)
    val sensorLoadAvg1 = transform( _sensorLoadAvg1)
    val sensorLoadAvg5 = transform( _sensorLoadAvg5)
    val sensorLoadAvg15 = transform( _sensorLoadAvg15)
    val sensorMemPercent = transform( _sensorMemPercent)
    private val _selectedTimeFrame = MutableStateFlow(Pair("120m", "1d"))
    val selectedTimeFrame = _selectedTimeFrame.asStateFlow()

    private fun transform(input: MutableStateFlow<ApiResponse<SensorResponse>?>): Flow<List<LineData>> {
        return input.filter { it is ApiResponse.Success }
            .map { (it as ApiResponse.Success).data.data }
            .map {
                val formatter =
                    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                it.map {
                    LineData(
                        xValue = Instant.ofEpochMilli(it.ts).let { formatter.format(it) },
                        yValue = it.v.toFloat()
                    )
                }
            }
    }

    fun cameras(deviceId: String) = viewModelScope.launch {
        deviceRepository.cameras(deviceId = deviceId).collect {
            _cameras.emit(it)
        }
    }
    fun loadSensorData(deviceId: String, binTime: String = "120m", since: String = "1d") =
        viewModelScope.launch {
            listOf(
                "cpu-percent" to _sensorCpuPercent,
                "cpu-temp" to _sensorCpuTemp,
                "disk-usage-percent" to _sensorDiskUsagePercent,
//                "disk-usage-free-mb" to _sensorDiskUsageFree,
//                "disk-usage-used-mb" to _sensorDiskUsageUsed,
//                "load-avg-1" to _sensorLoadAvg1,
//                "load-avg-5" to _sensorLoadAvg5,
//                "load-avg-15" to _sensorLoadAvg15,
                "mem-percent" to _sensorMemPercent,
            ).forEach {(measureName, targetFlow) ->
                deviceRepository.sensorData(
                    deviceId = deviceId,
                    aggFn = "avg",
                    measureName = measureName,
                    binTime = binTime,
                    since = since
                ).collect {
                    targetFlow.emit(it)
                    _selectedTimeFrame.emit(binTime to since)
                }
            }
        }

    fun onCameraSelected(camera: Camera) {
        viewModelScope.launch {
            latestTimelapseRepository.requestDownloadUrl(camera).collect {
                _timelapseDownloadInfo.emit(it)
            }
        }
    }

    fun onDownloadInfoConsumed() {
        viewModelScope.launch {
            _timelapseDownloadInfo.emit(null)
        }
    }
}
