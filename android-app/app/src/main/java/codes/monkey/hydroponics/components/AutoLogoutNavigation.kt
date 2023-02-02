package codes.monkey.hydroponics.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.screens.AuthViewModel

@Composable
fun AutoLogoutNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(), composable: @Composable () -> Unit
) {
    LaunchedEffect(key1 = LocalContext.current) {
        authViewModel.loggedIn.collect { loggedIn ->
            if (!loggedIn) {
                navController.navigate(AppScreens.LoginScreen.name)
            }
        }
    }
    composable()
}