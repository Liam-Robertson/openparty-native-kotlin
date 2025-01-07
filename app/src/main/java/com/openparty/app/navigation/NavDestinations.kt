package com.openparty.app.navigation

sealed class NavDestinations(val route: String) {
    object Loading : NavDestinations("loading")
    object Splash : NavDestinations("splash")
    object Login : NavDestinations("login")
    object Register : NavDestinations("register")
    object EmailVerification : NavDestinations("email_verification")
    object LocationVerification : NavDestinations("location_verification")
    object ScreenNameGeneration : NavDestinations("screen_name_generation")
    object ManualVerification : NavDestinations("manual_verification")
    object DiscussionsPreview : NavDestinations("discussions_preview")
    data class DiscussionsArticle(val discussionId: String) : NavDestinations("discussion_article/$discussionId")
    object CouncilMeetingsPreview : NavDestinations("council_meetings_preview")
    data class CouncilMeetingsArticle(val councilMeetingId: String) : NavDestinations("council_meetings_article/$councilMeetingId")
    object AddDiscussion : NavDestinations("add_discussion")
    data class AddComment(
        val discussionId: String,
        val titleText: String
    ) : NavDestinations("add_comment_screen?discussionId=$discussionId&titleText=$titleText")
    object Back : NavDestinations("back")
}
