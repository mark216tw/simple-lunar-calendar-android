package com.simplelunar.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DeityBirthdaysTest {
    @Test
    fun jadeEmperorBirthdayIsOnLunarJanuaryNinth() {
        val items = DeityBirthdays.forLunarDay(month = 1, day = 9, isLeap = false)

        assertTrue(items.contains("玉皇上帝大天尊聖壽"))
    }

    @Test
    fun mazuBirthdayIsOnLunarMarchTwentyThird() {
        val items = DeityBirthdays.forLunarDay(month = 3, day = 23, isLeap = false)

        assertTrue(items.contains("天上聖母聖誕"))
    }

    @Test
    fun leapMonthHasNoBirthdays() {
        val items = DeityBirthdays.forLunarDay(month = 1, day = 1, isLeap = true)

        assertTrue(items.isEmpty())
    }

    @Test
    fun unknownLunarDayReturnsEmptyList() {
        val items = DeityBirthdays.forLunarDay(month = 12, day = 31, isLeap = false)

        assertEquals(emptyList<String>(), items)
    }

    @Test
    fun allReturnsEntriesSortedByLunarMonthAndDay() {
        val all = DeityBirthdays.all()

        assertTrue(all.isNotEmpty())
        assertEquals(all, all.sortedWith(compareBy({ it.lunarMonth }, { it.lunarDay })))
        assertTrue(all.all { it.names.isNotEmpty() })
    }

    @Test
    fun allContainsJadeEmperorBirthdayEntry() {
        val entry = DeityBirthdays.all().first { it.lunarMonth == 1 && it.lunarDay == 9 }

        assertTrue(entry.names.contains("玉皇上帝大天尊聖壽"))
    }
}
