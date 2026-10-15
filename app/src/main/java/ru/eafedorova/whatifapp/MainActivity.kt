package ru.eafedorova.whatifapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ru.eafedorova.whatifapp.presentation.navigation.NavGraph
import ru.eafedorova.whatifapp.presentation.ui.theme.WhatIfAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatIfAppTheme {
                NavGraph()
            }
        }
    }
}
