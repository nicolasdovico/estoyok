package com.estoyok.app.core.data.remote

import android.content.Context
import android.content.Intent
import com.estoyok.app.core.data.local.SessionManager
import com.estoyok.app.services.TrackingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
    @ApplicationContext private val context: Context
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

        val savedUrl = runBlocking {
            sessionManager.apiBaseUrlFlow.firstOrNull()
        }

        val finalRequest = if (!savedUrl.isNullOrEmpty() && savedUrl != "http://127.0.0.1:8000/api/") {
            val newHttpUrl = originalRequest.url.toString().replace("http://127.0.0.1:8000/api/", savedUrl)
            val newUrl = newHttpUrl.toHttpUrlOrNull()
            if (newUrl != null) {
                requestBuilder.url(newUrl).build()
            } else {
                requestBuilder.build()
            }
        } else {
            requestBuilder.build()
        }

        val response = chain.proceed(finalRequest)
        
        // Handle 401 Unauthorized: session revoked remotely or expired
        if (response.code == 401) {
            runBlocking {
                sessionManager.clearSession()
            }
            try {
                val serviceIntent = Intent(context, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_STOP
                }
                context.stopService(serviceIntent)
            } catch (e: Exception) {
                // Ignore service stop exceptions
            }
        }

        return response
    }
}
