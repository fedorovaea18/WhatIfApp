package ru.eafedorova.whatifapp.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.R
import ru.eafedorova.whatifapp.audio.VoicePlayer
import ru.eafedorova.whatifapp.data.Answer
import ru.eafedorova.whatifapp.data.AppDatabase
import ru.eafedorova.whatifapp.data.categories
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit,
    onStatsClick: () -> Unit,
    onGoToHome: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val player = remember { VoicePlayer() }

    val answers by db.answerDao().getAll().collectAsState(initial = emptyList())

    var answerToDelete by remember { mutableStateOf<Answer?>(null) }
    var playingId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Что если?", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = {
                        player.stop()
                        onBack()
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_book),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Мои ответы",
                        style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Мысли, к которым можно вернуться",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Последние записи · ${answers.size} из 24",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onStatsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_stats),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Статистика",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (answers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_notebook),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(16.dp))

                                Text(
                                    text = "Пока нет ответов",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(8.dp))

                                Text(
                                    text = "Первая мысль может быть совсем короткой. Дай ей место здесь",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(24.dp))

                                Button(
                                    onClick = onGoToHome,
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
                                        painter = painterResource(id = R.drawable.ic_edit),
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Первый ответ", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(answers, key = { it.id }) { answer ->
                        AnswerCard(
                            answer = answer,
                            isPlaying = playingId == answer.id,
                            player = player,
                            onPlayPause = {
                                val path = answer.voicePath ?: return@AnswerCard
                                if (playingId == answer.id) {
                                    if (player.isPlaying.value) player.pause() else player.resume()
                                } else {
                                    player.stop()
                                    playingId = answer.id
                                    player.play(path) { playingId = null }
                                }
                            },
                            onDeleteClick = { answerToDelete = answer }
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ещё 21 запись ниже",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
            }
        }
    }

    answerToDelete?.let { answer ->
        AlertDialog(
            onDismissRequest = { answerToDelete = null },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    text = "Стереть эту мысль?",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "Она исчезнет с устройства навсегда. Но у тебя появится место для новых мыслей",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { answerToDelete = null },
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Оставить", style = MaterialTheme.typography.labelLarge)
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                if (playingId == answer.id) { player.stop(); playingId = null }
                                answer.voicePath?.let { path -> runCatching { File(path).delete() } }
                                db.answerDao().deleteById(answer.id)
                                answerToDelete = null
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8A3A3),
                            contentColor = Color(0xFF1C1E1E)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Стереть", style = MaterialTheme.typography.labelLarge)
                    }
                }
            },
            dismissButton = {}
        )
    }
}

@Composable
private fun AnswerCard(
    answer: Answer,
    isPlaying: Boolean,
    player: VoicePlayer,
    onPlayPause: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val position by player.position.collectAsState()
    val duration by player.duration.collectAsState()
    val isPlayerPlaying by player.isPlaying.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(
                text = answer.questionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))

            if (answer.answerText.isNotBlank()) {
                Text(
                    text = answer.answerText,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
            }


            if (!answer.voicePath.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onPlayPause, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (isPlaying && isPlayerPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                formatTime(if (isPlaying) position else 0),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                formatTime(duration),
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (isPlaying && duration > 0) position.toFloat() / duration.toFloat() else 0f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDateWithToday(answer.createdAt),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val category = answer.category
                        val categoryLabel =
                            categories.find { it.first == category }?.second ?: category
                        val iconRes = when (category) {
                            "philosophy" -> R.drawable.ic_cat_philosophy
                            "incredible" -> R.drawable.ic_cat_incredible
                            "superpower" -> R.drawable.ic_cat_superpower
                            "money" -> R.drawable.ic_cat_money
                            "funny" -> R.drawable.ic_cat_funny
                            "relationships" -> R.drawable.ic_cat_relationships
                            "dark" -> R.drawable.ic_cat_dark
                            else -> R.drawable.ic_cat_all
                        }

                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = categoryLabel,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trash),
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private fun formatDateWithToday(timestamp: Long): String {
    val now = Calendar.getInstance()
    val answerDate = Calendar.getInstance().apply { timeInMillis = timestamp }

    val isToday = now.get(Calendar.YEAR) == answerDate.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == answerDate.get(Calendar.DAY_OF_YEAR)

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == answerDate.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == answerDate.get(Calendar.DAY_OF_YEAR)

    val timeFormatter = SimpleDateFormat("HH:mm", Locale("ru"))
    val timeString = timeFormatter.format(Date(timestamp))

    return when {
        isToday -> "Сегодня, $timeString"
        isYesterday -> "Вчера, $timeString"
        else -> {
            val dateFormatter = SimpleDateFormat("d MMM, HH:mm", Locale("ru"))
            dateFormatter.format(Date(timestamp))
        }
    }
}

private fun formatTime(ms: Int): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
