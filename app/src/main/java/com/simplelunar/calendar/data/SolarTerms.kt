package com.simplelunar.calendar.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.floor
import kotlin.math.sin

object SolarTerms {
    private val names = listOf(
        "春分", "清明", "穀雨", "立夏", "小滿", "芒種",
        "夏至", "小暑", "大暑", "立秋", "處暑", "白露",
        "秋分", "寒露", "霜降", "立冬", "小雪", "大雪",
        "冬至", "小寒", "大寒", "立春", "雨水", "驚蟄",
    )

    private val taipei: ZoneId = ZoneId.of("Asia/Taipei")

    fun currentTerm(date: LocalDate): String? {
        val candidates = ArrayList<Pair<LocalDate, String>>(names.size * 2)
        for (year in intArrayOf(date.year - 1, date.year)) {
            for (index in names.indices) {
                val termDate = jdToLocalDate(crossing(year, index * 15.0))
                candidates += termDate to names[index]
            }
        }
        return candidates
            .filter { !it.first.isAfter(date) }
            .maxByOrNull { it.first }
            ?.second
    }

    private fun crossing(year: Int, targetLongitude: Double): Double {
        var jd = initialGuess(year, targetLongitude)
        repeat(12) {
            val difference = normalize(sunApparentLongitude(jd) - targetLongitude + 180.0) - 180.0
            jd -= difference / MEAN_MOTION
        }
        return jd
    }

    private fun initialGuess(year: Int, targetLongitude: Double): Double {
        val start = julianDay(year, 1, 1)
        val startLongitude = sunApparentLongitude(start)
        return start + normalize(targetLongitude - startLongitude) / MEAN_MOTION
    }

    private fun sunApparentLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val meanLongitude = 280.46646 + 36000.76983 * t + 0.0003032 * t * t
        val meanAnomaly = Math.toRadians(normalize(357.52911 + 35999.05029 * t - 0.0001537 * t * t))
        val center = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(meanAnomaly) +
            (0.019993 - 0.000101 * t) * sin(2.0 * meanAnomaly) +
            0.000289 * sin(3.0 * meanAnomaly)
        val trueLongitude = meanLongitude + center
        val omega = Math.toRadians(125.04 - 1934.136 * t)
        return normalize(trueLongitude - 0.00569 - 0.00478 * sin(omega))
    }

    private fun julianDay(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = y / 100
        val b = 2 - a + a / 4
        return floor(365.25 * (y + 4716)).toDouble() +
            floor(30.6001 * (m + 1)) +
            day + b - 1524.5
    }

    private fun jdToLocalDate(jd: Double): LocalDate {
        val epochMillis = ((jd - 2440587.5) * MILLIS_PER_DAY).toLong()
        return Instant.ofEpochMilli(epochMillis).atZone(taipei).toLocalDate()
    }

    private fun normalize(degrees: Double): Double {
        val remainder = degrees % 360.0
        return if (remainder < 0) remainder + 360.0 else remainder
    }

    private const val MEAN_MOTION = 0.9856473
    private const val MILLIS_PER_DAY = 86_400_000.0
}
