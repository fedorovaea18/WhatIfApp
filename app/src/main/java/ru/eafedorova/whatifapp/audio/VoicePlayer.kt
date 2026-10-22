package ru.eafedorova.whatifapp.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class VoicePlayer {

    private var player: MediaPlayer? = null
    private var currentPath: String? = null

    private val _position = MutableStateFlow(0)
    val position: StateFlow<Int> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    fun play(filePath: String, onComplete: () -> Unit = {}) {
        if (player != null && currentPath == filePath) {
            player?.start()
            _isPlaying.value = true
            startProgressUpdates()
            return
        }

        stop()

        val file = File(filePath)
        if (!file.exists()) {
            onComplete()
            return
        }

        player = MediaPlayer().apply {
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener {
                _isPlaying.value = false
                _position.value = 0
                stopProgressUpdates()
                onComplete()
            }
        }

        currentPath = filePath
        _duration.value = player?.duration ?: 0
        _position.value = 0
        _isPlaying.value = true
        startProgressUpdates()
    }

    fun pause() {
        if (player?.isPlaying == true) {
            player?.pause()
            _isPlaying.value = false
            stopProgressUpdates()
        }
    }

    fun resume() {
        if (player != null && !player!!.isPlaying) {
            player?.start()
            _isPlaying.value = true
            startProgressUpdates()
        }
    }

    fun stop() {
        stopProgressUpdates()
        try {
            player?.stop()
        } catch (_: Exception) {
        }
        player?.release()
        player = null
        currentPath = null
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }

    fun isCurrentFile(filePath: String): Boolean = currentPath == filePath

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (true) {
                player?.let { mp ->
                    try {
                        _position.value = mp.currentPosition
                    } catch (_: Exception) {
                        break
                    }
                } ?: break
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
}