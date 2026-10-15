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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

val questions = listOf(
    "Что если бы ты мог(ла) прожить один день в теле любого человека? Кого бы выбрал(а)?",
    "Что если бы завтра исчез интернет навсегда? Что бы ты делал(а) первую неделю?",
    "Что если бы ты нашёл(ла) чемодан с миллионом рублей? Что бы сделал(а)?",
    "Что если бы ты мог(ла) задать один вопрос вселенной и получить правдивый ответ?",
    "Что если бы ты проснулся(ась) через 100 лет? Что бы тебя удивило больше всего?",
    "Что если бы тебе предложили работу мечты, но в другой стране и без права вернуться?",
    "Что если бы ты мог(ла) отменить одно изобретение человечества? Какое?",
    "Что если бы твои мысли стали слышны всем на 24 часа?",
    "Что если бы ты встретил(а) себя из прошлого? Что бы сказал(а)?",
    "Что если бы завтра тебе дали любые ресурсы для одного проекта? Что бы ты создал(а)?"
)

@Composable
fun HomeScreen(onAnswerClick: (String) -> Unit) {
    var question by remember { mutableStateOf(questions.random()) }

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

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Text(
                text = question,
                modifier = Modifier.padding(24.dp),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(40.dp))

        Button(
            onClick = { question = questions.random() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Другой вопрос")
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = { onAnswerClick(question) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ответить")
        }
    }
}