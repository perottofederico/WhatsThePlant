package com.example.whatstheplant.api.plantid

sealed class ApiResult<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T> (data: T?): ApiResult<T>(data)
    class Error <T> (data: T?, message: String): ApiResult<T>(data, message)
}
