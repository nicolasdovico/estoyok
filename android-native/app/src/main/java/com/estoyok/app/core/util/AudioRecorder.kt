package com.estoyok.app.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.io.File

class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(): File? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("AudioRecorder", "Cannot start recording: RECORD_AUDIO permission not granted!")
            return null
        }

        val file = File(context.cacheDir, "sos_ambient_${System.currentTimeMillis()}.m4a")
        outputFile = file

        try {
            val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(64000)
                setAudioSamplingRate(44100)
                setOutputFile(file.absolutePath)
                prepare()
                start()
            }
            recorder = mediaRecorder
            Log.i("AudioRecorder", "Audio recording started successfully at: ${file.absolutePath}")
            return file
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Exception starting audio recording: ${e.message}", e)
            try {
                recorder?.release()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            recorder = null
            return null
        }
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
            Log.i("AudioRecorder", "Audio recording stopped and saved: ${outputFile?.absolutePath} (size: ${outputFile?.length()} bytes)")
        } catch (e: Exception) {
            Log.e("AudioRecorder", "Error stopping audio recorder: ${e.message}", e)
        } finally {
            recorder = null
        }
    }
}
