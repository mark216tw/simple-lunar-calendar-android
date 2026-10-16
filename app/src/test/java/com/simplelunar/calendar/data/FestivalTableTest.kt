package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FestivalTableTest {
    @Test
    fun springFestivalIsOnLunarJanuaryFirst() {
        val items = FestivalTable.forLunarDay(month = 1, day = 1, isLeap = false)

        assertTrue(items.contains("春節"))
    }

    @Test
    fun lanternFestivalIsOnLunarJanuaryFifteenth() {
        val items = FestivalTable.forLunarDay(month = 1, day = 15, isLeap = false)

        assertTrue(items.contains("元宵節"))
    }

    @Test
    fun dragonBoatFestivalIsOnLunarMayFifth() {
        val items = FestivalTable.forLunarDay(month = 5, day = 5, isLeap = false)

        assertTrue(items.contains("端午節"))
    }

    @Test
    fun labaFestivalIsOnLunarDecemberEighth() {
        val items = FestivalTable.forLunarDay(month = 12, day = 8, isLeap = false)

        assertTrue(items.contains("臘八節"))
    }

    @Test
    fun leapMonthHasNoFestivals() {
        val items = FestivalTable.forLunarDay(month = 1, day = 1, isLeap = true)

        assertTrue(items.isEmpty())
    }

    @Test
    fun unknownLunarDayReturnsEmptyList() {
        val items = FestivalTable.forLunarDay(month = 12, day = 31, isLeap = false)

        assertEquals(emptyList<String>(), items)
    }
}
