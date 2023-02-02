package codes.monkey.hydroponics.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import codes.monkey.hydroponics.screens.SplashScreen
import codes.monkey.hydroponics.screens.home.HomeScreen
import codes.monkey.hydroponics.screens.login.LoginScreen
import codes.monkey.hydroponics.screens.login.TokenViewModel

@Composable
fun AppNavigation(
    tokenViewModel: TokenViewModel = hiltViewModel()
) {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = AppScreens.SplashScreen.name) {
        composable(AppScreens.SplashScreen.name) {
            SplashScreen(navController = navController)
        }
        composable(AppScreens.LoginScreen.name) {
            LoginScreen(navController = navController)
        }
        composable(AppScreens.HomeScreen.name) {
                HomeScreen(navController = navController)

        }
    }
    LaunchedEffect(key1 = LocalContext.current) {
        tokenViewModel.logoutEvent.collect {
            if(it) {
                navController.navigate(AppScreens.LoginScreen.name)
            }
        }
    }
}