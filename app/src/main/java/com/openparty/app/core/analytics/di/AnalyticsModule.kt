package com.openparty.app.core.analytics.di

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.openparty.app.BuildConfig
import com.openparty.app.core.analytics.data.AnalyticsManager
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideMixpanel(@ApplicationContext context: Context): MixpanelAPI {
        return MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN)
    }

    @Provides
    @Singleton
    fun provideAnalyticsService(mixpanel: MixpanelAPI): AnalyticsService {
        return AnalyticsManager(mixpanel)
    }
}
