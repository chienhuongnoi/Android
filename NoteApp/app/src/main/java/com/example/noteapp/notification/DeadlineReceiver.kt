package com.example.noteapp.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.noteapp.R

class DeadlineReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val taskName = intent.getStringExtra("taskName") ?: "Nhiệm vụ"
        val taskId = intent.getIntExtra("taskId", 0)

        val nm = context.getSystemService(NotificationManager::class.java)

        // Tạo channel nếu cần
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "task_deadline_channel",
                "Nhắc nhở công việc",
                NotificationManager.IMPORTANCE_HIGH
            )
            nm.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, "task_deadline_channel")
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle("Đã đến hạn")
            .setContentText("Nhiệm vụ \"$taskName\" đã đến hạn.")
            .setAutoCancel(true)
            .build()

        nm.notify(taskId, notification)
    }
}