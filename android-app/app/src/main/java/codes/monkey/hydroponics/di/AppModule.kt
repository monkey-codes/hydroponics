package codes.monkey.hydroponics.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import codes.monkey.hydroponics.network.AuthAPI
import codes.monkey.hydroponics.network.AuthAuthenticator
import codes.monkey.hydroponics.network.AuthInterceptor
import codes.monkey.hydroponics.network.DeviceAPI
import codes.monkey.hydroponics.repository.AuthenticationRepository
import codes.monkey.hydroponics.repository.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "data_store")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)

    @Singleton
    @Provides
    fun provideAuthenticationRepository(api: AuthAPI, tokenManager: TokenManager) =
        AuthenticationRepository(api, tokenManager)

    @Singleton
    @Provides
    fun provideAuthAPI(): AuthAPI {
        val logging = HttpLoggingInterceptor { message -> Log.i("LOGIN", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)

        return Retrofit.Builder()
            .baseUrl("https://cognito-idp.ap-southeast-2.amazonaws.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient.build())
            .build()
            .create(AuthAPI::class.java)
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(tokenManager: TokenManager): AuthInterceptor =
        AuthInterceptor(tokenManager)

    @Singleton
    @Provides
    fun provideAuthAuthenticator(tokenManager: TokenManager, authAPI: AuthAPI): AuthAuthenticator =
        AuthAuthenticator(tokenManager, authAPI)

    @Singleton
    @Provides
    fun provideDeviceAPI(
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator,
    ): DeviceAPI {
        val logging = HttpLoggingInterceptor { message -> Log.i("DEVICE", message) }
        logging.level = HttpLoggingInterceptor.Level.BODY
        val httpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .authenticator(authAuthenticator)
            .build()

        return Retrofit.Builder()
            .baseUrl("https://mqnhmxqyp1.execute-api.ap-southeast-2.amazonaws.com")
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .build()
            .create(DeviceAPI::class.java)
    }
}