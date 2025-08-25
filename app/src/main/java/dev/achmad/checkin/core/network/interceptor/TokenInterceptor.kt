package dev.achmad.checkin.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(
    private val tokenProvider: () -> String?
): Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val requestBuilder = chain.request().newBuilder()
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}