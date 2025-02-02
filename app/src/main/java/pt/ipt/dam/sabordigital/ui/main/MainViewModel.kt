package pt.ipt.dam.sabordigital.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel for MainActivity which is responsible for managing authentication state.
 *
 * It exposes authentication status via LiveData and provides methods
 * to check and update the authentication state (e.g., logout).
 */
class MainViewModel : ViewModel() {

    // LiveData holding the current authentication state of the user.
    // True indicates that the user is authenticated; false otherwise.
    private val _authState = MutableLiveData<Boolean>()

    /**
     * Checks the current authentication state based on stored token information.
     *
     * Retrieves the JWT token from SharedPreferences. If a token exists,
     * the user is considered authenticated.
     *
     * @param context The context used to access SharedPreferences.
     */
    fun checkAuthState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("jwt_token", null)
        _authState.value = token != null
    }

}