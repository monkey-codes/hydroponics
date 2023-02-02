package codes.monkey.hydroponics.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import codes.monkey.hydroponics.components.AutoLogoutNavigation
import codes.monkey.hydroponics.screens.SplashScreen
import codes.monkey.hydroponics.screens.home.HomeScreen
import codes.monkey.hydroponics.screens.login.LoginScreen

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
                HomeScreen(navController = navController)
            }
        }
    }

}