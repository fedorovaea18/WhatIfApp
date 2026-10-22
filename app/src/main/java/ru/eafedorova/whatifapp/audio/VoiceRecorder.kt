package ru.eafedorova.whatifapp.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(): File {
        val dir = File(context.filesDir, "voices").apply { mkdirs() }
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")
        currentFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        return file
    }

    fun stop(): File? {
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            currentFile
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            currentFile?.delete()
            null
        }
    }

    fun cancel() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        recorder?.release()
        recorder = null
        currentFile?.delete()
        currentFile = null
    }
}