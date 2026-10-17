package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class SolarTermsTermOnTest {
    @Test
    fun termOnReturnsNameOnlyOnStartDate() {
        assertEquals("立春", SolarTerms.termOn(LocalDate.of(2026, 2, 4)))
        assertNull(SolarTerms.termOn(LocalDate.of(2026, 2, 5)))
    }

    @Test
    fun termsInYearContainsTwentyFourTerms() {
        assertEquals(24, SolarTerms.termsInYear(2026).size)
    }

    @Test
    fun termOnMatchesTermDatesForEveryTerm() {
        SolarTerms.termsInYear(2026).forEach { (date, name) ->
            assertEquals(name, SolarTerms.termOn(date))
            assertNull(SolarTerms.termOn(date.minusDays(1)))
        }
    }

    @Test
    fun termsInYearValuesAreAllKnownTerms() {
        val expected = setOf(
            "春分", "清明", "穀雨", "立夏", "小滿", "芒種",
            "夏至", "小暑", "大暑", "立秋", "處暑", "白露",
            "秋分", "寒露", "霜降", "立冬", "小雪", "大雪",
            "冬至", "小寒", "大寒", "立春", "雨水", "驚蟄",
        )
        assertTrue(SolarTerms.termsInYear(2026).values.containsAll(expected))
    }
}
