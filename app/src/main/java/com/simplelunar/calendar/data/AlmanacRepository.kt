package com.simplelunar.calendar.data

import android.icu.util.ChineseCalendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class AlmanacDay(
    val date: LocalDate,
    val lunarDate: String,
    val lunarMonthLabel: String,
    val lunarDayLabel: String,
    val festivals: List<String>,
    val solarTerm: String?,
    val deityBirthdays: List<String>,
)

data class CalendarCell(
    val day: AlmanacDay,
    val inMonth: Boolean,
    val termOnDay: String?,
)

data class CalendarDate(
    val date: LocalDate,
    val inMonth: Boolean,
    val termOnDay: String?,
)

data class CalendarMonth(
    val year: Int,
    val month: Int,
    val weeks: List<List<CalendarCell>>,
)

object AlmanacRepository {
    fun day(date: LocalDate): AlmanacDay {
        val lunar = lunarParts(date)
        return AlmanacDay(
            date = date,
            lunarDate = lunar.displayName,
            lunarMonthLabel = lunar.monthLabel,
            lunarDayLabel = lunar.dayLabel,
            festivals = festivals(date, lunar),
            solarTerm = SolarTerms.currentTerm(date),
            deityBirthdays = DeityBirthdays
                .forLunarDay(lunar.month, lunar.day, lunar.isLeapMonth)
                .distinct(),
        )
    }

    fun month(yearMonth: YearMonth): CalendarMonth {
        val weeks = monthDates(yearMonth).map { week ->
            week.map { cell ->
                CalendarCell(day = day(cell.date), inMonth = cell.inMonth, termOnDay = cell.termOnDay)
            }
        }
        return CalendarMonth(yearMonth.year, yearMonth.monthValue, weeks)
    }

    fun monthDates(yearMonth: YearMonth): List<List<CalendarDate>> {
        val terms = buildMap {
            putAll(SolarTerms.termsInYear(yearMonth.year - 1))
            putAll(SolarTerms.termsInYear(yearMonth.year))
            putAll(SolarTerms.termsInYear(yearMonth.year + 1))
        }
        val firstOfMonth = yearMonth.atDay(1)
        // 週日起算：週日 = 0、週一 = 1 … 週六 = 6。
        var cursor = firstOfMonth.minusDays((firstOfMonth.dayOfWeek.value % 7).toLong())
        val weeks = ArrayList<List<CalendarDate>>(6)
        repeat(6) {
            val week = ArrayList<CalendarDate>(7)
            repeat(7) {
                week += CalendarDate(
                    date = cursor,
                    inMonth = YearMonth.from(cursor) == yearMonth,
                    termOnDay = terms[cursor],
                )
                cursor = cursor.plusDays(1)
            }
            weeks += week
        }
        return weeks
    }

    private data class LunarParts(
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean,
        val monthLabel: String,
        val dayLabel: String,
        val displayName: String,
    )

    private fun lunarParts(date: LocalDate): LunarParts {
        val calendar = ChineseCalendar().apply {
            timeInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val month = calendar.get(ChineseCalendar.MONTH) + 1
        val day = calendar.get(ChineseCalendar.DAY_OF_MONTH)
        val isLeap = calendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        val monthLabel = "${if (isLeap) "閏" else ""}${LunarNames.monthName(month)}"
        val dayLabel = LunarNames.dayName(day)
        return LunarParts(month, day, isLeap, monthLabel, dayLabel, "$monthLabel$dayLabel")
    }

    private fun festivals(date: LocalDate, lunar: LunarParts): List<String> {
        val result = mutableListOf<String>()
        when (date.monthValue to date.dayOfMonth) {
            1 to 1 -> result += "元旦"
            2 to 28 -> result += "和平紀念日"
            4 to 4 -> result += "兒童節"
            10 to 10 -> result += "國慶日"
        }
        if (!lunar.isLeapMonth) {
            result += FestivalTable.forLunarDay(lunar.month, lunar.day, lunar.isLeapMonth)
            when {
                isLunarNewYear(date.plusDays(1)) -> result += "除夕"
                isLunarNewYear(date.plusDays(2)) -> result += "小年夜"
            }
        }
        return result.distinct()
    }

    private fun isLunarNewYear(date: LocalDate): Boolean {
        val lunar = lunarParts(date)
        return lunar.month == 1 && lunar.day == 1 && !lunar.isLeapMonth
    }
}
