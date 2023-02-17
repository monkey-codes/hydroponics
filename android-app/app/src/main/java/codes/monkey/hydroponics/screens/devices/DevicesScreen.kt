package codes.monkey.hydroponics.screens.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.components.*
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.network.Device
import codes.monkey.hydroponics.ui.theme.HydroponicsAppTheme
import codes.monkey.hydroponics.utils.TestDataFactory.createDevice


typealias DeviceClickHandler = (Device) -> Unit

@Composable
fun DevicesScreen(
    navController: NavHostController,
    devicesScreenViewModel: DevicesScreenViewModel = hiltViewModel()
) {

    val deviceList = devicesScreenViewModel.deviceList.collectAsState(initial = emptyList())
    val loading = devicesScreenViewModel.loading.collectAsState(initial = false)

    DeviceList(loading.value, deviceList.value, onClick = {
        navController.navigate(AppScreens.DeviceDetailsScreen.name+ "/${it.id}")
    })

}

@Composable
fun DeviceList(
    loading: Boolean = false,
    devices: List<Device> = listOf(
        createDevice(id = "b8:27:eb:66:03:0b"),
        createDevice(id = "c8:b7:ab:66:03:0b")
    ),
    onClick: (Device) -> Unit = {}
) {
    ListView(loading = loading, values = devices) {
        DeviceCard(device = it, onClick = onClick)
    }
}

@Composable
fun DeviceCard(device: Device, onClick: DeviceClickHandler) {
    IconCard(cardHeight = 80, item = device, onClick = onClick, icon = {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Play video",
            tint = MaterialTheme.colorScheme.primary
        )
    }) {
        LabelledData(
            data = listOf(
                "device" to device.id
            )
        )
    }
}

@DarkPreview
@Composable
fun DarkDevicesList() {
    PreviewSupport(
        title = "Devices"
    ) {
        DeviceList()
    }
}
