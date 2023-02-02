package codes.monkey.hydroponics.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.screens.AuthViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {

    Column(modifier = Modifier.fillMaxSize()) {
        Text(text = homeScreenViewModel.helloState.value)
        Button(onClick = {
            authViewModel.logout()
        }) {
            Text(text = "logout")
        }
    }

}