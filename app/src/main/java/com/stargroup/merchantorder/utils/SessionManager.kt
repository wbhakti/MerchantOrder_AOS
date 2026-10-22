package com.stargroup.merchantorder.utils

import android.content.Context
import com.stargroup.merchantorder.data.model.response.LoginData

class SessionManager(context: Context) {

    private val pref = context.getSharedPreferences("merchant_order", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGIN = "is_login"
        private const val KEY_ID = "id"
        private const val KEY_USERNAME = "username"
        private const val KEY_ROLE = "role"
        private const val KEY_TOKEN = "token"
    }

    fun saveLogin(data: LoginData) {
        pref.edit()
            .putBoolean(KEY_IS_LOGIN, true)
            .putInt(KEY_ID, data.id)
            .putString(KEY_USERNAME, data.username)
            .putString(KEY_ROLE, data.role)
            .putString(KEY_TOKEN, data.token)
            .apply()
    }

    fun isLogin(): Boolean =
        pref.getBoolean(KEY_IS_LOGIN, false)

    fun getToken(): String =
        pref.getString(KEY_TOKEN,"").toString()

    fun getUsername(): String =
        pref.getString(KEY_USERNAME,"").toString()

    fun getUserId(): Int =
        pref.getInt(KEY_ID,0).toInt()

    fun logout() {
        pref.edit().clear().apply()
    }
}