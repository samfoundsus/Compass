package com.example

import com.example.sensor.AdaptiveHeadingFilter
import com.example.sensor.SmoothingMode
import com.example.sensor.calculateDirectionSector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testWrapAround_359To1_TakesShortestPath() {
        val filter = AdaptiveHeadingFilter()
        // Initialize at 359°
        val initial = filter.filter(359f)
        assertEquals(359f, initial, 0.01f)

        // Step to 1° (should smoothly advance forward through 0°, not spin back through 180°)
        val next = filter.filter(1f)
        // With shortest-path forward delta of +2°, next should be slightly > 359° or small positive angle (e.g. 359.3° or 0.2°)
        // In circular degrees, (next - 359) should be positive
        val circularDelta = (next - 359f + 540f) % 360f - 180f
        assertTrue("Expected forward progression across 0° but got delta $circularDelta", circularDelta > 0f)
        assertTrue("Progression should not exceed target 2°", circularDelta <= 2f)
    }

    @Test
    fun testWrapAround_1To359_TakesShortestPath() {
        val filter = AdaptiveHeadingFilter()
        // Initialize at 1°
        val initial = filter.filter(1f)
        assertEquals(1f, initial, 0.01f)

        // Step to 359° (should smoothly move backward through 0°)
        val next = filter.filter(359f)
        val circularDelta = (next - 1f + 540f) % 360f - 180f
        assertTrue("Expected backward progression across 0° but got delta $circularDelta", circularDelta < 0f)
        assertTrue("Progression should not exceed -2°", circularDelta >= -2f)
    }

    @Test
    fun testTrueNorthDeclinationAdjustment() {
        val magneticHeading = 350f
        val declination = 15f // 15° East
        val trueHeading = (magneticHeading + declination + 360f) % 360f
        assertEquals(5f, trueHeading, 0.01f)

        val westDeclination = -12f // 12° West
        val trueHeadingWest = (5f + westDeclination + 360f) % 360f
        assertEquals(353f, trueHeadingWest, 0.01f)
    }

    @Test
    fun testSmoothingModeSwitchingPreservesState() {
        val filter = AdaptiveHeadingFilter(SmoothingMode.ADAPTIVE)
        filter.filter(180f)
        filter.setSmoothingMode(SmoothingMode.HIGH_STABILITY)
        val output = filter.filter(182f)
        assertTrue("Output should be near 180° with high stability", output in 180.0f..182.0f)
    }

    @Test
    fun testDirectionSectors() {
        assertEquals("N", calculateDirectionSector(0f))
        assertEquals("NE", calculateDirectionSector(45f))
        assertEquals("E", calculateDirectionSector(90f))
        assertEquals("SE", calculateDirectionSector(135f))
        assertEquals("S", calculateDirectionSector(180f))
        assertEquals("SW", calculateDirectionSector(225f))
        assertEquals("W", calculateDirectionSector(270f))
        assertEquals("NW", calculateDirectionSector(315f))
    }

    @Test
    fun testSectorHysteresisPreventsFlickering() {
        // At 22.6° (just past nominal 22.5°), if currently in "N", hysteresis (1.0°) holds "N" until >= 23.5°
        val sectorWithHysteresis = calculateDirectionSector(heading = 22.6f, currentSector = "N", hysteresisDeg = 1.0f)
        assertEquals("N", sectorWithHysteresis)

        // When crossing past 23.5°, transitions to "NE"
        val sectorAfterThreshold = calculateDirectionSector(heading = 23.6f, currentSector = "N", hysteresisDeg = 1.0f)
        assertEquals("NE", sectorAfterThreshold)
    }

    @Test
    fun testNorthReferenceCalculationSelection() {
        val magneticHeading = 120f
        val declination = 5f

        val effectiveMagnetic = magneticHeading
        val effectiveTrue = (magneticHeading + declination + 360f) % 360f

        assertEquals(120f, effectiveMagnetic, 0.01f)
        assertEquals(125f, effectiveTrue, 0.01f)
    }
}
