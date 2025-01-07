package com.openparty.app.features.shared.feature_permissions.domain

import android.content.Context
import com.openparty.app.core.shared.domain.DomainResult

interface PermissionManager {
    fun hasPermission(context: Context, permission: String): DomainResult<Boolean>
}