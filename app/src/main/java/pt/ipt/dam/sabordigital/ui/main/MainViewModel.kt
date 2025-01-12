package pt.ipt.dam.sabordigital.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _authState = MutableLiveData<Boolean>()
    val authState: LiveData<Boolean> = _authState

    fun checkAuthState(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPrefs.getString("jwt_token", null)
        _authState.value = token != null
    }

    fun logout(context: Context) {
        val sharedPrefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().remove("jwt_token").apply()
        _authState.value = false
    }
}
