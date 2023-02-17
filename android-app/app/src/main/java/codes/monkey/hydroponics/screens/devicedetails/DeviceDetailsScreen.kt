package codes.monkey.hydroponics.screens.devicedetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.components.*
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.screens.home.CameraList
import codes.monkey.hydroponics.utils.TestDataFactory
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.line.LineChart
import com.himanshoe.charty.line.model.LineData
import java.net.URLEncoder

@Composable
fun DeviceDetailsScreen(
    navController: NavHostController,
    deviceId: String,
    deviceDetailsScreenViewModel: DeviceDetailsScreenViewModel = hiltViewModel(),
    screenScaffoldViewModel: ScreenScaffoldViewModel = viewModel()
) {

    LaunchedEffect(key1 = deviceId) {
        deviceDetailsScreenViewModel.loadSensorData(deviceId = deviceId)
        deviceDetailsScreenViewModel.cameras(deviceId = deviceId)
        deviceDetailsScreenViewModel.downloadInfo.collect { downloadInfo ->
            navController.navigate(
                AppScreens.VideoPlayerScreen.name + "/${
                    URLEncoder.encode(
                        downloadInfo.timelapseDownloadUrl,
                        "utf-8"
                    )
                }"
            )
            deviceDetailsScreenViewModel.onDownloadInfoConsumed()
        }
    }
    val loading = deviceDetailsScreenViewModel.loading.collectAsState(initial = true)
    val cpuPercent =
        deviceDetailsScreenViewModel.sensorCpuPercent.collectAsState(initial = emptyList())
    val cpuTemp = deviceDetailsScreenViewModel.sensorCpuTemp.collectAsState(initial = emptyList())
    val diskUsagePercent = deviceDetailsScreenViewModel.sensorDiskUsagePercent.collectAsState(
        initial = emptyList()
    )
//    val diskUsageFree =
//        deviceDetailsScreenViewModel.sensorDiskUsageFree.collectAsState(initial = emptyList())
//    val diskUsageUsed =
//        deviceDetailsScreenViewModel.sensorDiskUsageUsed.collectAsState(initial = emptyList())
//    val loadAvg1 = deviceDetailsScreenViewModel.sensorLoadAvg1.collectAsState(initial = emptyList())
//    val loadAvg5 = deviceDetailsScreenViewModel.sensorLoadAvg5.collectAsState(initial = emptyList())
//    val loadAvg15 =
//        deviceDetailsScreenViewModel.sensorLoadAvg15.collectAsState(initial = emptyList())
    val memPercent =
        deviceDetailsScreenViewModel.sensorMemPercent.collectAsState(initial = emptyList())
    val selectedTimeFrame = deviceDetailsScreenViewModel.selectedTimeFrame.collectAsState()
    val cameras = deviceDetailsScreenViewModel.cameras.collectAsState(initial = emptyList())
    DeviceDetails(
        loading = loading.value, sensorData = listOf(
            "CPU Percent" to cpuPercent.value,
            "CPU Temp" to cpuTemp.value,
            "Memory Percent" to memPercent.value,
            "Disk Usage Percent" to diskUsagePercent.value,
//            "Disk Usage Free (MB)" to diskUsageFree.value,
//            "Disk Usage Used (MB)" to diskUsageUsed.value,
//            "Load Avg 1" to loadAvg1.value,
//            "Load Avg 5" to loadAvg5.value,
//            "Load Avg 15" to loadAvg15.value,
        ),
        updateBottomBar = { screenScaffoldViewModel.updateBottomBar(it) },
        changeTimeFrame = { binTime, since ->
            deviceDetailsScreenViewModel.loadSensorData(
                deviceId,
                binTime,
                since
            )
        },
        selectedTimeFrame = selectedTimeFrame.value,
        cameras = cameras.value,
        onCameraSelected = { deviceDetailsScreenViewModel.onCameraSelected(it) }
    )
}

@Composable
fun DeviceDetails(
    loading: Boolean = false,
    sensorData: List<Pair<String, List<LineData>>>,
    updateBottomBar: (@Composable () -> Unit) -> Unit = {},
    changeTimeFrame: (String, String) -> Unit = { _, _ -> },
    selectedTimeFrame: Pair<String, String> = "120m" to "1d",
    cameras: List<Camera> = emptyList(),
    onCameraSelected: (Camera) -> Unit = {}
) {
    var selectedTab by remember {
        mutableStateOf(0)
    }
    Column {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(text = "Sensors") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(text = "Cameras") }
            )
        }
        if (selectedTab == 0) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = Constants.GUTTER_WIDTH.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                this.loadingIndicator(loading = loading)
                items(sensorData) {
                    val (measureName, data) = it
                    if (data.isNotEmpty()) {
                        SensorLineChart(
                            title = measureName,
                            data = data
                        )
                    }
                }
            }
            updateBottomBar {
                BottomAppBar {
                    Row(
                        Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val modifier = Modifier
                            .padding(horizontal = Constants.GUTTER_PADDING.dp)
                            .width(120.dp)
                        Button(
                            onClick = {
                                changeTimeFrame("120m", "1d")
                            },
                            enabled = selectedTimeFrame != "120m" to "1d",
                            modifier = modifier
                        ) {
                            Text(text = "1 day ")
                        }
                        Button(
                            onClick = {
                                changeTimeFrame("1d", "7d")
                            },
                            enabled = selectedTimeFrame != "1d" to "7d",
                            modifier = modifier
                        ) {
                            Text(text = "1 week")
                        }
                    }
                }
            }
        } else {
            CameraList(loading = loading, cameraList = cameras, onClick = onCameraSelected)
            updateBottomBar {}
        }
    }
}

@DarkPreview
@Composable
fun DarkDevicesDetails() {
    PreviewSupport(
        title = "Device Details"
    ) {
        DeviceDetails(
            loading = false, sensorData = listOf(
                "CPU Percent" to listOf(
                    LineData("10:00", 25.5f),
                    LineData("10:10", 20.5f),
                    LineData("10:20", 10.5f)
                ),
                "CPU Temp" to listOf(
                    LineData("10:00", 25.5f),
                    LineData("10:10", 20.5f),
                    LineData("10:20", 10.5f)
                ),
                "Memory Percent" to listOf(
                    LineData("10:00", 25.5f),
                    LineData("10:10", 20.5f),
                    LineData("10:20", 10.5f)
                ),
            ), cameras = listOf(TestDataFactory.createCamera())
        )
    }
}

@Composable
fun SensorLineChart(title: String = "title", data: List<LineData> = emptyList()) {
    Column(modifier = Modifier.padding(vertical = Constants.GUTTER_PADDING.dp)) {

        Text(
            text = title, modifier =
            Modifier
                .padding(vertical = Constants.GUTTER_PADDING.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.padding(vertical = Constants.GUTTER_PADDING.dp))
        LineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .padding(top = Constants.GUTTER_PADDING.dp, bottom = 32.dp),
            color = MaterialTheme.colorScheme.primary,
            axisConfig = AxisConfig(
                xAxisColor = MaterialTheme.colorScheme.onSurface,
                showAxis = true,
                isAxisDashed = false,
                showUnitLabels = true,
                showXLabels = true,
                yAxisColor = MaterialTheme.colorScheme.onSurface,
                textColor = MaterialTheme.colorScheme.onSurface
            ),
            lineData = data
        )
        Spacer(modifier = Modifier.padding(vertical = Constants.GUTTER_PADDING.dp))
    }

}
