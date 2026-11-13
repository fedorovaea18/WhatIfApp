package ru.eafedorova.whatifapp.presentation.screens

import VoiceRecorder
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.audio.VoicePlayer
import ru.eafedorova.whatifapp.data.Answer
import ru.eafedorova.whatifapp.data.Question
import ru.eafedorova.whatifapp.data.QuestionRepository
import ru.eafedorova.whatifapp.data.categories
import ru.eafedorova.whatifapp.presentation.ui.theme.customColors
import ru.eafedorova.whatifapp.presentation.ui.theme.labelCapsSmall
import java.io.File

@RequiresApi(Build.VERSION_CODES.R)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(
    question: Question,
    onBack: () -> Unit,
    onStatsClick: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository.create(context) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var isSaving by remember { mutableStateOf(false) }
    var isPreparingToRecord by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(false) }


    val recorder = remember { VoiceRecorder(context) }
    val player = remember { VoicePlayer() }

    var text by remember { mutableStateOf("") }
    var voiceFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    var recordingTime by remember { mutableIntStateOf(0) }
    var amplitude by remember { mutableStateOf(List(20) { 0f }) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            voiceFile = recorder.start()
            isRecording = true
        } else {
            isPreparingToRecord = false
            Toast.makeText(context, "Нужно разрешение на микрофон", Toast.LENGTH_SHORT).show()
        }
    }

    fun startRecording() {
        if (isRecording) return

        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        android.util.Log.d("PERMISSION_CHECK", "Есть разрешение? $hasPermission")

        if (hasPermission) {
            try {
                voiceFile = recorder.start()
                isRecording = true
                isPaused = false
                recordingTime = 0
            } catch (e: Exception) {
                e.printStackTrace()
                android.util.Log.e("RECORD_CRASH", "Ошибка: ${e.message}")
                Toast.makeText(context, "Ошибка записи: ${e.message}", Toast.LENGTH_LONG).show()
            }
        } else {
            android.util.Log.d("PERMISSION_CHECK", "Запрашиваем разрешение...")
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        voiceFile = recorder.stop()
        isRecording = false
    }

    LaunchedEffect(isRecording, isPaused) {
        if (isRecording && !isPaused) {

            if (recordingTime == 0) {

            }

            val baseWave = List(24) { index ->
                val center = 12f
                val distanceFromCenter = kotlin.math.abs(index - center)
                val height = 1f - (distanceFromCenter / 14f)
                height.coerceIn(0.1f, 1f)
            }

            launch {
                while (isRecording && !isPaused) {
                    kotlinx.coroutines.delay(1000)
                    if (isRecording && !isPaused) recordingTime++
                }
            }

            launch {
                while (isRecording && !isPaused) {
                    kotlinx.coroutines.delay(150)
                    if (isRecording && !isPaused) {
                        amplitude = baseWave.map { baseHeight ->
                            val randomFactor = kotlin.random.Random.nextFloat() * 0.4f + 0.8f
                            (baseHeight * randomFactor).coerceIn(0.1f, 1f)
                        }
                    }
                }
            }
        }
    }

    val categoryLabel =
        categories.find { it.first == question.category }?.second ?: question.category
    val iconRes = when (question.category) {
        "philosophy" -> R.drawable.ic_cat_philosophy
        "incredible" -> R.drawable.ic_cat_incredible
        "superpower" -> R.drawable.ic_cat_superpower
        "money" -> R.drawable.ic_cat_money
        "funny" -> R.drawable.ic_cat_funny
        "relationships" -> R.drawable.ic_cat_relationships
        "dark" -> R.drawable.ic_cat_dark
        else -> R.drawable.ic_cat_all
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
                actions = {
                    Surface(
                        onClick = onStatsClick,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .width(116.dp)
                            .height(44.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_stats),
                                contentDescription = "Статистика",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Статистика",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Твоя мысль",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = categoryLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (text.isNotBlank() || isRecording || isFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    }
                )
            ) {
                if (isRecording || isPreparingToRecord) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    if (isRecording) recorder.cancel()
                                    isRecording = false
                                    isPaused = false
                                    isPreparingToRecord = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Закрыть",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(Modifier.weight(0.5f))

                        if (isRecording && !isPaused) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.customColors.attention)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "ИДЁТ ЗАПИСЬ",
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                    color = MaterialTheme.customColors.attention
                                )
                            }
                        } else if (isPaused) {
                            Text(
                                text = "ПАУЗА",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "ГОТОВ К ЗАПИСИ",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(Modifier.height(32.dp))


                        Text(
                            text = String.format(
                                "%02d:%02d",
                                recordingTime / 60,
                                recordingTime % 60
                            ),
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Говори в своём ритме",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(32.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            amplitude.forEach { amp ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(if (isRecording && !isPaused) amp else 0.1f)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = if (isRecording && !isPaused) 1f else 0.3f
                                            )
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        if (!isRecording && !isPaused) {
                            Button(
                                onClick = { startRecording() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_microphone),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Начать запись", style = MaterialTheme.typography.labelLarge)
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (isPaused) {
                                            recorder.resume()
                                            isPaused = false
                                        } else {
                                            recorder.pause()
                                            isPaused = true
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    if (isPaused) {
                                        Text(
                                            "Продолжить",
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    } else {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Box(
                                                Modifier
                                                    .size(4.dp, 16.dp)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                            Box(
                                                Modifier
                                                    .size(4.dp, 16.dp)
                                                    .background(MaterialTheme.colorScheme.onSurface)
                                            )
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Text("Пауза", style = MaterialTheme.typography.labelLarge)
                                    }
                                }

                                Button(
                                    onClick = {
                                        stopRecording()
                                        isPreparingToRecord = false
                                        isPaused = false
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(56.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.onPrimary)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Стоп", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Мои мысли",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Icon(
                                    painterResource(id = R.drawable.ic_pen),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Box(modifier = Modifier.fillMaxSize()) {
                                if (text.isEmpty() && !isFocused) {
                                    Text(
                                        text = "Какая мысль приходит первой?\nНачни с неё ...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                androidx.compose.foundation.text.BasicTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxSize(),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(
                                        MaterialTheme.colorScheme.primary
                                    ),
                                    interactionSource = interactionSource
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isPreparingToRecord = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_microphone),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Можно ответить голосом",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Нажми, чтобы начать",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = if (text.isNotBlank() || voiceFile != null) "Ты пишешь и это уже многое значит" else "Здесь не бывает правильных ответов",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showClearDialog = true },
                    enabled = (text.isNotBlank() || voiceFile != null || isRecording) && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        contentColor = MaterialTheme.customColors.attention,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_trash),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Очистить", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            repository.saveAnswer(
                                Answer(
                                    questionId = question.id,
                                    questionText = question.text,
                                    answerText = text.trim(),
                                    voicePath = voiceFile?.absolutePath,
                                    category = question.category
                                )
                            )

                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            Toast.makeText(context, "Твоя мысль сохранена", Toast.LENGTH_SHORT)
                                .show()

                            delay(1000)
                            onSaveSuccess()
                        }
                    },
                    enabled = (text.isNotBlank() || voiceFile != null) && !isRecording && !isSaving,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Сохранить", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Очистить всё?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Твой текст и голосовая запись будут удалены. Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showClearDialog = false },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            if (isRecording) {
                                recorder.cancel()
                                isRecording = false
                            }
                            isPreparingToRecord = false
                            player.stop()
                            isPlaying = false
                            voiceFile?.delete()
                            voiceFile = null
                            text = ""
                            showClearDialog = false
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Очистить", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {}
        )
    }
}