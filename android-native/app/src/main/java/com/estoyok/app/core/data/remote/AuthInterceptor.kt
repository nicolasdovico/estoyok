package com.estoyok.app.core.data.remote

import com.estoyok.app.core.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Blocking read is acceptable in OkHttp Interceptors as they run on background threads
        val token = runBlocking {
            sessionManager.authTokenFlow.firstOrNull()
        }

        val requestBuilder = originalRequest.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        if (!token.isNullOrEmpty()) {
            requestBuilder.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(requestBuilder.build())
        
        if (response.code == 401) {
            runBlocking {
                sessionManager.clearSession()
            }
        }

        return response
    }
}
