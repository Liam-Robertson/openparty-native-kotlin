package com.openparty.app.features.shared.feature_user.data.model

data class UserDto(
    val userId: String = "",
    val accountType: String? = null,
    val manuallyVerified: Boolean = false,
    val governmentName: String? = null,
    val location: String? = null,
    val locationVerified: Boolean = false,
    val otherUserInfo: OtherUserInfo? = null,
    val screenName: String? = null,
    val title: String? = null
)

data class OtherUserInfo(
    val email: String = "",
    val phoneNumber: String? = null,
    val profilePictureUrl: String? = null
)
