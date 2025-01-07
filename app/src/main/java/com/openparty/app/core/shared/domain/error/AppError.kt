package com.openparty.app.core.shared.domain.error

sealed class AppError : Exception() {

    sealed class Navigation : AppError() {
        object General : AppError()
        object DetermineAuthStates : AppError()
    }

    sealed class Analytics : AppError() {
        object TrackAppOpen : AppError()
        object IdentifyUser : AppError()
        object TrackDiscussionsPreviewClick : AppError()
        object TrackCouncilMeetingPreviewClick : AppError()
        object TrackDiscussionPosted : AppError()
        object TrackCommentPosted : AppError()
    }

    sealed class Permissions : AppError() {
        object General : AppError()
        object RefusedLocationPermissions : AppError()
    }

    sealed class User : AppError() {
        object General : AppError()
        object UpdateUserUseCase : AppError()
    }

    sealed class Authentication : AppError() {
        object General : AppError()
        object SignIn : AppError()
        object Register : AppError()
        object EmailVerification : AppError()
        object RefreshToken : AppError()
        object Logout : AppError()
        object GetUserId : AppError()
        object GetUser : AppError()
        object UserAlreadyExists : AppError()
    }

    sealed class Register : AppError() {
        object General : AppError()
        object ValidateEmail : AppError()
        object ValidatePassword : AppError()
    }

    sealed class LocationVerification : AppError() {
        object General : AppError()
        object UpdateUserLocation : AppError()
        object VerifyLocation : AppError()
        object LocationPopup : AppError()
        object HandleLocationsPopup : AppError()
        object LocationPermissionsError : AppError()
    }

    sealed class ScreenNameGeneration : AppError() {
        object General : AppError()
        object GenerateScreenName : AppError()
        object ScreenNameTaken : AppError()
    }

    sealed class Comments : AppError() {
        object General : AppError()
        object FetchComments : AppError()
        object AddComment : AppError()
    }

    sealed class Discussion : AppError() {
        object General : AppError()
        object FetchDiscussions : AppError()
        object AddDiscussion : AppError()
    }

    sealed class CouncilMeeting : AppError() {
        object General : AppError()
        object FetchCouncilMeetings : AppError()
        object PlayAudio : AppError()
        object PauseAudio : AppError()
    }
}
