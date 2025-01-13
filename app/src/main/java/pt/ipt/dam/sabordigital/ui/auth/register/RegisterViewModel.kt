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

class RegisterViewModel : ViewModel() {
    private val _registerState = MutableLiveData<Result<UserResponse>>()
    val registerState: LiveData<Result<UserResponse>> = _registerState

    private val _loginState = MutableLiveData<Result<TokenResponse>>()
    val loginState: LiveData<Result<TokenResponse>> = _loginState

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
                        // After successful registration, proceed to login
                        login(email, password)
                    } else {
                        _registerState.value = Result.failure(Exception("Registration failed"))
                    }
                }

                override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                    _registerState.value = Result.failure(t)
                }
            })
    }

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

        return true
    }
}
