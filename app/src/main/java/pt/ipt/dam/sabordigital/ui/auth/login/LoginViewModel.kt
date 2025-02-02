package pt.ipt.dam.sabordigital.ui.auth.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam.sabordigital.data.remote.models.TokenRequest
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel responsible for handling login business logic
// Manages authentication state and credential validation
// Uses LiveData for observing login state changes

class LoginViewModel : ViewModel() {
    // LiveData for login state management
    private val _loginState = MutableLiveData<Result<TokenResponse>>()
    val loginState: LiveData<Result<TokenResponse>> = _loginState

    // Handles login process with API communication
    fun login(email: String, password: String) {
        val credentials = TokenRequest(email, password)
        val call = RetrofitInitializer().authService().login(credentials)

        call.enqueue(object : Callback<TokenResponse> {
            // Handles API response
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = Result.success(response.body()!!)  // Success case
                } else {
                    // Error case (401, 404, etc.)
                    _loginState.value = Result.failure(Exception("Credenciais Inválidas"))
                }
            }

            // Handles network or other failures
            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                _loginState.value = Result.failure(t)
            }
        })
    }

    // Validates user input credentials
    fun validateCredentials(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }

        if (password.isEmpty()) {
            return false
        }

        return true
    }
}
