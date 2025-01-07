package com.openparty.app.features.shared.feature_user.domain.repository

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.shared.feature_user.data.model.UserDto
import com.openparty.app.features.shared.feature_user.domain.model.User

interface UserRepository {
    suspend fun getUser(userId: String): DomainResult<User>
    suspend fun isScreenNameTaken(name: String): DomainResult<Boolean>
    suspend fun updateUser(userId: String, request: Any): DomainResult<Unit>
    suspend fun addUser(userId: String, user: UserDto): DomainResult<Unit>
}
