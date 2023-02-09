package codes.monkey.hydroponics.screens.videoplayer

import android.content.res.Configuration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer.Builder
import androidx.media3.exoplayer.ExoPlayer.REPEAT_MODE_OFF
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayerScreen(
    url: String? = "\"https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4\""
) {
//    LockScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
    val context = LocalContext.current
    var playWhenReady by remember { mutableStateOf(true) }
    val exoPlayer = remember {
        Builder(context).build().apply {
//            setMediaItem(MediaItem.fromUri("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            setMediaItem(MediaItem.fromUri(url!!))
            repeatMode = REPEAT_MODE_OFF
            this.playWhenReady = playWhenReady
            prepare()
            play()
        }
    }
    val configuration = LocalConfiguration.current
    val modifier = when(configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> Modifier
        else -> Modifier.fillMaxSize()
    }
//    val modifier = Modifier.background(Color.Red)
    DisposableEffect(
        Box(modifier = modifier) {
            AndroidView(
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
        }

    ) {
        onDispose {
            exoPlayer.release()
        }
    }

}