package com.simplelunar.calendar.data

object LunarNames {
    private val months = listOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "臘月",
    )

    fun monthName(month: Int): String = months.getOrElse(month - 1) { "${month}月" }

    fun dayName(day: Int): String = when (day) {
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
}
