package pt.ipt.dam.sabordigital.ui.main.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import pt.ipt.dam.sabordigital.data.remote.models.User
import pt.ipt.dam.sabordigital.data.retrofit.RetrofitInitializer

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
                    RetrofitInitializer().userService().getUser("Bearer $token")
                        .enqueue(object : retrofit2.Callback<User> {
                            override fun onResponse(
                                call: retrofit2.Call<User>,
                                response: retrofit2.Response<User>
                            ) {
                                if (response.isSuccessful) {
                                    _user.value = response.body()
                                }
                                _loading.value = false
                            }

                            override fun onFailure(call: retrofit2.Call<User>, t: Throwable) {
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
                    RetrofitInitializer().userService().changePassword("Bearer $token", newPassword)
                        .enqueue(object : retrofit2.Callback<Void> {
                            override fun onResponse(
                                call: retrofit2.Call<Void>,
                                response: retrofit2.Response<Void>
                            ) {
                                onComplete(response.isSuccessful)
                            }

                            override fun onFailure(call: retrofit2.Call<Void>, t: Throwable) {
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
}