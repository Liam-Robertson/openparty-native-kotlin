package com.openparty.app.core.analytics.domain

sealed class AnalyticsEvent(val name: String, val properties: Map<String, Any> = emptyMap()) {
    data class AppOpened(val customProperties: Map<String, Any> = emptyMap()) : AnalyticsEvent(
        "App Opened", customProperties
    )
    data class UserIdentified(val userId: String) : AnalyticsEvent(
        "Identify User", mapOf("is_logged_in" to true)
    )
    object AnonymousAppOpened : AnalyticsEvent("Anonymous User Opened App")
    data class DiscussionPosted(val discussionId: String, val title: String) : AnalyticsEvent(
        "Discussion Posted", mapOf("discussion_id" to discussionId, "title" to title)
    )
    data class CommentPosted(val commentId: String, val discussionId: String, val contentText: String) : AnalyticsEvent(
        "Comment Posted", mapOf("comment_id" to commentId, "discussion_id" to discussionId, "content_text" to contentText)
    )
    data class CouncilMeetingSelected(val councilMeetingId: String) : AnalyticsEvent(
        "Council Meeting Selected", mapOf("council_meeting_id" to councilMeetingId)
    )
    data class DiscussionSelected(val discussionId: String) : AnalyticsEvent(
        "Discussion Selected", mapOf("discussion_id" to discussionId)
    )
}
