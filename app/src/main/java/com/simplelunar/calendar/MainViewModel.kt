package com.simplelunar.calendar

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.glance.appwidget.updateAll
import com.simplelunar.calendar.data.DisplayMode
import com.simplelunar.calendar.data.ThemeChoice
import com.simplelunar.calendar.data.ThemePreferences
import com.simplelunar.calendar.data.ThemeSettings
import com.simplelunar.calendar.widget.LunarWidget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = ThemePreferences(application)
    private val _settings = MutableStateFlow(ThemeSettings())
    val settings: StateFlow<ThemeSettings> = _settings.asStateFlow()
    private var saveJob: Job? = null

    init {
        viewModelScope.launch {
            preferences.settings.collect { _settings.value = it }
        }
    }

    fun selectColor(choice: ThemeChoice) = update(
        _settings.value.copy(
            colorChoice = choice,
            customHue = if (choice == ThemeChoice.CUSTOM) _settings.value.customHue else choice.hue,
        ),
    )

    fun setCustomHue(hue: Float) = update(
        _settings.value.copy(colorChoice = ThemeChoice.CUSTOM, customHue = hue),
        debounce = true,
    )

    fun selectDisplayMode(mode: DisplayMode) = update(_settings.value.copy(displayMode = mode))

    private fun update(settings: ThemeSettings, debounce: Boolean = false) {
        _settings.value = settings
        saveJob?.cancel()
        saveJob = viewModelScope.launch {
            if (debounce) delay(150)
            preferences.save(settings)
            LunarWidget().updateAll(getApplication())
        }
    }
}
