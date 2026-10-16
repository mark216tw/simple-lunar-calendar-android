package com.simplelunar.calendar.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.simplelunar.calendar.MainActivity
import com.simplelunar.calendar.data.AlmanacDay
import com.simplelunar.calendar.data.AlmanacRepository
import com.simplelunar.calendar.data.DisplayMode
import com.simplelunar.calendar.data.ThemePreferences
import com.simplelunar.calendar.ui.theme.resolvePrimary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class LunarWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = ThemePreferences(context).settings.first()
        val isSystemDark = context.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (settings.displayMode) {
            DisplayMode.SYSTEM -> isSystemDark
            DisplayMode.LIGHT -> false
            DisplayMode.DARK -> true
        }
        val primary = resolvePrimary(settings)
        val background = if (isDark) Color(0xFF211B18) else Color(0xFFFFFCFA)
        val content = if (isDark) Color(0xFFF4EDEA) else Color(0xFF271A16)
        provideContent {
            WidgetContent(
                day = AlmanacRepository.day(LocalDate.now()),
                primary = primary,
                background = background,
                content = content,
            )
        }
    }
}

class LunarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LunarWidget()
}

class DateChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val supportedActions = setOf(
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
        )
        if (intent.action !in supportedActions) return
        val result = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                LunarWidget().updateAll(context)
            } finally {
                result.finish()
            }
        }
    }
}

private val widgetDateFormatter = DateTimeFormatter.ofPattern("M 月 d 日 E", Locale.TAIWAN)

@androidx.compose.runtime.Composable
private fun WidgetContent(
    day: AlmanacDay,
    primary: Color,
    background: Color,
    content: Color,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ColorProvider(background))
            .cornerRadius(22.dp)
            .clickable(actionStartActivity<MainActivity>())
            .padding(16.dp),
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                "今日農民曆",
                style = TextStyle(color = ColorProvider(primary), fontWeight = FontWeight.Bold),
            )
            Spacer(GlanceModifier.defaultWeight())
            Text(day.date.format(widgetDateFormatter), style = TextStyle(color = ColorProvider(content)))
        }
        Spacer(GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(
                "農曆 ${day.lunarDate}",
                maxLines = 1,
                style = TextStyle(color = ColorProvider(content), fontWeight = FontWeight.Bold),
            )
            day.solarTerm?.let { solarTerm ->
                Spacer(GlanceModifier.defaultWeight())
                Text(
                    "節氣 $solarTerm",
                    maxLines = 1,
                    style = TextStyle(color = ColorProvider(primary), fontWeight = FontWeight.Bold),
                )
            }
        }
        Spacer(GlanceModifier.height(10.dp))
        Text(
            "神明生日",
            style = TextStyle(color = ColorProvider(primary), fontWeight = FontWeight.Bold),
        )
        Spacer(GlanceModifier.height(2.dp))
        Text(
            widgetDeitySummary(day.deityBirthdays),
            maxLines = 2,
            style = TextStyle(color = ColorProvider(content)),
        )
    }
}

private fun widgetDeitySummary(items: List<String>): String {
    if (items.isEmpty()) return "今日無神明生日"
    val summary = items.take(3).joinToString("、")
    return if (items.size > 3) "$summary 等 ${items.size} 則" else summary
}
