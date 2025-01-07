package com.openparty.app.core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EncryptedSharedPreferencesSecureStorage @Inject constructor(
    @ApplicationContext context: Context
) : SecureStorage {

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveToken(token: String) {
        sharedPreferences.edit().putString("auth_token", token).apply()
    }

    override fun getToken(): String? = sharedPreferences.getString("auth_token", null)

    override fun clearToken() {
        sharedPreferences.edit().remove("auth_token").apply()
    }
}
