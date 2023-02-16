package codes.monkey.hydroponics.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import codes.monkey.hydroponics.ui.theme.HydroponicsAppTheme

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode",
    backgroundColor = -14935265
)
annotation class DarkPreview()

@Composable
fun PreviewSupport(
    title: String = "Preview Support",
    content: @Composable () -> Unit = {}
) {
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
                    title = title) {
                   content()
                }
            }
        }
    }
}