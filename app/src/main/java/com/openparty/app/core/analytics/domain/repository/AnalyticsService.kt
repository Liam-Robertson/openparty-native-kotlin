package com.openparty.app.core.analytics.domain.repository

import com.openparty.app.core.analytics.domain.AnalyticsEvent

interface AnalyticsService {
    fun trackEvent(event: AnalyticsEvent)
    fun identifyUser(userId: String, properties: Map<String, Any>)
    fun getDistinctId(): String
}
