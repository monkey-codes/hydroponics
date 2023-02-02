package codes.monkey.hydroponics.repository

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.hilt.work.HiltWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import codes.monkey.hydroponics.di.dataStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.Duration

@HiltWorker
class ExampleWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    val tokenManager: TokenManager
) : Worker(appContext, workerParams) {
    override fun doWork(): Result {
        Log.i("LOGIN", "doing worker work")
        runBlocking {
//            tokenManager.logoutEvent.emit(true)
            tokenManager.logout()
        }

        return Result.success()
    }

}

//@HiltWorker
//class TempWorker(context: Context,params: WorkerParameters): Worker(context,params) {
//    override fun doWork(): Result {
//        Log.i("LOGIN", "doing worker work")
//        return Result.success()
//    }
//
//}
class TokenManager(private val context: Context) {
    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    }

    val logoutEvent = MutableSharedFlow<Boolean>()

    init {
        Log.i("LOGIN", "token manager created")
        val request:WorkRequest = OneTimeWorkRequest.Builder(ExampleWorker::class.java)
            .setInitialDelay(Duration.ofSeconds(20)).build()
        WorkManager.getInstance(context).enqueue(request)
    }
    fun getAccessToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ACCESS_TOKEN_KEY]
        }
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = token
        }
    }

    suspend fun deleteAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
        }
    }

    fun getRefreshToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[REFRESH_TOKEN_KEY]
        }
    }

    suspend fun saveRefreshToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[REFRESH_TOKEN_KEY] = token
        }
    }

    suspend fun deleteRefreshToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }

    suspend fun logout() {
       deleteRefreshToken()
        deleteAccessToken()
        logoutEvent.emit(true)
    }
}