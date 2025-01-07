package com.openparty.app.core.shared.domain

import com.openparty.app.core.shared.domain.error.AppError

sealed class DomainResult<out T> {
    data class Success<out T>(val data: T) : DomainResult<T>()
    data class Failure(val error: AppError) : DomainResult<Nothing>()
}
