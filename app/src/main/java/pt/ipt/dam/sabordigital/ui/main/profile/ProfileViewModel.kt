package pt.ipt.dam.sabordigital.ui.main.profile

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.R
import pt.ipt.dam.sabordigital.data.remote.api.PasswordChange
import pt.ipt.dam.sabordigital.data.remote.models.User
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer
import pt.ipt.dam.sabordigital.ui.auth.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    fun loadUser(context: Context) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val token = getAuthToken(context)
                if (token != null) {
                    RetrofitInitializer().userService().getUser("$token")
                        .enqueue(object : Callback<User> {
                            override fun onResponse(
                                call: Call<User>,
                                response: Response<User>
                            ) {
                                if (response.isSuccessful) {
                                    _user.value = response.body()
                                }
                                _loading.value = false
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                _loading.value = false
                            }
                        })
                }
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    private fun getAuthToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("jwt_token", null)
    }

    fun logout(context: Context) {
        // Clear shared preferences to log out the user
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }

    fun changePassword(context: Context, newPassword: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = getAuthToken(context)
                if (token != null) {
                    RetrofitInitializer().userService()
                        .changePassword("$token", PasswordChange(newPassword))
                        .enqueue(object : Callback<Void> {
                            override fun onResponse(
                                call: Call<Void>,
                                response: Response<Void>
                            ) {
                                onComplete(response.isSuccessful)
                            }

                            override fun onFailure(call: Call<Void>, t: Throwable) {
                                onComplete(false)
                            }
                        })
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    private fun navigateToLoginScreen(context: Context) {
        // Example: Navigate back to login screen after account deletion.
        // Replace this with your actual navigation logic.
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(context, intent, null)
    }

    fun deleteAccount(context: Context) {

        val call = getAuthToken(context)?.let {
            RetrofitInitializer().userService().deleteAccount(
                it
            )
        }

        call?.enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                Toast.makeText(
                    context,
                    R.string.account_deleted,
                    Toast.LENGTH_SHORT
                ).show()
                navigateToLoginScreen(context)

            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(
                    context,
                    R.string.error_deleting_account,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}