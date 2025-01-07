package com.openparty.app.features.shared.feature_permissions.di

import com.openparty.app.features.shared.feature_permissions.data.PermissionManagerImpl
import com.openparty.app.features.shared.feature_permissions.domain.PermissionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PermissionModule {

    @Provides
    fun providePermissionManager(): PermissionManager {
        return PermissionManagerImpl()
    }
}
