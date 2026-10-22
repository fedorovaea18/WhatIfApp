package ru.eafedorova.whatifapp.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.audio.VoicePlayer
import ru.eafedorova.whatifapp.audio.VoiceRecorder
import ru.eafedorova.whatifapp.data.Answer
import ru.eafedorova.whatifapp.data.Question
import ru.eafedorova.whatifapp.data.QuestionRepository
import java.io.File

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

    fun playVoice() {
        val file = voiceFile ?: return
        if (isPlaying) {
            player.stop()
            isPlaying = false
        } else {
            isPlaying = true
            player.play(file.absolutePath) {
                isPlaying = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text =  question.text,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            placeholder = { Text("Напиши свой ответ или запиши голос...") },
            enabled = !isRecording
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!isRecording) {
                Button(
                    onClick = { startRecording() },
                    modifier = Modifier.weight(1f),
                    enabled = voiceFile == null
                ) {
                    Text("Записать")
                }
            } else {
                Button(
                    onClick = { stopRecording() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text("⏹ Стоп")
                }
            }

            if (voiceFile != null && !isRecording) {
                OutlinedButton(
                    onClick = { playVoice() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isPlaying) "⏹ Стоп" else "Прослушать")
                }
            }
        }

        if (voiceFile != null && !isRecording) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    voiceFile?.delete()
                    voiceFile = null
                    player.stop()
                    isPlaying = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🗑 Удалить запись")
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isRecording) recorder.cancel()
                    player.stop()
                    onBack()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("← Назад")
            }
            Button(
                onClick = {
                    scope.launch {
                        repository.saveAnswer(
                            Answer(
                                questionId = question.id,
                                questionText = question.text,
                                answerText = text.trim(),
                                voicePath = voiceFile?.absolutePath
                            )
                        )
                        onBack()
                    }
                },
                enabled = (text.isNotBlank() || voiceFile != null) && !isRecording,
                modifier = Modifier.weight(1f)
            ) {
                Text("Сохранить")
            }
        }
    }
}