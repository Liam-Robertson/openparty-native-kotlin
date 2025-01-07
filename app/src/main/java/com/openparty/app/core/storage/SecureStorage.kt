package com.openparty.app.core.storage

interface SecureStorage {
    fun saveToken(token: String)
    fun getToken(): String?
    fun clearToken()
}
