package ru.eafedorova.whatifapp.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.audio.VoicePlayer
import ru.eafedorova.whatifapp.audio.VoiceRecorder
import ru.eafedorova.whatifapp.data.Answer
import ru.eafedorova.whatifapp.data.Question
import ru.eafedorova.whatifapp.data.QuestionRepository
import ru.eafedorova.whatifapp.data.categories
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnswerScreen(question: Question, onBack: () -> Unit) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository.create(context) }
    val scope = rememberCoroutineScope()

    val recorder = remember { VoiceRecorder(context) }
    val player = remember { VoicePlayer() }

    var text by remember { mutableStateOf("") }
    var voiceFile by remember { mutableStateOf<File?>(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

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
        }
    }

    fun startRecording() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            voiceFile = recorder.start()
            isRecording = true
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun stopRecording() {
        voiceFile = recorder.stop()
        isRecording = false
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingTime = 0

            val baseWave = List(24) { index ->
                val center = 12f
                val distanceFromCenter = kotlin.math.abs(index - center)
                val height = 1f - (distanceFromCenter / 14f)
                height.coerceIn(0.1f, 1f)
            }


            launch {

                while (isRecording) {
                    kotlinx.coroutines.delay(1000)
                    if (isRecording) recordingTime++
                }
            }

            launch {
                while (isRecording) {
                    kotlinx.coroutines.delay(150)
                    if (isRecording) {
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
                        "Страница для тебя",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
                    )
                },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {

            Text(
                text = "Побудь в этой мысли",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onBackground
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))

            Text(
                text = question.text,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(24.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = if (text.isNotBlank() || isRecording || isFocused) 1.dp else 0.dp,
                    color = if (text.isNotBlank() || isRecording || isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
                )
            ) {
                if (isRecording) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Красная точка и текст
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE57373)) // Красный
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "ИДЁТ ЗАПИСЬ",
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(32.dp))

                        Text(
                            text = String.format(
                                "%02d:%02d",
                                recordingTime / 60,
                                recordingTime % 60
                            ),
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontSize = 56.sp
                            ),
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
                                .height(64.dp), // Высота контейнера
                            horizontalArrangement = Arrangement.spacedBy(4.dp), // Отступ между полосками
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            amplitude.forEach { amp ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(amp) // Высота полоски
                                        .clip(CircleShape) // ⬅️ ВОТ ЭТО ДЕЛАЕТ КРАЯ КРУГЛЫМИ (как капсулы)
                                        .background(
                                            if (isRecording) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // Чуть бледнее при прослушивании
                                        )
                                )
                            }
                        }

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = { stopRecording() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onSurface)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Остановить запись", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                } else if (voiceFile != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Твоя мысль записана",
                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(32.dp))

                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = {
                                    if (isPlaying) {
                                        player.stop()
                                        isPlaying = false
                                    } else {
                                        isPlaying = true
                                        player.play(voiceFile!!.absolutePath) { isPlaying = false }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) androidx.compose.material.icons.Icons.Default.Check else androidx.compose.material.icons.Icons.Default.PlayArrow, // Или иконка Pause, если есть
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(32.dp))

                        TextButton(
                            onClick = {
                                voiceFile?.delete()
                                voiceFile = null
                                player.stop()
                                isPlaying = false
                            }
                        ) {
                            Text("Перезаписать", color = MaterialTheme.colorScheme.error)
                        }
                    }
                } else {

                    Column(modifier = Modifier.fillMaxSize()) {
                        // Верхняя часть: поле ввода
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
                                    style = MaterialTheme.typography.bodyMedium,
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
                                if (text.isEmpty()) {
                                    Text(
                                        text = "Какая мысль приходит первой?\nНачни с неё...",
                                        style = MaterialTheme.typography.bodyLarge,
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
                                .clickable { startRecording() }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
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
                                    text = "Нажми, чтобы начать запись",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }


            Spacer(Modifier.height(16.dp))
            Text(
                text = if (text.isNotBlank() || voiceFile != null) "Можно не искать идеальные слова" else "Здесь не бывает правильных ответов",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        if (isRecording) recorder.cancel()
                        player.stop()
                        onBack()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Назад",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            repository.saveAnswer(
                                Answer(
                                    questionId = question.id,
                                    questionText = question.text,
                                    answerText = text.trim(),
                                    voicePath = voiceFile?.absolutePath,
                                    category = question.category
                                )
                            )
                            onBack()
                        }
                    },
                    enabled = (text.isNotBlank() || voiceFile != null) && !isRecording,
                    modifier = Modifier
                        .width(160.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Сохранить", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Секретно. Только для тебя",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}