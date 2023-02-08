package codes.monkey.hydroponics.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
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

            navController.navigate(AppScreens.VideoPlayerScreen.name+"/${URLEncoder.encode(downloadInfo.timelapseDownloadUrl, "utf-8")}")

        }
    }
    CameraList(loading.value, cameraList.value, onClick = homeScreenViewModel::onCameraSelected)

}


@Preview(showBackground = true)
@Composable
fun CameraList(
    loading: Boolean = false,
    cameraList: List<Camera> = listOf(createCamera(id = "video1"), createCamera(id = "video2")),
    onClick: CameraClickHandler = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (loading) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(25.dp))
            }
        }
        items(cameraList) {
            CameraCard(camera = it, onClick = onClick)
        }
    }
}

@Composable
fun CameraCard(camera: Camera, onClick: CameraClickHandler) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick(camera) }
    ) {
        Row() {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play video")
            Column() {
                Text(text = "id: ${camera.id}")
                Text(text = "device: ${camera.deviceId}")
            }
        }
    }
}

