package com.eyther.lumbridge.core.navigation.model

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.navigation3.runtime.NavKey
import kotlin.reflect.KClass

/**
 * Lightweight navigator helper that mutates a [NavigationState]. It contains helper methods for
 * navigating to and from destinations, as well as popping the back stack.
 *
 * @param state the [NavigationState] that this navigator will manipulate.
 */
@Stable
class Navigator(
    val state: NavigationState,
) {
    /**
     * Navigates to the specified [destination].
     *
     * If the destination is a root defined by our backstack keys, it switches to that root and
     * maintains its existing back stack, no deletions, no additions. If the destination is not a
     * root, it pushes the destination onto the current back stack of the active root.
     *
     * @param destination the [NavKey] representing the destination to navigate to.
     */
    fun navigateTo(destination: NavKey) {
        if (state.isRootDestination(destination)) {
            state.activeRoot = destination
        } else {
            state.backStacks[state.activeRoot]?.add(destination)
        }
    }

    /**
     * Navigates to the specified [destination] and pops the current back stack up to that
     * destination.
     *
     * If the destination is a root defined by our backstack keys, it switches to that root and
     * clears its back stack, leaving only the destination as the sole entry. If the destination is
     * not a root, it pops the current back stack of the active root up to that destination,
     * optionally including the destination itself based on the [inclusive] parameter.
     *
     * @param destination the [NavKey] representing the destination to navigate to.
     * @param inclusive if true, the destination itself will be popped from the stack; if false, it
     *   will remain as the last entry in the stack. Defaults to true.
     */
    fun navigateAndPopTo(
        destination: NavKey,
        inclusive: Boolean = true,
    ) {
        // If target is a top-level route, switch stacks. Mimic launchSingleTop behavior.
        if (state.isRootDestination(destination)) {
            // Get the backstack for the existing target destination. If it doesn't exist, throw an
            // error as this should never happen if the navigation state is set up correctly.
            val targetStack =
                state.getBackstackForRoot(destination)
                    ?: error("Back stack for $destination not found")

            // If the active root is already correct, and we only have one entry in the stack, we
            // don't need to do anything, we've popped to the root already. After this line
            // we will clear the stack and add the root for the destination, so we don't want to do
            // that if it's already correct.
            if (state.activeRoot == destination && targetStack.size == 1) return

            // Clear the target stack and add the destination as the only entry. Then set the
            // active root to the destination.
            targetStack.clear()
            targetStack.add(destination)
            state.activeRoot = destination

            return
        }

        // From here onwards, we are navigating to a non-root destination. Get the backstack
        // of whatever our current active root is.
        val currentStack =
            state.getActiveBackStack() ?: error("Back stack for ${state.activeRoot} not found")

        // Where is this destination in the current stack?
        val index = currentStack.lastIndexOf(destination)

        if (index == -1) {
            // We couldn't find the destination in the current stack, so we just pop everything
            // above the root.
            while (currentStack.size > 1) currentStack.removeLastOrNull()
        } else {
            // Destination was found. If inclusive, we want to pop everything including the
            // destination itself, else we keep it in the stack but pop everything else
            // above it.
            val indexOfLastRemainingItem = if (inclusive) index else index + 1
            while (currentStack.size > indexOfLastRemainingItem) currentStack.removeLastOrNull()
        }

        // After popping, if the destination is already the last entry in the stack, we don't need
        // to add it again. Just a minor optimisation to avoid unnecessary stack mutations.
        if (currentStack.lastOrNull() == destination) return

        // Otherwise, add the destination to the current stack.
        currentStack.add(destination)
    }

    /**
     * Replaces the current route with the specified [destination]. It completely wipes the current
     * back stack for the specified destination's root and adds the destination as the only entry.
     * If the destination is a root, it switches to that root and clears its back stack, leaving
     * only the destination as the sole entry.
     *
     * @param destination the [NavKey] representing the destination to replace the current route
     *   with.
     */
    fun resetTo(destination: NavKey) {
        val isRoot = state.isRootDestination(destination)

        val targetStack =
            if (isRoot) {
                state.getBackstackForRoot(destination)
                    ?: error("Back stack for $destination not found")
            } else {
                state.getActiveBackStack()
                    ?: error("Back stack for ${state.activeRoot} doesn't exist")
            }

        // Clear the target stack and add the destination as the only entry.
        targetStack.clear()
        targetStack.add(destination)

        // If it's a root destination, besides clearing, we also have to set the active root to
        // the destination so the screens properly swap backstacks.
        if (isRoot) {
            state.activeRoot = destination
        }
    }

    /**
     * Pops the last entry from the active back stack. A no-op at the stack's own root (a single
     * remaining entry) — this navigator only owns one stack and has no notion of what "leaving" it
     * should mean, so it leaves that decision to whoever is above it rather than emptying the
     * stack, which [NavDisplay] isn't designed to render.
     */
    fun goBack() {
        val currentStack =
            state.getActiveBackStack() ?: error("Back stack for ${state.activeRoot} doesn't exist")

        if (currentStack.size <= 1) return

        currentStack.removeLastOrNull()
    }
}

/**
 * Observes the current visible route in the navigation state.
 *
 * This composable sets up a [LaunchedEffect] that tracks changes to the active route (the top of
 * the current back stack) and invokes [onRouteChanged] whenever the route changes. This is useful
 * for centralizing side effects like updating toolbar visibility based on the current destination.
 *
 * @param onRouteChanged callback invoked with the current [NavKey] whenever the active route
 *   changes.
 */
@Suppress("ComposableNaming")
@Composable
fun Navigator.observeCurrentRoute(
    onRouteChanged: (NavKey) -> Unit,
) {
    LaunchedEffect(state) {
        snapshotFlow {
            val currentStack = state.backStacks[state.activeRoot]
            currentStack?.lastOrNull() ?: state.activeRoot
        }
            .collect { currentRoute ->
                onRouteChanged(currentRoute)
            }
    }
}

/**
 * Creates a navigation metadata map for a nav display.
 *
 * This helper eliminates the need for explicit type declarations and casts when defining transition
 * specs for navigation destinations.
 *
 * @param entries vararg of [Pair] where the key is the destination class and the value is the
 *   metadata map (typically transition specs).
 * @return a map compatible with nav display's metadata parameter.
 */
fun navigationMetadata(
    vararg entries: Pair<KClass<out NavKey>, Map<String, Any>>,
): Map<KClass<out NavKey>, Map<String, Any>> {
    return mapOf(*entries)
}
