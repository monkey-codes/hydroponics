package codes.monkey.hydroponics.navigation

enum class AppScreens {
    SplashScreen,
    LoginScreen,
    HomeScreen;

    companion object {
        fun fromRoute(route: String?): AppScreens =
            when(route?.substringBefore("/")) {
                SplashScreen.name -> SplashScreen
                LoginScreen.name -> LoginScreen
                HomeScreen.name -> HomeScreen
                null -> HomeScreen
                else -> throw IllegalArgumentException("$route is not configured")
            }
    }
}