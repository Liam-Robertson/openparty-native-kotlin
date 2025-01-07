package com.openparty.app.core.network

import com.openparty.app.core.storage.SecureStorage
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.startup.feature_authentication.domain.usecase.RefreshAccessTokenUseCase
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import timber.log.Timber

class TokenInterceptor @Inject constructor(
    private val secureStorage: SecureStorage,
    private val refreshAccessTokenUseCase: RefreshAccessTokenUseCase
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var token = secureStorage.getToken()

        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())

        if (response.code == 401) {
            response.close()
            val refreshResult = runBlocking { refreshAccessTokenUseCase() }

            when (refreshResult) {
                is DomainResult.Success -> {
                    token = refreshResult.data
                    if (!token.isNullOrBlank()) {
                        secureStorage.saveToken(token)
                        val newRequest = originalRequest.newBuilder()
                            .header("Authorization", "Bearer $token")
                            .build()
                        Timber.d("TokenInterceptor: Retry with new token")
                        return chain.proceed(newRequest)
                    }
                }
                is DomainResult.Failure -> {
                    Timber.e(refreshResult.error, "TokenInterceptor: Failed to refresh access token")
                    // Optionally, you can handle specific AppError cases here
                    // For now, return the original 401 response
                }
            }
        }

        return response
    }
}
