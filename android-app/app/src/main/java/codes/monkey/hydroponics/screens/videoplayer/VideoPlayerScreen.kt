package codes.monkey.hydroponics.screens.videoplayer

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.exoplayer.ExoPlayer.REPEAT_MODE_OFF
import androidx.media3.ui.PlayerView
import androidx.navigation.NavHostController

@Composable
fun VideoPlayerScreen(
    navController: NavHostController,
    url: String?
) {
    val context = LocalContext.current
    var playWhenReady by remember { mutableStateOf(true) }
    val exoPlayer = remember {
        Builder(context).build().apply {
//            setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            setMediaItem(MediaItem.fromUri(url!!))
            repeatMode = REPEAT_MODE_OFF
            playWhenReady = playWhenReady
            prepare()
            play()
        }
    }
    DisposableEffect(
        AndroidView(
            modifier = Modifier,
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = true
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            }
        )
    ) {
        onDispose {
            exoPlayer.release()
        }
    }

}