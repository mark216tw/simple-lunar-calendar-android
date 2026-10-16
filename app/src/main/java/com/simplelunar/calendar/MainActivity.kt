package com.simplelunar.calendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SettingsBrightness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.simplelunar.calendar.data.AlmanacDay
import com.simplelunar.calendar.data.AlmanacRepository
import com.simplelunar.calendar.data.DisplayMode
import com.simplelunar.calendar.data.ThemeChoice
import com.simplelunar.calendar.data.ThemeSettings
import com.simplelunar.calendar.ui.theme.SimpleLunarTheme
import com.simplelunar.calendar.ui.theme.resolvePrimary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val settings by viewModel.settings.collectAsStateWithLifecycle()
            SimpleLunarTheme(settings) {
                SimpleLunarApp(settings, viewModel)
            }
        }
    }
}

private enum class Screen { TODAY, SETTINGS }

@Composable
private fun SimpleLunarApp(settings: ThemeSettings, viewModel: MainViewModel) {
    var screen by rememberSaveable { mutableStateOf(Screen.TODAY) }
    BackHandler(enabled = screen == Screen.SETTINGS) { screen = Screen.TODAY }
    when (screen) {
        Screen.TODAY -> TodayScreen(onOpenSettings = { screen = Screen.SETTINGS })
        Screen.SETTINGS -> SettingsScreen(
            settings = settings,
            onBack = { screen = Screen.TODAY },
            onColorSelected = viewModel::selectColor,
            onHueChanged = viewModel::setCustomHue,
            onDisplayModeSelected = viewModel::selectDisplayMode,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodayScreen(onOpenSettings: () -> Unit) {
    var selectedDateText by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val selectedDate = LocalDate.parse(selectedDateText)
    val today = LocalDate.now()
    val day = AlmanacRepository.day(selectedDate)
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("簡單農民曆", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Rounded.Settings, contentDescription = "設定")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DateHero(
                day = day,
                isToday = selectedDate == today,
                onPickDate = { showDatePicker = true },
                onPrevious = { selectedDateText = selectedDate.minusDays(1).toString() },
                onToday = { selectedDateText = today.toString() },
                onNext = { selectedDateText = selectedDate.plusDays(1).toString() },
            )
            Spacer(Modifier.height(18.dp))
            OccasionCard(day)
            Spacer(Modifier.height(14.dp))
            DeityBirthdayCard(day.deityBirthdays)
            Spacer(Modifier.height(18.dp))
        }
    }

    if (showDatePicker) {
        val initialMillis = selectedDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        selectedDateText = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    showDatePicker = false
                }) { Text("確定", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } },
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
private fun DateHero(
    day: AlmanacDay,
    isToday: Boolean,
    onPickDate: () -> Unit,
    onPrevious: () -> Unit,
    onToday: () -> Unit,
    onNext: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofPattern("yyyy 年 M 月 d 日 EEEE", Locale.TAIWAN) }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onPickDate,
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text(day.date.format(formatter), fontWeight = FontWeight.Bold)
            }
        }
        Text(
            day.date.dayOfMonth.toString(),
            fontSize = 68.sp,
            lineHeight = 76.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
        )
        Text("農曆 ${day.lunarDate}", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPrevious) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = null)
                Text("前一天", fontWeight = FontWeight.Bold)
            }
            if (!isToday) {
                TextButton(onClick = onToday) { Text("回到今天", fontWeight = FontWeight.Bold) }
            }
            TextButton(onClick = onNext) {
                Text("後一天", fontWeight = FontWeight.Bold)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
private fun OccasionCard(day: AlmanacDay) {
    val hasOccasion = day.festivals.isNotEmpty() || day.solarTerm != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        if (hasOccasion) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                day.solarTerm?.let { LabelValue("節氣", it) }
                if (day.festivals.isNotEmpty()) LabelValue("節日", day.festivals.joinToString("、"))
            }
        } else {
            Text(
                "今日無節日與節氣",
                modifier = Modifier.fillMaxWidth().padding(18.dp),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun LabelValue(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
    }
}

@Composable
private fun DeityBirthdayCard(items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(
                "神明生日",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(10.dp))
            if (items.isEmpty()) {
                Text(
                    "今日無神明生日",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                        ) {
                            Text(item, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    settings: ThemeSettings,
    onBack: () -> Unit,
    onColorSelected: (ThemeChoice) -> Unit,
    onHueChanged: (Float) -> Unit,
    onDisplayModeSelected: (DisplayMode) -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.statusBarsPadding(),
                title = { Text("設定", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            SettingsHeading(Icons.Rounded.SettingsBrightness, "顯示模式")
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DisplayMode.entries.forEach { mode ->
                    DisplayModeChoice(
                        modifier = Modifier.weight(1f),
                        mode = mode,
                        selected = settings.displayMode == mode,
                        onClick = { onDisplayModeSelected(mode) },
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))
            SettingsHeading(Icons.Rounded.Palette, "主題色彩")
            Spacer(Modifier.height(10.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth().selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 2,
            ) {
                ThemeChoice.entries.filter { it != ThemeChoice.CUSTOM }.forEach { choice ->
                    ThemeColorChoice(
                        modifier = Modifier.weight(1f),
                        choice = choice,
                        selected = settings.colorChoice == choice,
                        onClick = { onColorSelected(choice) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            CustomColorPanel(settings, onColorSelected, onHueChanged)
            Spacer(Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SettingsHeading(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(onClick = {}, modifier = Modifier.size(40.dp)) {
            Icon(icon, contentDescription = null)
        }
        Spacer(Modifier.width(12.dp))
        Text(title, fontSize = 21.sp, fontWeight = FontWeight.Normal)
    }
}

@Composable
private fun ThemeColorChoice(modifier: Modifier, choice: ThemeChoice, selected: Boolean, onClick: () -> Unit) {
    val previewSettings = ThemeSettings(colorChoice = choice)
    val swatch = resolvePrimary(previewSettings)
    val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier
            .height(52.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = content,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(24.dp).clip(CircleShape).background(swatch).border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape))
            Spacer(Modifier.width(9.dp))
            Text(choice.label, modifier = Modifier.weight(1f), fontWeight = if (selected) FontWeight.Black else FontWeight.Medium)
            if (selected) Icon(Icons.Rounded.Check, contentDescription = "已選取", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CustomColorPanel(
    settings: ThemeSettings,
    onColorSelected: (ThemeChoice) -> Unit,
    onHueChanged: (Float) -> Unit,
) {
    val selected = settings.colorChoice == ThemeChoice.CUSTOM
    val customColor = resolvePrimary(settings)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = { onColorSelected(ThemeChoice.CUSTOM) }, role = Role.RadioButton),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) customColor else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(customColor)
                        .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
                )
                Spacer(Modifier.width(10.dp))
                Text("自訂色彩", modifier = Modifier.weight(1f), fontWeight = if (selected) FontWeight.Black else FontWeight.Medium)
                if (selected) Icon(Icons.Rounded.Check, contentDescription = "已選取", tint = customColor)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxWidth().height(12.dp).padding(horizontal = 10.dp)) {
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Red, Color.Yellow, Color.Green, Color.Cyan,
                                Color.Blue, Color.Magenta, Color.Red,
                            ),
                        ),
                        cornerRadius = CornerRadius(size.height / 2),
                    )
                }
                Slider(
                    value = settings.customHue,
                    onValueChange = onHueChanged,
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = customColor,
                        activeTrackColor = Color.Transparent,
                        inactiveTrackColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DisplayModeChoice(
    modifier: Modifier,
    mode: DisplayMode,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val icon = when (mode) {
        DisplayMode.SYSTEM -> Icons.Rounded.SettingsBrightness
        DisplayMode.LIGHT -> Icons.Rounded.LightMode
        DisplayMode.DARK -> Icons.Rounded.DarkMode
    }
    val container = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    Surface(
        modifier = modifier
            .height(52.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        shape = RoundedCornerShape(16.dp),
        color = container,
        contentColor = content,
        border = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                mode.label,
                maxLines = 1,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Medium,
            )
        }
    }
}
