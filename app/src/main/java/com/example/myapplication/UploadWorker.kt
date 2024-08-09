package com.example.myapplication

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

class UploadWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val filePath = inputData.getString("FILE_PATH") ?: return Result.failure()
        val file = File(filePath)
        if (file.exists()) {
            val awsCredentials = BasicAWSCredentials("", "")

            // Using the recommended constructor
            val s3Client = AmazonS3Client(awsCredentials, Region.getRegion(Regions.AP_SOUTH_1))

            val transferUtility = TransferUtility.builder()
                .s3Client(s3Client)
                .context(applicationContext)
                .build()

            val uploadObserver = transferUtility.upload("", file.name, file)

            var uploadResult: Result = Result.failure()

            uploadObserver.setTransferListener(object : TransferListener {
                override fun onStateChanged(id: Int, state: TransferState?) {
                    if (state == TransferState.COMPLETED) {
                        uploadResult = if (file.delete()) {
                            Result.success()
                        } else {
                            Result.failure()
                        }
                    } else if (state == TransferState.FAILED) {
                        uploadResult = Result.failure()
                    }
                }

                override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                    // Handle progress
                }

                override fun onError(id: Int, ex: Exception?) {
                    // Handle error
                    uploadResult = Result.failure()
                }
            })

            // Wait for the upload to finish
            while (uploadObserver.state != TransferState.COMPLETED && uploadObserver.state != TransferState.FAILED) {
                Thread.sleep(100)
            }

            return uploadResult
        }
        return Result.failure()
    }
}
