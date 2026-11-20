package ru.eafedorova.whatifapp.presentation.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.audio.VoicePlayer
import ru.eafedorova.whatifapp.data.AppDatabase
import ru.eafedorova.whatifapp.data.ThemePreferences
import ru.eafedorova.whatifapp.notifications.NotificationScheduler
import ru.eafedorova.whatifapp.presentation.ui.theme.customColors
import ru.eafedorova.whatifapp.presentation.ui.theme.labelCapsSmall
import java.io.File

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.offset

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themePrefs = remember { ThemePreferences(context) }
    val db = remember { AppDatabase.getInstance(context) }

    val currentTheme by themePrefs.themeFlow.collectAsState(initial = "system")
    var vibrationEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(false) }

    val player = remember { VoicePlayer() }

    val notificationsPermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    val channelEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = notificationManager.getNotificationChannel("daily_question")
        channel == null || channel.importance != android.app.NotificationManager.IMPORTANCE_NONE
    } else {
        true
    }

    var textAnswersCount by remember { mutableIntStateOf(0) }
    var voiceAnswersCount by remember { mutableIntStateOf(0) }

    var reminderFrequency by remember { mutableStateOf("daily") }
    var reminderHour by remember { mutableIntStateOf(19) }
    var reminderMinute by remember { mutableIntStateOf(0) }

    var showFrequencyDialog by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteTextDialog by remember { mutableStateOf(false) }
    var showDeleteVoiceDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        reminderFrequency = themePrefs.getReminderFrequency()
        reminderHour = themePrefs.getReminderHour()
        reminderMinute = themePrefs.getReminderMinute()
        vibrationEnabled = themePrefs.getVibrationEnabled()
        notificationsEnabled = themePrefs.getNotificationsEnabled()

        val allAnswers = db.answerDao().getAll().first()
        textAnswersCount = allAnswers.count { it.answerText.isNotBlank() }
        voiceAnswersCount = allAnswers.count { !it.voicePath.isNullOrBlank() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "ЧТО ЕСЛИ?",
                        style = MaterialTheme.typography.labelCapsSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        player.stop()
                        onBack()
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Настройки",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Сделай это место своим",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SettingsSection(title = "Атмосфера") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeButton(
                                label = "Светлая",
                                value = "light",
                                currentTheme = currentTheme,
                                modifier = Modifier.weight(1f)
                            ) { scope.launch { themePrefs.setTheme("light") } }

                            ThemeButton(
                                label = "Тёмная",
                                value = "dark",
                                currentTheme = currentTheme,
                                modifier = Modifier.weight(1f)
                            ) { scope.launch { themePrefs.setTheme("dark") } }

                            ThemeButton(
                                label = "Системная",
                                value = "system",
                                currentTheme = currentTheme,
                                modifier = Modifier.weight(1f)
                            ) { scope.launch { themePrefs.setTheme("system") } }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SettingsSection(title = "Прикосновения") {
                        SettingsSwitchRow(
                            title = "Виброотклик",
                            subtitle = "Лёгкая вибрация при нажатии",
                            checked = vibrationEnabled,
                            onCheckedChange = {
                                vibrationEnabled = it
                                scope.launch { themePrefs.setVibrationEnabled(it) }
                            },
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SettingsSection(title = "Ритм") {
                        SettingsSwitchRow(
                            title = "Минута для себя",
                            subtitle = "Тихое напоминание о новом вопросе",
                            checked = notificationsEnabled,
                            onCheckedChange = { isEnabled ->
                                notificationsEnabled = isEnabled
                                scope.launch {
                                    themePrefs.setNotificationsEnabled(isEnabled)
                                    if (isEnabled) {
                                        NotificationScheduler.schedule(
                                            context,
                                            reminderFrequency,
                                            reminderHour,
                                            reminderMinute
                                        )
                                    } else {
                                        NotificationScheduler.cancelDailyQuestion(context)
                                    }
                                }
                            }
                        )

                        if (notificationsEnabled) {
                            Spacer(Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            Spacer(Modifier.height(12.dp))

                            SettingsValueRow(
                                label = "Как часто",
                                value = getFrequencyLabel(reminderFrequency) + " ▾",
                                onClick = { showFrequencyDialog = true }
                            )

                            SettingsValueRow(
                                label = "Во сколько",
                                value = formatTime(reminderHour, reminderMinute),
                                onClick = { showTimePicker = true }
                            )

                            if (!notificationsPermissionGranted || !channelEnabled) {
                                Spacer(Modifier.height(12.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            "⚠ Уведомления отключены в системе",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                        Spacer(Modifier.height(8.dp))
                                        Text(
                                            "Открыть настройки →",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.clickable {
                                                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                    }
                                                } else {
                                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                                        data = android.net.Uri.parse("package:${context.packageName}")
                                                    }
                                                }
                                                context.startActivity(intent)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    SettingsSection(title = "Следы") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Мысли текстом",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (textAnswersCount > 0) {
                                        "$textAnswersCount ${pluralizeAnswers(textAnswersCount)}"
                                    } else {
                                        "Пока нет записей"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showDeleteTextDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trash),
                                    contentDescription = "Удалить текстовые мысли",
                                    tint = MaterialTheme.customColors.attention
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Мысли голосом",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = if (voiceAnswersCount > 0) {
                                        "$voiceAnswersCount ${pluralizeVoices(voiceAnswersCount)}"
                                    } else {
                                        "Пока нет записей"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showDeleteVoiceDialog = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_trash),
                                    contentDescription = "Удалить голосовые мысли",
                                    tint = MaterialTheme.customColors.attention
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Что если?",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "Версия 1.0.0",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }


    if (showFrequencyDialog) {
        AlertDialog(
            onDismissRequest = { showFrequencyDialog = false },
            title = { Text("Выбери свой ритм") },
            text = {
                Column {
                    listOf(
                        "daily" to "Каждый день",
                        "weekdays" to "По будням",
                        "weekly" to "Раз в неделю"
                    ).forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    reminderFrequency = key
                                    showFrequencyDialog = false
                                    scope.launch {
                                        themePrefs.setReminderFrequency(key)
                                        NotificationScheduler.schedule(
                                            context, reminderFrequency, reminderHour, reminderMinute
                                        )
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = reminderFrequency == key,
                                onClick = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFrequencyDialog = false }) {
                    Text("Закрыть")
                }
            }
        )
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = reminderHour,
            initialMinute = reminderMinute,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Выбери удобное время") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    reminderHour = timePickerState.hour
                    reminderMinute = timePickerState.minute
                    showTimePicker = false
                    scope.launch {
                        themePrefs.setReminderTime(reminderHour, reminderMinute)
                        NotificationScheduler.schedule(
                            context, reminderFrequency, reminderHour, reminderMinute
                        )
                    }
                }) {
                    Text("ОК")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Отмена")
                }
            }
        )
    }

    if (showDeleteTextDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTextDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Убрать все текстовые мысли?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Они исчезнут с устройства навсегда.\nНо у тебя появится место для новых мыслей",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteTextDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Оставить", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                player.stop()
                                val textAnswers = db.answerDao().getAllOnce().filter { it.answerText.isNotBlank() }
                                textAnswers.forEach { answer ->
                                    answer.voicePath?.let { path ->
                                        runCatching { File(path).delete() }
                                    }
                                }
                                db.answerDao().deleteTextAnswers()
                                textAnswersCount = 0
                                showDeleteTextDialog = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Убрать", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {}
        )
    }


    if (showDeleteVoiceDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteVoiceDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Убрать все голосовые мысли?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Они исчезнут с устройства навсегда.\nНо у тебя появится место для новых мыслей",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 15.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDeleteVoiceDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Оставить", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                player.stop()
                                val voiceAnswers = db.answerDao().getVoiceAnswers()
                                voiceAnswers.forEach { answer ->
                                    answer.voicePath?.let { path ->
                                        runCatching { File(path).delete() }
                                    }
                                }
                                db.answerDao().deleteVoiceAnswers()
                                voiceAnswersCount = 0
                                showDeleteVoiceDialog = false
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Убрать", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {}
        )
    }
}


