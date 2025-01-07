package com.openparty.app.core.shared.presentation

data class UiState (
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)