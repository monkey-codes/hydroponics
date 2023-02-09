package codes.monkey.hydroponics.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import codes.monkey.hydroponics.components.AutoLogoutNavigation
import codes.monkey.hydroponics.components.ScreenScaffold
import codes.monkey.hydroponics.screens.SplashScreen
import codes.monkey.hydroponics.screens.home.HomeScreen
import codes.monkey.hydroponics.screens.login.LoginScreen
import codes.monkey.hydroponics.screens.videoplayer.VideoPlayerScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = AppScreens.SplashScreen.name) {
        composable(AppScreens.SplashScreen.name) {
            SplashScreen(navController = navController)
        }
        composable(AppScreens.LoginScreen.name) {
            LoginScreen(navController = navController)
        }
        composable(AppScreens.HomeScreen.name) {
            AutoLogoutNavigation(navController = navController) {
                ScreenScaffold(navController = navController, title = "Timelapse Videos") {
                    HomeScreen(navController = navController)
                }
            }
        }
        composable(AppScreens.VideoPlayerScreen.name+"/{url}") {
//            AutoLogoutNavigation(navController = navController) {
//                ScreenScaffold(navController = navController) {
                    VideoPlayerScreen(url = it.arguments?.getString("url"))
//                }
//            }
        }
    }

}