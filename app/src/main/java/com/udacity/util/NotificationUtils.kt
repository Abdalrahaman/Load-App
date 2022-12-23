package com.udacity.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.udacity.DetailActivity
import com.udacity.MainActivity
import com.udacity.MainActivity.Companion.DownloadType
import com.udacity.R

private val NOTIFICATION_ID = 0
private val REQUEST_CODE = 0
private val FLAGS = 0

fun NotificationManager.sendNotification(
    downloadType: DownloadType,
    status: Int,
    applicationContext: Context
) {
    val contentIntent = Intent(applicationContext, MainActivity::class.java)
    val contentPendingIntent = PendingIntent.getActivity(
        applicationContext,
        NOTIFICATION_ID,
        contentIntent,
        FLAGS
    )

    val seeChangesIntent = Intent(applicationContext, DetailActivity::class.java).apply {
        putExtra("fileName", applicationContext.getString(downloadType.fileName))
        putExtra("status", status)
    }
    val seeChangesPendingIntent: PendingIntent = PendingIntent.getActivity(
        applicationContext,
        REQUEST_CODE,
        seeChangesIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = NotificationCompat.Builder(
        applicationContext,
        applicationContext.getString(R.string.channel_id)
    )

        .setSmallIcon(R.drawable.ic_assistant_black_24dp)
        .setContentTitle(
            applicationContext
                .getString(R.string.notification_title)
        )
        .setContentText(applicationContext.getText(R.string.notification_description).toString())

        .setContentIntent(contentPendingIntent)
        .setAutoCancel(true)

        .addAction(
            0,
            applicationContext.getString(R.string.notification_button),
            seeChangesPendingIntent
        )
        .setPriority(NotificationCompat.PRIORITY_HIGH)

    notify(NOTIFICATION_ID, builder.build())
}

fun NotificationManager.cancelNotifications() {
    cancelAll()
}