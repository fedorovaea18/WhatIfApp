package ru.eafedorova.whatifapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import ru.eafedorova.whatifapp.presentation.screens.AnswerScreen
import ru.eafedorova.whatifapp.presentation.screens.HomeScreen
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Answer : Screen("answer/{question}") {
        fun create(question: String): String {
            val encoded = URLEncoder.encode(question, StandardCharsets.UTF_8.toString())
            return "answer/$encoded"
        }
    }
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAnswerClick = { question ->
                    navController.navigate(Screen.Answer.create(question))
                }
            )
        }
        composable(Screen.Answer.route) { backStackEntry ->
            val encoded = backStackEntry.arguments?.getString("question") ?: ""
            val question = URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString())
            AnswerScreen(
                question = question,
                onBack = { navController.popBackStack() }
            )
        }
    }
}