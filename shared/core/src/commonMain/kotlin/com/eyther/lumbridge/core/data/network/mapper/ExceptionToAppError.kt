package com.eyther.lumbridge.core.data.network.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.domain.errors.PictureError
import io.github.jan.supabase.auth.exception.SessionRequiredException
import io.github.jan.supabase.auth.exception.TokenExpiredException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Exception.toAppError(): AppError =
    when (this) {
        is AppError -> this
        is SessionRequiredException,
        is TokenExpiredException -> CommonError.NotAuthenticated(this)
        is HttpRequestException,
        is HttpRequestTimeoutException,
        is IOException -> CommonError.NoConnection(this)
        is SerializationException -> CommonError.InvalidResponse(this)
        is PostgrestRestException ->
            when {
                code == "42501" -> CommonError.AccessDenied(this)
                code == "PGRST301" || code == "PGRST302" || code == "PGRST303" ->
                    CommonError.NotAuthenticated(this)
                code == "P0001" && error == "Please sign in." -> CommonError.NotAuthenticated(this)
                code == "P0001" && error == "Household access denied." ->
                    CommonError.AccessDenied(this)
                code == "P0001" && error == "pictures.invalid_picture" -> PictureError.Rejected(this)
                code == "P0001" && error == "pictures.access_denied" ->
                    CommonError.AccessDenied(this)
                else -> toHttpError()
            }
        is RestException -> toHttpError()
        else -> CommonError.Unknown(this)
    }

private fun RestException.toHttpError(): AppError =
    when (statusCode) {
        401 -> CommonError.NotAuthenticated(this)
        403 -> CommonError.AccessDenied(this)
        404 -> CommonError.NotFound(this)
        in 500..599 -> CommonError.ServiceUnavailable(this)
        else -> CommonError.Unknown(this)
    }
