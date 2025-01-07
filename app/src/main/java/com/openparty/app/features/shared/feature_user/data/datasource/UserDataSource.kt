package com.openparty.app.features.shared.feature_user.data.datasource

import com.openparty.app.features.shared.feature_user.data.model.UserDto

interface UserDataSource {
    suspend fun fetchUser(userId: String): UserDto
    suspend fun isScreenNameTaken(name: String): Boolean
    suspend fun updateUser(userId: String, request: Any)
    suspend fun addUser(userId: String, user: UserDto)
}
