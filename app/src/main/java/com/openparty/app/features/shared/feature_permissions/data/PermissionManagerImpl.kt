package com.openparty.app.features.shared.feature_permissions.data

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_permissions.domain.PermissionManager
import timber.log.Timber
import javax.inject.Inject

class PermissionManagerImpl @Inject constructor() : PermissionManager {

    override fun hasPermission(context: Context, permission: String): DomainResult<Boolean> {
        Timber.d("Checking permission: $permission")
        return try {
            val isGranted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            Timber.d("Permission $permission granted: $isGranted")
            DomainResult.Success(isGranted)
        } catch (e: Exception) {
            Timber.e(e, "Error checking permission: $permission")
            DomainResult.Failure(AppError.Permissions.General)
        }
    }
}
