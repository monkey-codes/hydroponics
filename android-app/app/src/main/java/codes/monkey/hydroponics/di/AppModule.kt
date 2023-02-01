package codes.monkey.hydroponics.di

import android.util.Log
import codes.monkey.hydroponics.network.AuthAPI
import codes.monkey.hydroponics.repository.AuthenticationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideAuthenticationRepository(api: AuthAPI) = AuthenticationRepository(api)

    @Singleton
    @Provides
    fun provideAuthAPI(): AuthAPI {
        val logging = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.i("LOGIN", message)
            }
        })
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
}