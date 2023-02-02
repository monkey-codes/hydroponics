package codes.monkey.hydroponics.screens.home

import android.util.Log
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.navigation.AppScreens
import codes.monkey.hydroponics.screens.login.TokenViewModel

@Composable
fun HomeScreen(navController: NavHostController,
               tokenViewModel: TokenViewModel = hiltViewModel()
) {
//Text(text = "HomeScreen")


//    LaunchedEffect(key1 = LocalContext.current) {
//        tokenViewModel.logoutEvent.collect {
//            if(it) {
//                navController.navigate(AppScreens.LoginScreen.name)
//            }
//        }
//    }

    Button(onClick = {
        tokenViewModel.deleteTokens()
    }) {
        Text(text = "logout")
    }
}