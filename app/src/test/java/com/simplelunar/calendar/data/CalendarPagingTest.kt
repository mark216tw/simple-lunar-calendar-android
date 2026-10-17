package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

class CalendarPagingTest {
    @Test
    fun pageCountCoversEveryMonthInRange() {
        assertEquals((CalendarPaging.MAX_YEAR - CalendarPaging.MIN_YEAR + 1) * 12, CalendarPaging.pageCount)
    }

    @Test
    fun boundariesMapToFirstAndLastPage() {
        assertEquals(0, CalendarPaging.toPage(YearMonth.of(1900, 1)))
        assertEquals(CalendarPaging.pageCount - 1, CalendarPaging.toPage(YearMonth.of(2100, 12)))
    }

    @Test
    fun toPageAndToMonthRoundTripWithinRange() {
        val months = listOf(
            YearMonth.of(1900, 1),
            YearMonth.of(2026, 10),
            YearMonth.of(2100, 12),
        )
        months.forEach { month ->
            assertEquals(month, CalendarPaging.toMonth(CalendarPaging.toPage(month)))
        }
    }

    @Test
    fun outOfRangeValuesAreClamped() {
        assertEquals(0, CalendarPaging.toPage(YearMonth.of(1800, 6)))
        assertEquals(CalendarPaging.pageCount - 1, CalendarPaging.toPage(YearMonth.of(2200, 6)))
        assertEquals(YearMonth.of(1900, 1), CalendarPaging.toMonth(-5))
        assertEquals(YearMonth.of(2100, 12), CalendarPaging.toMonth(CalendarPaging.pageCount + 5))
    }

    @Test
    fun clampYearKeepsYearsInsideSupportedRange() {
        assertEquals(1900, CalendarPaging.clampYear(1800))
        assertEquals(2026, CalendarPaging.clampYear(2026))
        assertEquals(2100, CalendarPaging.clampYear(2200))
    }
}
