package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SolarTermsTest {
    @Test
    fun startOfSpringFallsOnExpectedDate() {
        assertEquals("立春", SolarTerms.currentTerm(LocalDate.of(2026, 2, 4)))
    }

    @Test
    fun summerSolsticeFallsOnExpectedDate() {
        assertEquals("夏至", SolarTerms.currentTerm(LocalDate.of(2026, 6, 21)))
    }

    @Test
    fun autumnEquinoxFallsOnExpectedDate() {
        assertEquals("秋分", SolarTerms.currentTerm(LocalDate.of(2026, 9, 23)))
    }

    @Test
    fun winterSolsticeFallsOnExpectedDate() {
        assertEquals("冬至", SolarTerms.currentTerm(LocalDate.of(2026, 12, 22)))
    }

    @Test
    fun dayBeforeStartOfSpringKeepsPreviousTerm() {
        assertEquals("大寒", SolarTerms.currentTerm(LocalDate.of(2026, 2, 3)))
    }

    @Test
    fun termPersistsAfterItsStartDate() {
        assertEquals("立春", SolarTerms.currentTerm(LocalDate.of(2026, 2, 5)))
    }

    @Test
    fun earlyJanuaryKeepsWinterSolsticeOfPreviousYear() {
        assertEquals("冬至", SolarTerms.currentTerm(LocalDate.of(2026, 1, 1)))
    }
}
