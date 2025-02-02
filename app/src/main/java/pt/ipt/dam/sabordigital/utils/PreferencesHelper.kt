package pt.ipt.dam.sabordigital.utils

import android.content.Context

/**
Helper object for managing application preferences.
Provides functions to check and update whether the user has accepted the user agreement,
using SharedPreferences.
 */
object PreferencesHelper {
    private const val PREF_NAME = "app_preferences"
    private const val KEY_USER_AGREEMENT_ACCEPTED = "user_agreement_accepted"

    /**
    Determines whether the user agreement has been accepted.
    @param context The Context used to retrieve the SharedPreferences.
    @return True if the user agreement is accepted; otherwise, false.
     */
    fun isUserAgreementAccepted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USER_AGREEMENT_ACCEPTED, false)
    }

    /**
    Sets the user agreement acceptance status in SharedPreferences.
    @param context The Context used to retrieve the SharedPreferences.
    @param accepted True if the user agreement is accepted; otherwise, false.
     */
    fun setUserAgreementAccepted(context: Context, accepted: Boolean) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USER_AGREEMENT_ACCEPTED, accepted).apply()
    }
}