private fun pluralizeAnswers(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "ответов"
        mod10 == 1 -> "ответ"
        mod10 in 2..4 -> "ответа"
        else -> "ответов"
    }
}

private fun pluralizeVoices(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod100 in 11..19 -> "записей"
        mod10 == 1 -> "запись"
        mod10 in 2..4 -> "записи"
        else -> "записей"
    }
}

private fun getFrequencyLabel(frequency: String): String {
    return when (frequency) {
        "daily" -> "Каждый день"
        "weekdays" -> "По будням"
        "weekly" -> "Раз в неделю"
        else -> "Каждый день"
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(Modifier.height(16.dp))

            content()
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        CompactSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ThemeButton(
    label: String,
    value: String,
    currentTheme: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isSelected = value == currentTheme
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.secondaryContainer,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondaryContainer
        ),
        border =  null,
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun CompactSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {

    val trackWidth = 44.dp
    val trackHeight = 26.dp
    val thumbSize = 20.dp
    val padding = 3.dp


    val trackColor = Color(0xFFE8C58F)
    val offThumbColor = Color(0xFF5C4B35)
    val onThumbColor = Color(0xFF302419)


    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - padding else padding,
        animationSpec = tween(durationMillis = 200),
        label = "thumbOffset"
    )

    val currentThumbColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (checked) onThumbColor else offThumbColor,
        animationSpec = tween(durationMillis = 200),
        label = "thumbColor"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false, radius = trackHeight / 2),
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset - padding)
                .size(thumbSize)
                .clip(CircleShape)
                .background(currentThumbColor)
        )
    }
}