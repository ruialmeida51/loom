package com.eyther.lumbridge.core.navigation.model

import androidx.compose.runtime.getValue
import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for app destinations that are also navigation keys used by Navigation3's
 * runtime.
 *
 * Implementing this interface indicates the type can be used as a destination within the app's
 * navigation graph. It delegates to [NavKey] from the Navigation3 runtime which is the actual
 * polymorphic base used by the navigation serialization.
 */
interface NavDestination : NavKey
