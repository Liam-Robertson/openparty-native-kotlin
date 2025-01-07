package com.openparty.app.core.shared.presentation

import com.openparty.app.navigation.NavDestinations

abstract class UiEvent {
    data class Navigate(val destination: NavDestinations) : UiEvent()
}
