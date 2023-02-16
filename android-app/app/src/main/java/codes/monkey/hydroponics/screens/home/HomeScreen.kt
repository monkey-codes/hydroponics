package codes.monkey.hydroponics.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.components.*
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.utils.TestDataFactory.createCamera
import java.net.URLEncoder

typealias CameraClickHandler = (Camera) -> Unit

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {

    val cameraList = homeScreenViewModel.cameraList.collectAsState(initial = emptyList())
    val loading = homeScreenViewModel.loading.collectAsState(initial = false)
    val key1 = LocalContext.current
    LaunchedEffect(key1 = key1) {
        homeScreenViewModel.downloadInfo.collect { downloadInfo ->

            navController.navigate(
                AppScreens.VideoPlayerScreen.name + "/${
                    URLEncoder.encode(
                        downloadInfo.timelapseDownloadUrl,
                        "utf-8"
                    )
                }"
            )

        }
    }
    CameraList(loading.value, cameraList.value, onClick = homeScreenViewModel::onCameraSelected)

}

@Composable
fun CameraList(
    loading: Boolean = false,
    cameraList: List<Camera> = listOf(createCamera(id = "video1"), createCamera(id = "video2")),
    onClick: CameraClickHandler = {}
) {
    ListView(loading = loading, values = cameraList) {
        CameraCard(camera = it, onClick = onClick)
    }
}


@Composable
fun CameraCard(camera: Camera, onClick: CameraClickHandler) {
    IconCard(cardHeight = 80, item = camera, onClick = onClick, icon = {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = "Play video",
            tint = MaterialTheme.colorScheme.primary
        )
    }) {
        LabelledData(
            data = listOf(
                "camera" to camera.id,
                "device" to camera.deviceId
            )
        )
    }
}

@DarkPreview
@Composable
fun DarkCameraList() {
    PreviewSupport(
        title = "Timelapse Videos"
    ) {
        CameraList()
    }
}

