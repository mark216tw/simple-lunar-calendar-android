package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class AlmanacRepositoryMonthTest {
    @Test
    fun monthHasSixWeeksOfSevenDays() {
        val weeks = AlmanacRepository.monthDates(YearMonth.of(2026, 10))

        assertEquals(6, weeks.size)
        assertTrue(weeks.all { it.size == 7 })
    }

    @Test
    fun gridStartsOnSundayBeforeFirstDay() {
        val first = AlmanacRepository.monthDates(YearMonth.of(2026, 10)).first().first().date

        assertEquals(LocalDate.of(2026, 9, 27), first)
        assertEquals(DayOfWeek.SUNDAY, first.dayOfWeek)
    }

    @Test
    fun inMonthFlagDistinguishesPaddingDays() {
        val cells = AlmanacRepository.monthDates(YearMonth.of(2026, 10)).flatten()

        assertTrue(cells.first { it.date == LocalDate.of(2026, 10, 1) }.inMonth)
        assertFalse(cells.first { it.date == LocalDate.of(2026, 9, 30) }.inMonth)
    }

    @Test
    fun everyDayOfMonthIsPresentExactlyOnce() {
        val days = AlmanacRepository.monthDates(YearMonth.of(2026, 10)).flatten()
            .filter { it.inMonth }
            .map { it.date.dayOfMonth }
            .sorted()

        assertEquals((1..31).toList(), days)
    }

    @Test
    fun solarTermIsMarkedOnlyOnItsStartDate() {
        val cells = AlmanacRepository.monthDates(YearMonth.of(2026, 2)).flatten()

        assertEquals("立春", cells.first { it.date == LocalDate.of(2026, 2, 4) }.termOnDay)
        assertNull(cells.first { it.date == LocalDate.of(2026, 2, 5) }.termOnDay)
    }

    @Test
    fun adjacentMonthPaddingKeepsContiguousDates() {
        val cells = AlmanacRepository.monthDates(YearMonth.of(2026, 10)).flatten()

        cells.zipWithNext { a, b -> assertEquals(a.date.plusDays(1), b.date) }
    }
}
