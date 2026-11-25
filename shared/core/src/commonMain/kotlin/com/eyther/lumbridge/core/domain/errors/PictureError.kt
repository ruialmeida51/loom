package com.eyther.lumbridge.core.domain.errors

/** Profile and household pictures share one pipeline, so their failures live with it in core. */
sealed class PictureError(cause: Throwable? = null) : AppError(cause) {

    /** The chosen file is not an image the device can read. */
    class Unreadable(cause: Throwable? = null) : PictureError(cause)

    /** Even after resizing, the picture does not fit the upload limit. */
    class TooLarge(cause: Throwable? = null) : PictureError(cause)

    /** The server refused the picture (not uploaded, wrong owner or path). */
    class Rejected(cause: Throwable? = null) : PictureError(cause)
}
