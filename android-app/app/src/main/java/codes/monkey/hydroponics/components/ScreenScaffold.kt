package codes.monkey.hydroponics.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import codes.monkey.hydroponics.components.Constants.GUTTER_PADDING
import codes.monkey.hydroponics.screens.AuthViewModel
import codes.monkey.hydroponics.ui.theme.HydroponicsAppTheme
import kotlinx.coroutines.launch

@Composable
fun ScreenScaffold(
    navController: NavHostController = NavHostController(LocalContext.current),
    title: String = "Hydroponics App",
    authViewModel: AuthViewModel = hiltViewModel(),
    content: @Composable () -> Unit = {}
) {
    ScreenScaffoldContainer(title, authViewModel::logout, content)

}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark Mode Scaffold",
    backgroundColor = -14935265
)
@Composable
fun ScreenScaffoldPreivew() {
    HydroponicsAppTheme {
        ScreenScaffoldContainer()
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ScreenScaffoldContainer(
    title: String = "Hydroponics App",
    logout: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val items = listOf(Icons.Default.ExitToApp)
    val selectedItem = remember { mutableStateOf(items[0]) }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                drawerContentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Spacer(Modifier.height(20.dp))
                Text(modifier = Modifier.padding(start = 30.dp, bottom = GUTTER_PADDING.dp),
                    text = "Hydroponics App", style = MaterialTheme.typography.headlineSmall)
                Divider(modifier = Modifier.padding(top = GUTTER_PADDING.dp))
                NavigationDrawerItem(
                    label = { Text(text = "Log out") },
                    icon = { Icon(imageVector = Icons.Default.ExitToApp, contentDescription = "Log out")},
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                        }
                        logout()
                    },
                    modifier = Modifier
                        .padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier,
                    title = { Text(text = title, color = MaterialTheme.colorScheme.onPrimaryContainer) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "menu",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {  logout()  }) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = "log out",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                )
            }
        ) {
            Box(
                modifier = Modifier
                    .padding(it)
            ) {
                content()
            }
        }
    }
}