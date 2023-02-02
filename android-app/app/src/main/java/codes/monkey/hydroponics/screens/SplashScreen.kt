package codes.monkey.hydroponics.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.screens.login.TokenViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController,
                 tokenViewModel: TokenViewModel = hiltViewModel()
) {
    val scale = remember {
        Animatable(0f)
    }
    LaunchedEffect(key1 = true) {
        scale.animateTo(0.9f, animationSpec = tween(
            durationMillis = 800, easing = {
                OvershootInterpolator(8f).getInterpolation(it)
            }
        ))
        delay(2000L)
        if(tokenViewModel.accessToken.value == null)
            navController.navigate(AppScreens.LoginScreen.name)
        else
            navController.navigate(AppScreens.HomeScreen.name)
    }
    Surface(
        modifier = Modifier
            .padding(15.dp)
            .size(330.dp)
            .scale(scale.value),
        shape = CircleShape,
        color = Color.White,
        border = BorderStroke(
            width = 2.dp,
            color = Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier.padding(1.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
//            ReaderLogo()
            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "Hydroponics App",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

        }
    }
}
