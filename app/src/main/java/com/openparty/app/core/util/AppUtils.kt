package com.openparty.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AppUtils {
    fun openAppSettings(context: Context) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
