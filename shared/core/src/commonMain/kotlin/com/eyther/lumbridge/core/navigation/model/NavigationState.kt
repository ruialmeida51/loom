package com.eyther.lumbridge.core.navigation.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.modules.PolymorphicModuleBuilder
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/**
 * Represents the navigation state of the app, including the back stacks for each top-level route
 * and the currently active route.
 *
 * @property backStacks a map of top-level routes to their corresponding back stacks.
 * @property startRoot the initial top-level route when the app starts.
 * @property root a mutable state representing the currently active top-level route.
 * @property activeRoot the currently active top-level route, derived from [root] at the beginning.
 */
@Stable
data class NavigationState(
    internal val backStacks: Map<NavKey, NavBackStack<NavKey>>,
    internal val startRoot: NavKey,
    internal val root: MutableState<NavKey>,
) {
    // Initialise the activeRoot property with the value of root at the time of creation.
    var activeRoot: NavKey by root

    /**
     * @return the back stack corresponding to the currently active top-level [root], or null if
     *   none exists.
     */
    fun getActiveBackStack(): NavBackStack<NavKey>? {
        return getBackstackForRoot(activeRoot)
    }

    /** @return the back stack corresponding to the specified [root], or null if none exists. */
    fun getBackstackForRoot(root: NavKey): NavBackStack<NavKey>? {
        return backStacks[root]
    }

    /**
     * @return true if the specified [destination] is a top-level [root] defined in [backStacks],
     *   false otherwise.
     */
    fun isRootDestination(destination: NavKey): Boolean {
        return backStacks.containsKey(destination)
    }
}

/**
 * Attempts to create and remember a [NavigationState] instance for the caller.
 *
 * @param startRoot the initial top-level root that will open when this navigation state is created.
 * @param roots the top-level roots that will contain their own backstacks.
 * @param subClasses a lambda that allows you to register polymorphic subclasses of [NavKey] for
 *   serialization.
 */
@Composable
fun rememberNavigationState(
    startRoot: NavKey,
    roots: Set<NavKey> = emptySet(),
    subClasses: PolymorphicModuleBuilder<NavKey>.() -> Unit = {},
): NavigationState {

    // If there are no roots provided, we default to using the startRoot as
    // the only top-level screen to create a backstack for. Otherwise, we wouldn't
    // have any backstacks to navigate through.
    val actualRoots = roots.ifEmpty { mutableSetOf(startRoot) }

    val serializersConfig = SavedStateConfiguration {
        serializersModule = SerializersModule {
            polymorphic(
                baseClass = NavKey::class,
                builderAction = subClasses,
            )
        }
    }

    // We use rememberSerializable to persist the currently active root across configuration
    // changes. Remember both the startRoot and actualRoots as inputs to ensure that if either
    // of them changes, the serialized state is recomputed. This is important for cases where
    // the set of roots might change dynamically based on app state or user actions.
    val serializedRoots =
        rememberSerializable(
            inputs = arrayOf(startRoot, actualRoots),
            configuration = serializersConfig,
            serializer = MutableStateSerializer(PolymorphicSerializer(NavKey::class)),
            init = { mutableStateOf(startRoot) },
        )

    // For each root in actualRoots, we create a corresponding back stack. In the case that
    // we only provide a single root (the startRoot), this will create a back stack for that root.
    // If we provide multiple roots, each will have its own back stack, allowing for independent
    // navigation flows.
    val backStacks = actualRoots.associateWith { key ->
        rememberNavBackStack(
            configuration = serializersConfig,
            elements = arrayOf(key),
        )
    }

    // Remember the NavigationState instance in its entirety.
    return remember(startRoot, actualRoots, serializedRoots) {
        NavigationState(
            startRoot = startRoot,
            root = serializedRoots,
            backStacks = backStacks,
        )
    }
}

/**
 * Converts the [NavigationState] into a list of [NavEntry]s for use with Navigation3's [NavKey].
 * This function retrieves the active back stack and applies decorators to each entry, allowing for
 * state saving and ViewModel scoping.
 *
 * @param entryProvider a lambda that takes a [NavKey] and returns a corresponding [NavEntry].
 * @return a [SnapshotStateList] of decorated [NavEntry]s representing the current navigation state.
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>,
): SnapshotStateList<NavEntry<NavKey>> {
    // Decorate entries for ALL back stacks, not just the active one. This ensures that when we
    // switch between roots, the state of each back stack is preserved.
    val decoratedEntries = backStacks.mapValues { (_, stack) ->
        val decorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator(),
            )

        rememberDecoratedNavEntries(
            backStack = stack,
            entryDecorators = decorators,
            entryProvider = entryProvider,
        )
    }

    // For the currently active root, return its decorated entries as a mutable state list.
    return decoratedEntries[activeRoot]?.toMutableStateList() ?: mutableStateListOf()
}
