package com.openparty.app.features.shared.feature_user.domain.model

data class User(
    val userId: String,
    val screenName: String,
    val accountType: AccountType,
    val title: String?,
    val manuallyVerified: Boolean,
    val isLocationVerified: Boolean,
)

enum class AccountType {
    ADMIN,
    CONSTITUENT,
    REPRESENTATIVE
}
