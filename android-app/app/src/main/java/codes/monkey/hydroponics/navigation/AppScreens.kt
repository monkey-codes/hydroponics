package codes.monkey.hydroponics.navigation

enum class AppScreens {
    SplashScreen,
    LoginScreen,
    HomeScreen,
    DevicesScreen,
    DeviceDetailsScreen,
    VideoPlayerScreen;

    companion object {
        fun fromRoute(route: String?): AppScreens =
            when (route?.substringBefore("/")) {
                SplashScreen.name -> SplashScreen
                LoginScreen.name -> LoginScreen
                HomeScreen.name -> HomeScreen
                DevicesScreen.name -> DevicesScreen
                DeviceDetailsScreen.name -> DeviceDetailsScreen
                null -> HomeScreen
                else -> throw IllegalArgumentException("$route is not configured")
            }
    }
}