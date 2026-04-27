package com.example.movie_project.networking

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp Interceptor that retries failed network requests.
 * Retries on both network failures (IOException) and HTTP error responses (non-2xx).
 *
 * @param maxRetries Maximum number of total attempts (default: 3)
 * @param retryDelayMillis Delay in milliseconds between retries (default: 3000ms)
 */
class RetryInterceptor(
    private val maxRetries: Int = 3,
    private val retryDelayMillis: Long = 3000L
) : Interceptor {

    companion object {
        private const val TAG = "RetryInterceptor"
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var lastException: IOException? = null
        var response: Response? = null

        for (attempt in 1..maxRetries) {
            try {
                // Close previous unsuccessful response before retrying
                response?.close()

                response = chain.proceed(request)

                // If the response is successful, return immediately
                if (response.isSuccessful) {
                    return response
                }

                // HTTP error response — log and retry if attempts remain
                Log.w(TAG, "Attempt $attempt/$maxRetries failed with HTTP ${response.code}")

                if (attempt == maxRetries) {
                    // Final attempt — return the error response as-is
                    return response
                }

                // Wait before retrying
                Thread.sleep(retryDelayMillis)

            } catch (e: IOException) {
                lastException = e
                Log.w(TAG, "Attempt $attempt/$maxRetries failed with exception: ${e.message}")

                if (attempt == maxRetries) {
                    throw lastException
                }

                // Wait before retrying
                Thread.sleep(retryDelayMillis)
            }
        }

        // This should never be reached, but satisfies the compiler
        throw lastException ?: IOException("Unknown error after $maxRetries retries")
    }
}