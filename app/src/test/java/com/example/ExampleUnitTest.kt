package com.example

import com.example.data.preferences.SecurityUtils
import com.example.util.CurpHelper
import com.example.util.TimeHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSecurityUtilsSha256Hashing() {
        val hash1 = SecurityUtils.hashSha256("2026")
        val hash2 = SecurityUtils.hashSha256("2026")
        val hashDifferent = SecurityUtils.hashSha256("1234")

        assertEquals("Same PIN must produce identical hash", hash1, hash2)
        assertNotEquals("Different PIN must produce different hash", hash1, hashDifferent)
        assertEquals("Hash length must be 64 hex characters (SHA-256)", 64, hash1.length)
    }

    @Test
    fun testCurpAgeCalculation() {
        // Person born in year 95 (1995)
        val curp95 = "AGOH950514HDFSR01"
        val age95 = CurpHelper.calculateAgeFromCurp(curp95)
        assertNotNull(age95)
        assertTrue("Age for 1995 birth should be around 30-32", age95!! in 30..32)

        // Person born in 2005 (05 with century letter 'A' at index 16)
        val curp05 = "AGOH050514HDFSRSA1"
        val age05 = CurpHelper.calculateAgeFromCurp(curp05)
        assertNotNull(age05)
        assertTrue("Age for 2005 birth should be around 20-22", age05!! in 20..22)

        // Invalid CURP returns null
        val invalid = CurpHelper.calculateAgeFromCurp("INVALID")
        assertEquals(null, invalid)
    }

    @Test
    fun testTimeHelperEstanciaCalculation() {
        // Standard same-day duration: 08:30 to 10:45 -> 135 minutes
        val mins = TimeHelper.calculateMinutesBetween("08:30", "10:45")
        assertEquals(135, mins)

        // Overnight crossing: 23:30 to 01:15 -> 105 minutes
        val minsOvernight = TimeHelper.calculateMinutesBetween("23:30", "01:15")
        assertEquals(105, minsOvernight)

        // Blank times return 0
        assertEquals(0, TimeHelper.calculateMinutesBetween("", "10:00"))
        assertEquals(0, TimeHelper.calculateMinutesBetween("10:00", ""))
    }

    @Test
    fun testDurationFormatting() {
        // 3665 seconds = 1 hour, 1 minute, 5 seconds
        assertEquals("01:01:05", TimeHelper.formatDurationHms(3665L))
        assertEquals("1h 1m 5s", TimeHelper.formatDurationHuman(3665L))

        // 125 seconds = 2 minutes, 5 seconds
        assertEquals("00:02:05", TimeHelper.formatDurationHms(125L))
        assertEquals("2m 5s", TimeHelper.formatDurationHuman(125L))
    }
}
