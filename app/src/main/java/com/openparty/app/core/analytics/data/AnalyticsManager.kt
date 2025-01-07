package com.openparty.app.core.analytics.data

import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.openparty.app.core.analytics.domain.AnalyticsEvent
import com.openparty.app.core.analytics.domain.repository.AnalyticsService
import org.json.JSONObject
import javax.inject.Inject

class AnalyticsManager @Inject constructor(
    private val mixpanel: MixpanelAPI
) : AnalyticsService {

    override fun trackEvent(event: AnalyticsEvent) {
        val properties = if (event.properties.isNotEmpty()) JSONObject(event.properties) else null
        if (properties != null) {
            mixpanel.track(event.name, properties)
        } else {
            mixpanel.track(event.name)
        }
    }

    override fun identifyUser(userId: String, properties: Map<String, Any>) {
        mixpanel.identify(userId)
        mixpanel.people.identify(userId)
        mixpanel.people.set(JSONObject(properties))
    }

    override fun getDistinctId(): String {
        return mixpanel.distinctId
    }
}
