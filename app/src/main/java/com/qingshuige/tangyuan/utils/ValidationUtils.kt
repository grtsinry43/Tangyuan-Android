package com.qingshuige.tangyuan.utils

import java.util.regex.Pattern

/**
 * 表单验证工具类
 */
object ValidationUtils {

    /**
     * 验证手机号格式
     * 支持中国大陆手机号格式：11位数字，以1开头
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        if (phoneNumber.isBlank()) return false
        
        // 中国大陆手机号正则表达式
        val phonePattern = Pattern.compile("^1[3-9]\\d{9}$")
        return phonePattern.matcher(phoneNumber).matches()
    }

    /**
     * 验证密码强度
     * 要求：至少6位，包含字母和数字
     */
    fun isValidPassword(password: String): Boolean {
        if (password.length < 6) return false
        
        // 检查是否包含字母和数字
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        
        return hasLetter && hasDigit
    }

    /**
     * 验证昵称格式
     * 要求：1-20个字符，不能包含特殊字符
     */
    fun isValidNickname(nickname: String): Boolean {
        if (nickname.isBlank() || nickname.length > 20) return false
        
        // 允许中文、英文、数字、下划线
        val nicknamePattern = Pattern.compile("^[\\u4e00-\\u9fa5a-zA-Z0-9_]+$")
        return nicknamePattern.matcher(nickname).matches()
    }

    /**
     * 获取手机号验证错误信息
     */
    fun getPhoneNumberError(phoneNumber: String): String? {
        return when {
            phoneNumber.isBlank() -> "请输入手机号"
            !isValidPhoneNumber(phoneNumber) -> "请输入正确的手机号格式"
            else -> null
        }
    }

    /**
     * 获取密码验证错误信息
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isBlank() -> "请输入密码"
            password.length < 6 -> "密码至少需要6位"
            !password.any { it.isLetter() } -> "密码必须包含字母"
            !password.any { it.isDigit() } -> "密码必须包含数字"
            else -> null
        }
    }

    /**
     * 获取昵称验证错误信息
     */
    fun getNicknameError(nickname: String): String? {
        return when {
            nickname.isBlank() -> "请输入昵称"
            nickname.length > 20 -> "昵称不能超过20个字符"
            !isValidNickname(nickname) -> "昵称只能包含中文、英文、数字和下划线"
            else -> null
        }
    }

    /**
     * 获取确认密码验证错误信息
     */
    fun getConfirmPasswordError(password: String, confirmPassword: String): String? {
        return when {
            confirmPassword.isBlank() -> "请确认密码"
            password != confirmPassword -> "两次输入的密码不一致"
            else -> null
        }
    }
}