package com.simplelunar.calendar.data

import java.time.YearMonth

object CalendarPaging {
    const val MIN_YEAR = 1900
    const val MAX_YEAR = 2100

    val baseMonth: YearMonth = YearMonth.of(MIN_YEAR, 1)
    val pageCount: Int = (MAX_YEAR - MIN_YEAR + 1) * 12

    fun toPage(yearMonth: YearMonth): Int =
        ((yearMonth.year - baseMonth.year) * 12 + (yearMonth.monthValue - 1))
            .coerceIn(0, pageCount - 1)

    fun toMonth(page: Int): YearMonth =
        baseMonth.plusMonths(page.coerceIn(0, pageCount - 1).toLong())

    fun clampYear(year: Int): Int = year.coerceIn(MIN_YEAR, MAX_YEAR)
}
