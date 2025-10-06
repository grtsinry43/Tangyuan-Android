package com.qingshuige.tangyuan.network

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import android.util.Base64
import com.auth0.jwt.JWT
import com.auth0.jwt.exceptions.JWTDecodeException

/**
 * TokenManager - 负责管理用户认证信息
 * 
 * 安全性说明：
 * 1. 使用SharedPreferences存储在应用私有目录，其他应用无法直接访问
 * 2. 密码使用简单的Base64编码（非加密，仅混淆）
 * 3. 对于生产环境，建议：
 *    - 使用Android Keystore进行真正的加密
 *    - 实现生物识别验证
 *    - 使用refresh token机制减少密码存储
 */
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
        get() {
            val encodedPassword = prefs.getString("password", null)
            return encodedPassword?.let { decodePassword(it) }
        }

    fun setPhoneNumberAndPassword(phoneNumber: String?, password: String?) {
        prefs.edit {
            putString("phoneNumber", phoneNumber)
            // 对密码进行简单编码存储
            putString("password", password?.let { encodePassword(it) })
        }
    }

    /**
     * 清除所有存储的认证信息
     */
    fun clearAll() {
        prefs.edit {
            remove("JwtToken")
            remove("phoneNumber") 
            remove("password")
        }
    }

    /**
     * 检查是否有保存的登录凭据
     */
    fun hasCredentials(): Boolean {
        return phoneNumber != null && password != null
    }

    /**
     * 从JWT token中解析用户ID
     */
    fun getUserIdFromToken(): Int? {
        return token?.let { jwtToken ->
            try {
                val decodedJWT = JWT.decode(jwtToken)
                println("DEBUG: 解析到的JWT: $decodedJWT")
                // 从JWT的name claim中获取用户ID
                decodedJWT.getClaim("http://schemas.xmlsoap.org/ws/2005/05/identity/claims/name")
                    .asString()?.toIntOrNull()
            } catch (e: JWTDecodeException) {
                null
            }
        }
    }

    /**
     * 检查token是否已过期
     */
    fun isTokenExpired(): Boolean {
        return token?.let { jwtToken ->
            try {
                val decodedJWT = JWT.decode(jwtToken)
                val expiresAt = decodedJWT.expiresAt
                expiresAt?.before(java.util.Date()) == true
            } catch (e: JWTDecodeException) {
                true // 如果无法解析，认为已过期
            }
        } ?: true // 如果没有token，认为已过期
    }

    /**
     * 检查token是否有效（存在且未过期）
     */
    fun isTokenValid(): Boolean {
        return token != null && !isTokenExpired()
    }

    /**
     * 简单的密码编码（Base64，仅用于混淆）
     * 注意：这不是真正的加密，只是为了避免明文存储
     */
    private fun encodePassword(password: String): String {
        return Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
    }

    /**
     * 密码解码
     */
    private fun decodePassword(encodedPassword: String): String {
        return String(Base64.decode(encodedPassword, Base64.DEFAULT))
    }

    companion object {
        /**
         * 用于生产环境的密码加密建议
         * 
         * 更安全的实现应该：
         * 1. 使用Android Keystore生成和存储密钥
         * 2. 使用AES加密密码
         * 3. 结合生物识别验证
         * 4. 实现Token刷新机制，减少对密码的依赖
         */
        const val SECURITY_NOTE = """
            当前实现使用SharedPreferences + Base64编码存储密码。
            
            安全性评估：
            - ✅ 存储在应用私有目录，其他应用无法访问
            - ✅ Base64编码避免明文存储
            - ✅ JWT解析获取用户信息
            - ✅ Token过期检查
            - ⚠️ Root设备可能被读取
            - ⚠️ Base64不是真正的加密
            
            生产环境建议：
            - 使用Android Keystore进行真正加密
            - 实现refresh token机制
            - 添加生物识别验证
            - 考虑不存储密码，仅依赖token
        """
    }
}
