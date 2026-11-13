import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.File

class VoiceRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun start(): File? {
        try {

            currentFile = File(context.cacheDir, "record_${System.currentTimeMillis()}.m4a")

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
            mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            mediaRecorder?.setOutputFile(currentFile?.absolutePath)

            mediaRecorder?.prepare()
            mediaRecorder?.start()

            return currentFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun stop(): File? {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
            currentFile?.delete()
            currentFile = null
        } finally {
            mediaRecorder = null
        }
        return currentFile
    }

    fun cancel() {
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaRecorder = null
            currentFile?.delete()
            currentFile = null
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun pause() {
        try {
            mediaRecorder?.pause()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun resume() {
        try {
            mediaRecorder?.resume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}