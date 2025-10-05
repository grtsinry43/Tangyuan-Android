package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.CreateUserDto
import com.qingshuige.tangyuan.model.LoginDto
import com.qingshuige.tangyuan.model.User
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

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _userUiState = MutableStateFlow(UserUiState())
    val userUiState: StateFlow<UserUiState> = _userUiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

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
                    _loginState.value = _loginState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                    )
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
                    _userUiState.value = _userUiState.value.copy(
                        isLoading = false
                    )
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
        _loginState.value = LoginState()
        _userUiState.value = UserUiState()
    }

    fun clearError() {
        _loginState.value = _loginState.value.copy(error = null)
        _userUiState.value = _userUiState.value.copy(error = null)
    }
}