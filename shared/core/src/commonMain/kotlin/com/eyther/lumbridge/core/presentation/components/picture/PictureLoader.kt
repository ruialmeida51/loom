package com.eyther.lumbridge.core.presentation.components.picture

import androidx.compose.runtime.staticCompositionLocalOf
import com.eyther.lumbridge.core.domain.media.PictureLoader

/** Provided at the app root; the default shows initials only (previews and tests). */
val LocalPictureLoader = staticCompositionLocalOf<PictureLoader> { PictureLoader { null } }
