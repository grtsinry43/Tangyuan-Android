package com.qingshuige.tangyuan.repository

import com.qingshuige.tangyuan.api.ApiInterface
import com.qingshuige.tangyuan.model.CreateUserDto
import com.qingshuige.tangyuan.model.LoginDto
import com.qingshuige.tangyuan.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val apiInterface: ApiInterface
) {
    
    fun login(loginDto: LoginDto): Flow<Map<String, String>> = flow {
        val response = apiInterface.login(loginDto).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("Empty response body")
        } else {
            throw Exception("Login failed: ${response.message()}")
        }
    }
    
    fun register(createUserDto: CreateUserDto): Flow<Boolean> = flow {
        val response = apiInterface.postUser(createUserDto).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Registration failed: ${response.message()}")
        }
    }
    
    fun getUserById(userId: Int): Flow<User> = flow {
        val response = apiInterface.getUser(userId).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: throw Exception("User not found")
        } else {
            throw Exception("Failed to get user: ${response.message()}")
        }
    }
    
    fun updateUser(userId: Int, user: User): Flow<Boolean> = flow {
        val response = apiInterface.putUser(userId, user).awaitResponse()
        if (response.isSuccessful) {
            emit(true)
        } else {
            throw Exception("Failed to update user: ${response.message()}")
        }
    }
    
    fun searchUsers(keyword: String): Flow<List<User>> = flow {
        val response = apiInterface.searchUserByKeyword(keyword).awaitResponse()
        if (response.isSuccessful) {
            response.body()?.let { emit(it) } 
                ?: emit(emptyList())
        } else {
            throw Exception("Search failed: ${response.message()}")
        }
    }
}