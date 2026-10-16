package com.simplelunar.calendar.ui.theme

import androidx.compose.ui.graphics.Color
import com.simplelunar.calendar.data.ThemeChoice
import com.simplelunar.calendar.data.ThemeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AppThemeTest {
    @Test
    fun presetsResolveToDistinctColors() {
        val colors = ThemeChoice.entries
            .filter { it != ThemeChoice.CUSTOM }
            .map { resolvePrimary(ThemeSettings(colorChoice = it)) }

        assertEquals(6, colors.distinct().size)
    }

    @Test
    fun customHueChangesResolvedColor() {
        val red = resolvePrimary(ThemeSettings(colorChoice = ThemeChoice.CUSTOM, customHue = 0f))
        val blue = resolvePrimary(ThemeSettings(colorChoice = ThemeChoice.CUSTOM, customHue = 240f))

        assertNotEquals(red, blue)
        assertEquals(Color.White, selectedContentColor(blue))
    }
}
