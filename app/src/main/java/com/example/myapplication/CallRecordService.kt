

package com.example.myapplication

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CallRecordService : Service() {
    private var recorder: MediaRecorder? = null
    private var fileName: String? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        startRecording()
        return START_STICKY
    }

    private fun startRecording() {
        val filePath = Environment.getExternalStorageDirectory().path
        val file = File(filePath, "/CallRecordings")
        if (!file.exists()) {
            file.mkdirs()
        }

        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val currentDateandTime: String = sdf.format(Date())

        fileName = "${file.absolutePath}/call_$currentDateandTime.m4a"
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000) // 128 kbps bitrate
            setAudioSamplingRate(44100) // 44.1 kHz sampling rate
            setOutputFile(fileName)

            try {
                prepare()
                start()
                Log.d("CallRecordService", "Recording started successfully. File: $fileName")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }

    private fun stopRecording() {
        recorder?.apply {
            stop()
            release()
        }
        recorder = null
        uploadRecording()
    }

    private fun uploadRecording() {
        if (fileName != null) {
            val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadWorker>()
                .setInputData(workDataOf("FILE_PATH" to fileName))
                .build()
            WorkManager.getInstance(applicationContext).enqueue(uploadWorkRequest)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
