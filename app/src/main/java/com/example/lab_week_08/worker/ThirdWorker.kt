package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object {
        const val INPUT_DATA_ID = "third_worker_id"
    }

    override fun doWork(): Result {
        Thread.sleep(2000) // simulasi proses
        return Result.success()
    }
}
