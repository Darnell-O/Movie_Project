package com.example.movie_project.networking

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RetryInterceptorTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Use 0ms delay so tests run instantly
        client = OkHttpClient.Builder()
            .addInterceptor(RetryInterceptor(maxRetries = 3, retryDelayMillis = 3000L))
            .build()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    /**
     * Test 1: Verifies that a successful first response returns immediately
     * with no retries — the happy path.
     */
    @Test
    fun successOnFirstAttempt_noRetry() {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/")).build()
        ).execute()

        assertEquals(200, response.code)
        assertEquals(1, mockWebServer.requestCount)
    }

    /**
     * Test 2: Verifies that when the first attempt returns an HTTP 500 error,
     * the interceptor retries and succeeds on the second attempt.
     */
    @Test
    fun httpErrorThenSuccess_retriesAndSucceeds() {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("OK"))

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/")).build()
        ).execute()

        assertEquals(200, response.code)
        assertEquals(2, mockWebServer.requestCount) // Retried once
    }

    /**
     * Test 3: Verifies that when all 3 attempts return HTTP errors,
     * the interceptor exhausts all retries and returns the final error response.
     */
    @Test
    fun allAttemptsFailWithHttpError_returnsLastErrorResponse() {
        repeat(3) {
            mockWebServer.enqueue(MockResponse().setResponseCode(503).setBody("Service Unavailable"))
        }

        val response = client.newCall(
            Request.Builder().url(mockWebServer.url("/")).build()
        ).execute()

        assertEquals(503, response.code)
        assertEquals(3, mockWebServer.requestCount) // All 3 attempts exhausted
    }
}
