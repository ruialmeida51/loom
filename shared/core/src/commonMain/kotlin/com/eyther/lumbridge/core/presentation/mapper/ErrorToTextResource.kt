package com.eyther.lumbridge.core.presentation.mapper

import com.eyther.lumbridge.core.domain.errors.AppError
import com.eyther.lumbridge.core.domain.errors.CommonError
import com.eyther.lumbridge.core.generated.resources.Res
import com.eyther.lumbridge.core.generated.resources.error_access_denied
import com.eyther.lumbridge.core.generated.resources.error_no_connection
import com.eyther.lumbridge.core.generated.resources.error_not_authenticated
import com.eyther.lumbridge.core.generated.resources.error_not_found
import com.eyther.lumbridge.core.generated.resources.error_service_unavailable
import com.eyther.lumbridge.core.generated.resources.ui_something_went_wrong_please_try_again
import com.eyther.lumbridge.core.presentation.resources.TextResource

fun AppError.toTextResource(): TextResource = TextResource.Resource(
    res = when (this) {
        is CommonError.NoConnection -> Res.string.error_no_connection
        is CommonError.NotAuthenticated -> Res.string.error_not_authenticated
        is CommonError.AccessDenied -> Res.string.error_access_denied
        is CommonError.NotFound -> Res.string.error_not_found
        is CommonError.ServiceUnavailable -> Res.string.error_service_unavailable
        else -> Res.string.ui_something_went_wrong_please_try_again
    },
)
