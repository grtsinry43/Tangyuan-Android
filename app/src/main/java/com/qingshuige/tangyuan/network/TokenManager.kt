package com.qingshuige.tangyuan.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class TokenManager(context: Context? = null) {
    private val prefs: SharedPreferences

    init {
        // 如果没有提供 context，使用全局 Application context
        val ctx = context ?: com.qingshuige.tangyuan.TangyuanApplication.instance
        prefs = ctx.getSharedPreferences("tangyuan_token_prefs", Context.MODE_PRIVATE)
    }

    var token: String?
        get() = prefs.getString("JwtToken", null)
        set(token) {
            prefs.edit { putString("JwtToken", token) }
        }

    val phoneNumber: String?
        get() = prefs.getString("phoneNumber", null)

    val password: String?
        get() = prefs.getString("password", null)

    fun setPhoneNumberAndPassword(phoneNumber: String?, password: String?) {
        prefs.edit {
            putString("phoneNumber", phoneNumber)
            putString("password", password)
        }
    }
}
