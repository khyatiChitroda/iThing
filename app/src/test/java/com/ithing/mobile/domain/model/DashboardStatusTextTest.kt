package com.ithing.mobile.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DashboardStatusTextTest {
    private val source = DashboardWidgetSource(
        valueInputMode = "value",
        statusTextValues = mapOf("1" to "Text Widget Text 1", "2" to "Text Widget Text 2")
    )

    @Test
    fun resolvesNumericTelemetryAgainstStringKeys() {
        assertEquals("Text Widget Text 1", source.statusTextFor(1.0))
        assertEquals("Text Widget Text 2", source.statusTextFor(2.0))
    }

    @Test
    fun doesNotRoundUnknownValuesOrInventMissingText() {
        assertNull(source.statusTextFor(1.5))
        assertNull(source.statusTextFor(9.0))
        assertNull(source.statusTextFor(null))
        assertNull(source.statusTextFor(Double.NaN))
    }

    @Test
    fun bitModeUsesOnlyTheSelectedBit() {
        val bitSource = source.copy(valueInputMode = "bit", bitSelection = 2)
        assertEquals("Text Widget Text 1", bitSource.statusTextFor(4.0))
        assertNull(bitSource.statusTextFor(2.0))
        assertNull(bitSource.copy(bitSelection = 64).statusTextFor(4.0))
    }
}
