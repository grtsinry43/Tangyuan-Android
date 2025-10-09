package com.qingshuige.tangyuan.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingshuige.tangyuan.model.User
import com.qingshuige.tangyuan.network.TokenManager
import com.qingshuige.tangyuan.repository.UserRepository
import com.qingshuige.tangyuan.utils.UIUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val nickName: String = "",
    val email: String = "",
    val region: String = "",
    val bio: String = "",
    val hasChanges: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private var originalUser: User? = null
    private val tokenManager = TokenManager()

    init {
        // 初始化时加载当前用户信息
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            // 从 token 获取用户 ID
            val userId = tokenManager.getUserIdFromToken()
            if (userId != null) {
                loadUserById(userId)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "未找到用户信息，请先登录"
                )
            }
        }
    }

    private fun loadUserById(userId: Int) {
        viewModelScope.launch {
            userRepository.getUserById(userId)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "加载用户信息失败: ${e.message}"
                    )
                }
                .collect { user ->
                    originalUser = user
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = user,
                        nickName = user.nickName,
                        email = user.email,
                        region = user.isoRegionName,
                        bio = user.bio
                    )
                }
        }
    }

    fun updateNickName(nickName: String) {
        _uiState.value = _uiState.value.copy(
            nickName = nickName,
            hasChanges = hasChanges(nickName, email = _uiState.value.email, region = _uiState.value.region, bio = _uiState.value.bio)
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            hasChanges = hasChanges(nickName = _uiState.value.nickName, email = email, region = _uiState.value.region, bio = _uiState.value.bio)
        )
    }

    fun updateRegion(region: String) {
        _uiState.value = _uiState.value.copy(
            region = region,
            hasChanges = hasChanges(nickName = _uiState.value.nickName, email = _uiState.value.email, region = region, bio = _uiState.value.bio)
        )
    }

    fun updateBio(bio: String) {
        _uiState.value = _uiState.value.copy(
            bio = bio,
            hasChanges = hasChanges(nickName = _uiState.value.nickName, email = _uiState.value.email, region = _uiState.value.region, bio = bio)
        )
    }

    private fun hasChanges(nickName: String, email: String, region: String, bio: String): Boolean {
        val original = originalUser ?: return false
        return nickName != original.nickName ||
                email != original.email ||
                region != original.isoRegionName ||
                bio != original.bio
    }

    fun saveProfile() {
        val currentState = _uiState.value
        val user = currentState.currentUser ?: return

        if (!currentState.hasChanges) {
            return
        }

        _uiState.value = currentState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val updatedUser = user.copy(
                    nickName = currentState.nickName,
                    email = currentState.email,
                    isoRegionName = currentState.region,
                    bio = currentState.bio
                )

                userRepository.updateUser(user.userId, updatedUser)
                    .catch { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "保存失败: ${exception.message}"
                        )
                        UIUtils.showError("保存失败: ${exception.message}")
                    }
                    .collect { success ->
                        if (success) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                saveSuccess = true,
                                currentUser = updatedUser,
                                hasChanges = false
                            )
                            originalUser = updatedUser
                            UIUtils.showSuccess("保存成功")
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "保存失败，请重试"
                            )
                            UIUtils.showError("保存失败，请重试")
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "保存失败: ${e.message}"
                )
                UIUtils.showError("保存失败: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
