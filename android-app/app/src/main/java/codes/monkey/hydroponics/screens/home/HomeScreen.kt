package codes.monkey.hydroponics.screens.home

import android.content.res.Configuration
import android.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.components.Constants
import codes.monkey.hydroponics.components.Constants.CORNER_SIZE
import codes.monkey.hydroponics.components.Constants.GUTTER_PADDING
import codes.monkey.hydroponics.components.ScreenScaffoldContainer
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.network.Camera
import codes.monkey.hydroponics.ui.theme.HydroponicsAppTheme
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


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    backgroundColor = -14935265
)
@Composable
fun DarkCameraList() {
    HydroponicsAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScreenScaffoldContainer(
                    title = "Timelapse Videos") {
                    CameraList()
                }
            }
        }

    }
}

@Composable
fun CameraList(
    loading: Boolean = false,
    cameraList: List<Camera> = listOf(createCamera(id = "video1"), createCamera(id = "video2")),
    onClick: CameraClickHandler = {}
) {
    val x = Color.parseColor("#1c1b1f")
    println(x)
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(GUTTER_PADDING.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val indicatorHeight = (Constants.GUTTER_WIDTH * 2).dp
        if (loading) {
            item {
                CircularProgressIndicator(modifier = Modifier.size(indicatorHeight))
            }
        } else {
            item {
                Box(modifier = Modifier.size(indicatorHeight)) { }
            }

        }

        items(cameraList) {
            CameraCard(camera = it, onClick = onClick)
        }
    }
}

@Composable
fun CameraCard(camera: Camera, onClick: CameraClickHandler) {
    val cardHeight = 80
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(GUTTER_PADDING.dp)
            .height(cardHeight.dp)
            .clickable { onClick(camera) },
        shape = RoundedCornerShape(corner = CornerSize(CORNER_SIZE.dp))
    ) {
        Row(modifier =
        Modifier
            .padding(GUTTER_PADDING.dp)
            .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(modifier = Modifier
                .padding(GUTTER_PADDING.dp)
                .size((cardHeight * 0.8).dp),
                shape = RectangleShape,
                shadowElevation = Constants.ELEVATION.dp
            ){
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play video"
                )
            }

            Column(modifier = Modifier.padding(GUTTER_PADDING.dp)) {
                Text(text = "camera: ${camera.id}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "device: ${camera.deviceId}", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

