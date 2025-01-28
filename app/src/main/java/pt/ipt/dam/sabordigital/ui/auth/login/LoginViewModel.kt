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

class LoginViewModel : ViewModel() {
    private val _loginState = MutableLiveData<Result<TokenResponse>>()
    val loginState: LiveData<Result<TokenResponse>> = _loginState

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
