package com.estoyok.app.core.data.remote

import com.estoyok.app.core.data.local.SessionManager
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONObject
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
        
        if (response.code == 401) {
            val retriedResponse = synchronized(this) {
                // Re-read token to see if another parallel request already refreshed it
                val currentToken = runBlocking { sessionManager.authTokenFlow.firstOrNull() }
                val originalToken = finalRequest.header("Authorization")?.replace("Bearer ", "")
                
                if (currentToken != originalToken && !currentToken.isNullOrEmpty()) {
                    // Token has been refreshed by another thread
                    val retriedRequest = finalRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    val nextResponse = chain.proceed(retriedRequest)
                    if (nextResponse.code != 401) nextResponse else null
                } else {
                    // Try to perform a silent auto-login
                    val email = runBlocking { sessionManager.userEmailFlow.firstOrNull() }
                    val password = runBlocking { sessionManager.getDecryptedPassword() }
                    
                    if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        val baseUrl = savedUrl ?: "http://127.0.0.1:8000/api/"
                        val loginUrl = if (baseUrl.endsWith("/")) "${baseUrl}login" else "$baseUrl/login"
                        
                        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
                        val loginBodyJson = """{"email":"$email","password":"$password","device_name":"android"}"""
                        
                        val loginRequest = Request.Builder()
                            .url(loginUrl)
                            .post(loginBodyJson.toRequestBody(mediaType))
                            .header("Accept", "application/json")
                            .header("Content-Type", "application/json")
                            .build()
                        
                        var loginResponse: Response? = null
                        try {
                            loginResponse = chain.proceed(loginRequest)
                            if (loginResponse.isSuccessful) {
                                val bodyString = loginResponse.body?.string()
                                if (bodyString != null) {
                                    val json = JSONObject(bodyString)
                                    val newToken = json.getString("token")
                                    val userJson = json.getJSONObject("user")
                                    val name = userJson.getString("name")
                                    val phone = if (userJson.isNull("phone")) null else userJson.getString("phone")
                                    
                                    runBlocking {
                                        sessionManager.saveSession(newToken, name, email, phone)
                                    }
                                    
                                    val retriedRequest = finalRequest.newBuilder()
                                        .header("Authorization", "Bearer $newToken")
                                        .build()
                                    chain.proceed(retriedRequest)
                                } else {
                                    null
                                }
                            } else {
                                runBlocking {
                                    sessionManager.clearSession()
                                }
                                null
                            }
                        } catch (e: Exception) {
                            null
                        } finally {
                            loginResponse?.close()
                        }
                    } else {
                        runBlocking {
                            sessionManager.clearSession()
                        }
                        null
                    }
                }
            }
            if (retriedResponse != null) {
                // Close the original response body before returning the new one to prevent resource leaks
                response.close()
                return retriedResponse
            }
        }

        return response
    }
}
