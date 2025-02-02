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

/**
 * ViewModel handling user profile-related operations.
 *
 * It manages user data loading, logout, password change, and account deletion
 * by communicating with the API through Retrofit.
 */
class ProfileViewModel : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    /**
     * Loads the user profile from the API.
     *
     * Retrieves the authentication token from SharedPreferences and uses it to
     * fetch the user details. Updates the [_user] LiveData when data is retrieved.
     *
     * @param context The context used to access SharedPreferences.
     */
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
                } else {
                    _loading.value = false
                }
            } catch (e: Exception) {
                _loading.value = false
            }
        }
    }

    /**
     * Retrieves the authentication token from SharedPreferences.
     *
     * @param context The context used to access SharedPreferences.
     * @return The authentication token if available; otherwise, null.
     */
    private fun getAuthToken(context: Context): String? {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPrefs.getString("jwt_token", null)
    }

    /**
     * Logs out the user by clearing the authentication token from SharedPreferences.
     *
     * @param context The context used to access SharedPreferences.
     */
    fun logout(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }

    /**
     * Changes the user's password.
     *
     * Sends a password change request to the API with the new password.
     * The provided callback function is used to indicate whether the operation was successful.
     *
     * @param context The context used for API communication and SharedPreferences.
     * @param newPassword The new password to set.
     * @param onComplete Callback invoked with true if the password was successfully changed, false otherwise.
     */
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

    /**
     * Navigates the user to the login screen.
     *
     * This method sets up an intent to launch the login activity with flags to clear the current task.
     * It is typically called after account deletion.
     *
     * @param context The context used to start the activity.
     */
    private fun navigateToLoginScreen(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(context, intent, null)
    }

    /**
     * Deletes the user's account.
     *
     * Sends an account deletion request to the API. On success, displays a toast indicating account deletion
     * and navigates the user to the login screen. On failure, shows an error toast.
     *
     * @param context The context used for API communication and UI feedback.
     */
    fun deleteAccount(context: Context) {
        val call = getAuthToken(context)?.let {
            RetrofitInitializer().userService().deleteAccount(it)
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