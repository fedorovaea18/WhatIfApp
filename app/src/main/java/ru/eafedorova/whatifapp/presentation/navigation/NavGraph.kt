package ru.eafedorova.whatifapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.eafedorova.whatifapp.data.AppDatabase
import ru.eafedorova.whatifapp.data.Question
import ru.eafedorova.whatifapp.presentation.screens.AnswerScreen
import ru.eafedorova.whatifapp.presentation.screens.HistoryScreen
import ru.eafedorova.whatifapp.presentation.screens.HomeScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Answer : Screen("answer/{questionId}") {
        fun create(questionId: Long): String = "answer/$questionId"
    }
    object History : Screen("history")
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAnswerClick = { question ->
                    navController.navigate(Screen.Answer.create(question.id))
                },
                onHistoryClick = {                    // ← добавить
                    navController.navigate(Screen.History.route)
                }
            )
        }
        composable(Screen.Answer.route) { backStackEntry ->
            val questionId = backStackEntry.arguments?.getString("questionId")?.toLongOrNull()
            var question by remember { mutableStateOf<Question?>(null) }

            LaunchedEffect(questionId) {
                if (questionId != null) {
                    question = db.questionDao().getById(questionId)
                }
            }

            question?.let {
                AnswerScreen(question = it, onBack = { navController.popBackStack() })
            }
        }
        composable(Screen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}