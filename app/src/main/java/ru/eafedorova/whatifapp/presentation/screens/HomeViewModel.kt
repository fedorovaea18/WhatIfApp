package ru.eafedorova.whatifapp.presentation.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.eafedorova.whatifapp.data.AppDatabase

class HomeViewModel(context: Context) : ViewModel() {
    private val db = AppDatabase.Companion.getInstance(context)

    private val _totalAnswers = MutableStateFlow(0)
    val totalAnswers: StateFlow<Int> = _totalAnswers.asStateFlow()

    fun refreshProgress() {
        viewModelScope.launch {
            _totalAnswers.value = db.answerDao().count()
        }
    }
}