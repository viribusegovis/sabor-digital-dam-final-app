package pt.ipt.dam.sabordigital.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.ipt.dam.sabordigital.data.remote.models.TokenRequest
import pt.ipt.dam.sabordigital.data.remote.models.TokenResponse
import pt.ipt.dam.sabordigital.data.remote.models.UserCreate
import pt.ipt.dam.sabordigital.data.remote.models.UserResponse
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// ViewModel responsible for handling user registration and subsequent login
// Manages registration state, validation, and API communication

class RegisterViewModel : ViewModel() {
    // LiveData for tracking registration and login states
    private val _registerState = MutableLiveData<Result<UserResponse>>()
    private val _loginState = MutableLiveData<Result<TokenResponse>>()
    val loginState: LiveData<Result<TokenResponse>> = _loginState

    /**
     * Handles user registration process through API
     * On successful registration, automatically proceeds to login
     *
     * @param name User's full name
     * @param email User's email address
     * @param password User's chosen password
     */
    fun register(name: String, email: String, password: String) {
        val request = UserCreate(email, password, name)
        RetrofitInitializer().userService().register(request)
            .enqueue(object : Callback<UserResponse> {
                override fun onResponse(
                    call: Call<UserResponse>,
                    response: Response<UserResponse>
                ) {
                    if (response.isSuccessful) {
                        _registerState.value = Result.success(response.body()!!)
                        login(email, password)  // Automatic login after registration
                    } else {
                        _registerState.value = Result.failure(Exception("Registration failed"))
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    _registerState.value = Result.failure(t)
                }
            })
    }

    /**
     * Handles user login process after successful registration
     *
     * @param email User's email address
     * @param password User's password
     */
    fun login(email: String, password: String) {
        val credentials = TokenRequest(email, password)
        val call = RetrofitInitializer().authService().login(credentials)

        call.enqueue(object : Callback<TokenResponse> {
            override fun onResponse(call: Call<TokenResponse>, response: Response<TokenResponse>) {
                response.body()?.let { token ->
                    _loginState.value = Result.success(token)
                }
            }

            override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                _loginState.value = Result.failure(t)
            }
        })
    }

    /**
     * Validates user input credentials
     *
     * @param name User's full name
     * @param email User's email address
     * @param password User's password
     * @return Boolean indicating if all credentials are valid
     *
     * Validation rules:
     * - Name must not be empty
     * - Email must not be empty and must be valid format
     * - Password must not be empty and must be at least 8 characters
     */
    fun validateCredentials(name: String, email: String, password: String): Boolean {
        if (email.isEmpty()) {
            return false
        }
        if (name.isEmpty()) {
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        if (password.isEmpty()) {
            return false
        }
        if (password.length < 8) {
            return false
        }
        return true
    }
}
