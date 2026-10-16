package com.simplelunar.calendar.data

object FestivalTable {
    private val data: Map<Int, Map<Int, List<String>>> = mapOf(
        1 to mapOf(
            1 to listOf("春節"),
            9 to listOf("天公生"),
            15 to listOf("元宵節"),
        ),
        2 to mapOf(
            2 to listOf("土地公生（頭牙）"),
        ),
        3 to mapOf(
            3 to listOf("玄天上帝誕辰", "上巳節"),
            23 to listOf("媽祖誕辰"),
        ),
        4 to mapOf(
            8 to listOf("浴佛節"),
        ),
        5 to mapOf(
            5 to listOf("端午節"),
        ),
        7 to mapOf(
            7 to listOf("七夕"),
            15 to listOf("中元節"),
        ),
        8 to mapOf(
            15 to listOf("中秋節"),
        ),
        9 to mapOf(
            9 to listOf("重陽節"),
        ),
        12 to mapOf(
            8 to listOf("臘八節"),
            24 to listOf("送神"),
        ),
    )

    fun forLunarDay(month: Int, day: Int, isLeap: Boolean): List<String> {
        if (isLeap) return emptyList()
        return data[month]?.get(day).orEmpty()
    }
}
