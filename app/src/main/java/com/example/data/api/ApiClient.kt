package com.example.data.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    const val DEFAULT_BASE_URL = "https://mock.attendflow.local/"

    private var apiService: AttendanceApiService? = null
    private var okHttpClient: OkHttpClient? = null

    fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: synchronized(this) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(MockApiInterceptor(context))
                .addInterceptor(logging)
                .build()
            okHttpClient = client
            client
        }
    }

    fun getApiService(context: Context): AttendanceApiService {
        return apiService ?: synchronized(this) {
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(DEFAULT_BASE_URL)
                .client(getOkHttpClient(context))
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()

            val service = retrofit.create(AttendanceApiService::class.java)
            apiService = service
            service
        }
    }

    data class ApiExecutionResult(
        val statusCode: Int,
        val statusMessage: String,
        val durationMs: Long,
        val responseBody: String,
        val headers: Map<String, String>,
        val requestCurl: String
    )

    suspend fun executeRawTestRequest(
        context: Context,
        method: String,
        urlPath: String,
        requestBodyJson: String? = null,
        baseUrl: String = DEFAULT_BASE_URL
    ): ApiExecutionResult = withContext(Dispatchers.IO) {
        val client = getOkHttpClient(context)
        val fullUrl = if (urlPath.startsWith("http")) urlPath else baseUrl.trimEnd('/') + "/" + urlPath.trimStart('/')

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = if (method == "POST" || method == "PUT") {
            (requestBodyJson ?: "{}").toRequestBody(mediaType)
        } else null

        val requestBuilder = Request.Builder()
            .url(fullUrl)
            .addHeader("Accept", "application/json")
            .method(method, body)

        if (body != null) {
            requestBuilder.addHeader("Content-Type", "application/json")
        }

        val request = requestBuilder.build()
        val curlBuilder = StringBuilder("curl -X $method \"$fullUrl\"")
        if (body != null) {
            curlBuilder.append(" -H \"Content-Type: application/json\" -d '${requestBodyJson ?: "{}"}'")
        }

        val startTime = System.currentTimeMillis()
        try {
            val response = client.newCall(request).execute()
            val duration = System.currentTimeMillis() - startTime
            val bodyString = response.body?.string() ?: ""
            val headersMap = mutableMapOf<String, String>()
            for (i in 0 until response.headers.size) {
                headersMap[response.headers.name(i)] = response.headers.value(i)
            }

            ApiExecutionResult(
                statusCode = response.code,
                statusMessage = response.message,
                durationMs = duration,
                responseBody = bodyString,
                headers = headersMap,
                requestCurl = curlBuilder.toString()
            )
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            ApiExecutionResult(
                statusCode = 500,
                statusMessage = e.javaClass.simpleName,
                durationMs = duration,
                responseBody = "{\n  \"error\": \"${e.localizedMessage ?: "Network or internal error"}\"\n}",
                headers = emptyMap(),
                requestCurl = curlBuilder.toString()
            )
        }
    }
}
