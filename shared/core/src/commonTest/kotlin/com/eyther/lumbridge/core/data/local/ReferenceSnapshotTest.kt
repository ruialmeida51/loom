package com.eyther.lumbridge.core.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TestTimeSource

@OptIn(ExperimentalTime::class)
class ReferenceSnapshotTest {
    @Test
    fun reuseExpiresAndNeverCrossesUserOrHouseholdScope() {
        val time = TestTimeSource()
        val snapshot = ReferenceSnapshot<String>(timeSource = time)
        snapshot.put(key = "alice:home", value = "members")
        assertEquals("members", snapshot.get(key = "alice:home"))
        assertNull(snapshot.get(key = "bob:home"))
        assertNull(snapshot.get(key = "alice:trip"))
        time += 30.seconds
        assertNull(snapshot.get(key = "alice:home"))
        snapshot.put(key = "alice:home", value = "new members")
        snapshot.clear()
        assertNull(snapshot.get(key = "alice:home"))
    }
}
