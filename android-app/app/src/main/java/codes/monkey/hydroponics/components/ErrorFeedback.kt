package codes.monkey.hydroponics.components

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import codes.monkey.hydroponics.model.ApiResponse
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.merge

@Composable
fun <T> ErrorFeedback(
    vararg apiStates: StateFlow<ApiResponse<T>?>
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = context) {
        merge(*apiStates).collect { response ->
            if (response is ApiResponse.Failure) {
                Toast.makeText(
                    context,
                    "${response.code}: ${response.errorMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}