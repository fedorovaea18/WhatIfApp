package ru.eafedorova.whatifapp.audio

import android.media.MediaPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun play(filePath: String, onComplete: () -> Unit = {}) {
        if (currentPath == filePath && player != null) {
            if (_isPlaying.value) return
            player?.start()
            _isPlaying.value = true
            startProgressUpdates()
            return
        }

        releasePlayer()

        val file = File(filePath)
        if (!file.exists()) {
            onComplete()
            return
        }

        val mp = MediaPlayer()
        player = mp
        currentPath = filePath

        try {
            mp.setDataSource(filePath)
            mp.prepare()
            mp.start()
        } catch (e: Exception) {
            releasePlayer()
            onComplete()
            return
        }

        mp.setOnCompletionListener {
            releasePlayer()
            onComplete()
        }

        _duration.value = mp.duration
        _position.value = 0
        _isPlaying.value = true
        startProgressUpdates()
    }

    fun pause() {
        val mp = player ?: return
        if (mp.isPlaying) {
            mp.pause()
            _isPlaying.value = false
            stopProgressUpdates()
        }
    }

    fun resume() {
        val mp = player ?: return
        if (!mp.isPlaying) {
            mp.start()
            _isPlaying.value = true
            startProgressUpdates()
        }
    }

    fun stop() {
        releasePlayer()
    }

    fun isCurrentFile(filePath: String): Boolean = currentPath == filePath

    /**
     * Единственное место, где освобождается MediaPlayer.
     * Идемпотентно: можно вызывать сколько угодно раз.
     */
    private fun releasePlayer() {
        stopProgressUpdates()
        val mp = player
        player = null
        currentPath = null
        if (mp != null) {
            try {
                if (mp.isPlaying) mp.stop()
            } catch (_: IllegalStateException) {
            }
            try {
                mp.reset()
            } catch (_: IllegalStateException) {
            }
            mp.release()
        }
        _isPlaying.value = false
        _position.value = 0
        _duration.value = 0
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = scope.launch {
            while (true) {
                val mp = player ?: break
                val pos = try {
                    mp.currentPosition
                } catch (_: IllegalStateException) {
                    break
                }
                _position.value = pos
                delay(200)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }
}