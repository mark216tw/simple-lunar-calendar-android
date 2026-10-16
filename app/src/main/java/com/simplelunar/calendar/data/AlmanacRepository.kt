package com.simplelunar.calendar.data

import android.icu.util.ChineseCalendar
import java.time.LocalDate
import java.time.ZoneId

data class AlmanacDay(
    val date: LocalDate,
    val lunarDate: String,
    val festivals: List<String>,
    val solarTerm: String?,
    val deityBirthdays: List<String>,
)

object AlmanacRepository {
    fun day(date: LocalDate): AlmanacDay {
        val lunar = lunarParts(date)
        return AlmanacDay(
            date = date,
            lunarDate = lunar.displayName,
            festivals = festivals(date, lunar),
            solarTerm = SolarTerms.currentTerm(date),
            deityBirthdays = DeityBirthdays
                .forLunarDay(lunar.month, lunar.day, lunar.isLeapMonth)
                .distinct(),
        )
    }

    private data class LunarParts(
        val month: Int,
        val day: Int,
        val isLeapMonth: Boolean,
        val displayName: String,
    )

    private fun lunarParts(date: LocalDate): LunarParts {
        val calendar = ChineseCalendar().apply {
            timeInMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
        val month = calendar.get(ChineseCalendar.MONTH) + 1
        val day = calendar.get(ChineseCalendar.DAY_OF_MONTH)
        val isLeap = calendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        val monthName = listOf(
            "正月", "二月", "三月", "四月", "五月", "六月",
            "七月", "八月", "九月", "十月", "冬月", "臘月",
        )[month - 1]
        val dayName = when (day) {
            10 -> "初十"
            20 -> "二十"
            30 -> "三十"
            else -> {
                val prefix = when (day) {
                    in 1..9 -> "初"
                    in 11..19 -> "十"
                    in 21..29 -> "廿"
                    else -> ""
                }
                val number = listOf("", "一", "二", "三", "四", "五", "六", "七", "八", "九")[day % 10]
                prefix + number
            }
        }
        return LunarParts(month, day, isLeap, "${if (isLeap) "閏" else ""}$monthName$dayName")
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
