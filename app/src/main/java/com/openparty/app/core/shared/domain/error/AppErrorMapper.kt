package com.openparty.app.core.shared.domain.error

import kotlin.reflect.KClass

object AppErrorMapper {

    fun getUserFriendlyMessage(
        error: AppError,
        customOverrides: Map<KClass<out AppError>, String>? = null
    ): String {
        val overrideMessage = customOverrides?.get(error::class)
        if (overrideMessage != null) {
            return overrideMessage
        }
        return when (error) {
            // Authentication
            is AppError.Authentication.SignIn -> "We couldn't sign you in. Please check your credentials."
            is AppError.Authentication.EmailVerification -> "Verification email couldn't be sent. Please try again in a while."
            is AppError.Authentication.RefreshToken -> "There was an issue sending your request. Please try again in a while."
            is AppError.Authentication.Logout -> "Logout failed. Please try again in a while."
            is AppError.Authentication.General -> "Unknown authentication error."
            is AppError.Authentication.GetUser -> "Unable to fetch user."
            is AppError.Authentication.GetUserId -> "Unable to fetch user."
            is AppError.Authentication.Register -> "Registration failed. Please try again in a while."
            is AppError.Authentication.UserAlreadyExists -> "Cannot create a user that already exists."

            // Discussions
            is AppError.Discussion.General -> "Unknown discussions error."
            is AppError.Discussion.FetchDiscussions -> "Unable to fetch discussions."
            is AppError.Discussion.AddDiscussion -> "Failed to add discussion."

            // Navigation
            is AppError.Navigation.General -> "Unknown navigation error."
            is AppError.Navigation.DetermineAuthStates -> "Unable to go to next screen."

            // Permissions
            is AppError.Permissions.General -> "Unknown permissions error."
            is AppError.Permissions.RefusedLocationPermissions -> "You have not granted location permissions."

            // Metrics
            AppError.Analytics.IdentifyUser -> "Metrics event failed - Identify user."
            AppError.Analytics.TrackAppOpen -> "Metrics event failed - opening app."
            AppError.Analytics.TrackCouncilMeetingPreviewClick -> "Metrics event failed - council meeting preview click."
            AppError.Analytics.TrackDiscussionsPreviewClick -> "Metrics event failed - discussion preview click."
            AppError.Analytics.TrackDiscussionPosted -> "Metrics event failed - add discussion."
            AppError.Analytics.TrackCommentPosted -> "Metrics event failed - add comment."

            // User
            is AppError.User.General -> "Unknown user error."
            is AppError.User.UpdateUserUseCase -> "Failed to update user. Please try again in a while."

            // Register Screen
            is AppError.Register.General -> "Unknown register error."
            is AppError.Register.ValidateEmail -> "Email validation failed."
            is AppError.Register.ValidatePassword -> "Password validation failed."

            // Location Screen
            is AppError.LocationVerification.General -> "Unknown location verification error."
            is AppError.LocationVerification.HandleLocationsPopup -> "Error while handing location permissions. Please try again in a while."
            is AppError.LocationVerification.LocationPermissionsError -> "Error while handing location permissions. Please try again in a while."
            is AppError.LocationVerification.LocationPopup -> "Error while handing location permissions. Please try again in a while."
            is AppError.LocationVerification.UpdateUserLocation -> "Failed to update user location. Please try again in a while."
            is AppError.LocationVerification.VerifyLocation -> "Failed to verify location. Please try again in a while."

            // Screen Name Generation Screen
            is AppError.ScreenNameGeneration.General -> "Unknown name generation error. Please try again in a while."
            is AppError.ScreenNameGeneration.GenerateScreenName -> "Failed to generate screen name. Please try again in a while."
            is AppError.ScreenNameGeneration.ScreenNameTaken -> "That screen name is taken. Please choose another."

            // Comments Screen
            AppError.Comments.General -> "Unknown comments error. Please try again in a while."
            AppError.Comments.FetchComments -> "Failed to fetch comments"
            AppError.Comments.AddComment -> "Failed to add a comment"

            // Council Meetings Screen
            is AppError.CouncilMeeting.FetchCouncilMeetings -> "Failed to fetch latest council meetings. Please try again in a while."
            is AppError.CouncilMeeting.General -> "Failed to verify location. Please try again in a while."
            is AppError.CouncilMeeting.PauseAudio -> "Pausing audio failed."
            is AppError.CouncilMeeting.PlayAudio -> "Play audio failed."
        }
    }
}
