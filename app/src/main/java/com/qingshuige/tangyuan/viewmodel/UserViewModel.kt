package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.CreateUserDto
import com.qingshuige.tangyuan.model.LoginDto
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

data class UserUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val currentUser: User? = null,
    val error: String? = null
)

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val tokenManager = TokenManager()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _userUiState = MutableStateFlow(UserUiState())
    val userUiState: StateFlow<UserUiState> = _userUiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    init {
        // 启动时尝试自动登录
        checkAutoLogin()
    }

    /**
     * 检查是否可以自动登录
     */
    private fun checkAutoLogin() {
        viewModelScope.launch {
            val token = tokenManager.token
            val phoneNumber = tokenManager.phoneNumber
            val password = tokenManager.password
            
            if (tokenManager.isTokenValid()) {
                // Token有效，设置登录状态并获取用户信息
                _loginState.value = _loginState.value.copy(isLoggedIn = true)
                getCurrentUserFromToken()
            } else if (phoneNumber != null && password != null) {
                // Token无效但有保存的账号密码，尝试自动登录
                autoLogin(phoneNumber, password)
            }
        }
    }

    /**
     * 自动登录
     */
    private fun autoLogin(phoneNumber: String, password: String) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            val loginDto = LoginDto(phoneNumber = phoneNumber, password = password)
            
            userRepository.login(loginDto)
                .catch { e ->
                    // 自动登录失败，清除保存的凭据
                    tokenManager.clearAll()
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = null // 自动登录失败不显示错误
                    )
                }
                .collect { result ->
                    // 自动登录成功，保存新token
                    val newToken = result["token"]
                    if (newToken != null) {
                        tokenManager.token = newToken
                    }
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                    )
                    // 自动登录成功后获取用户信息
                    getCurrentUserFromToken()
                }
        }
    }

    /**
     * 从token中获取用户ID并加载用户信息
     */
    private fun getCurrentUserFromToken() {
        viewModelScope.launch {
            val userId = tokenManager.getUserIdFromToken()
            println("DEBUG: 从token中获取的用户ID: $userId")
            if (userId != null) {
                // 获取用户信息
                userRepository.getUserById(userId)
                    .catch { e ->
                        println("DEBUG: 获取用户信息失败: ${e.message}")
                        // 获取用户信息失败
                        _userUiState.value = _userUiState.value.copy(
                            error = e.message
                        )
                    }
                    .collect { user ->
                        println("DEBUG: 获取到用户信息: ${user.nickName}, 头像: ${user.avatarGuid}")
                        // 更新userUiState
                        _userUiState.value = _userUiState.value.copy(
                            currentUser = user
                        )
                        // 同时更新loginState中的用户信息
                        _loginState.value = _loginState.value.copy(user = user)
                    }
            } else {
                println("DEBUG: 无法从token中解析用户ID")
            }
        }
    }

    fun login(loginDto: LoginDto) {
        viewModelScope.launch {
            _loginState.value = _loginState.value.copy(isLoading = true, error = null)
            userRepository.login(loginDto)
                .catch { e ->
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { result ->
                    // 登录成功，保存token
                    val token = result["token"]
                    if (token != null) {
                        tokenManager.token = token
                    }
                    // 登录成功，保存账号密码用于自动登录
                    tokenManager.setPhoneNumberAndPassword(
                        loginDto.phoneNumber, 
                        loginDto.password
                    )
                    
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                    )
                    
                    // 登录成功后获取用户信息
                    getCurrentUserFromToken()
                }
        }
    }

    fun register(createUserDto: CreateUserDto) {
        viewModelScope.launch {
            _userUiState.value = _userUiState.value.copy(isLoading = true, error = null)
            userRepository.register(createUserDto)
                .catch { e ->
                    _userUiState.value = _userUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { success ->
                    if (success) {
                        // 注册成功后自动登录
                        _userUiState.value = _userUiState.value.copy(isLoading = false)
                        
                        // 自动登录
                        val loginDto = LoginDto(
                            phoneNumber = createUserDto.phoneNumber,
                            password = createUserDto.password
                        )
                        login(loginDto)
                    } else {
                        _userUiState.value = _userUiState.value.copy(
                            isLoading = false,
                            error = "注册失败，请重试"
                        )
                    }
                }
        }
    }

    fun getUserById(userId: Int) {
        viewModelScope.launch {
            _userUiState.value = _userUiState.value.copy(isLoading = true, error = null)
            userRepository.getUserById(userId)
                .catch { e ->
                    _userUiState.value = _userUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { user ->
                    _userUiState.value = _userUiState.value.copy(
                        isLoading = false,
                        currentUser = user
                    )
                }
        }
    }

    fun updateUser(userId: Int, user: User) {
        viewModelScope.launch {
            _userUiState.value = _userUiState.value.copy(isLoading = true, error = null)
            userRepository.updateUser(userId, user)
                .catch { e ->
                    _userUiState.value = _userUiState.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { success ->
                    if (success) {
                        getUserById(userId)
                    }
                }
        }
    }

    fun searchUsers(keyword: String) {
        viewModelScope.launch {
            userRepository.searchUsers(keyword)
                .catch { e ->
                    _userUiState.value = _userUiState.value.copy(error = e.message)
                }
                .collect { users ->
                    _searchResults.value = users
                }
        }
    }

    fun logout() {
        // 清除所有登录信息
        tokenManager.clearAll()
        
        _loginState.value = LoginState()
        _userUiState.value = UserUiState()
    }

    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
        _userUiState.value = _userUiState.value.copy(error = null)
    }

    /**
     * 获取当前用户头像URL
     */
    fun getCurrentUserAvatarUrl(): String? {
        val user = _loginState.value.user ?: _userUiState.value.currentUser
        return user?.let { 
            "${com.qingshuige.tangyuan.TangyuanApplication.instance.bizDomain}images/${it.avatarGuid}.jpg"
        }
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isTokenValid() && _loginState.value.isLoggedIn
    }
}