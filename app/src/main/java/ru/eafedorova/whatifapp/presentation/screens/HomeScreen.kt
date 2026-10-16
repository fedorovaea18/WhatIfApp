package ru.eafedorova.whatifapp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.data.Question
import ru.eafedorova.whatifapp.data.QuestionRepository

@Composable
fun HomeScreen(onAnswerClick: (Question) -> Unit) {
    val context = LocalContext.current
    val repository = remember { QuestionRepository.create(context) }
    val scope = rememberCoroutineScope()

    var question by remember { mutableStateOf<Question?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        repository.ensureSeeded()
        question = repository.randomQuestion()
        loading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Что если?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(32.dp))

        if (loading || question == null) {
            CircularProgressIndicator()
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = question!!.text,
                    modifier = Modifier.padding(24.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = {
                scope.launch {
                    loading = true
                    question = repository.randomQuestion()
                    loading = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Другой вопрос")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { question?.let { onAnswerClick(it) } },
            modifier = Modifier.fillMaxWidth(),
            enabled = question != null
        ) {
            Text("Ответить")
        }
    }
}