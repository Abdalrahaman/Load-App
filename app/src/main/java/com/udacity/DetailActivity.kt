package com.udacity

import android.app.DownloadManager
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.util.cancelNotifications
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var fileName: String
    private var status: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotifications()

        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        intent.let {
            fileName = it.getStringExtra("fileName").toString()
            status = it.getIntExtra("status", -1)
        }

        tvFileName.text = fileName
        tvStatus.text = when (status) {
            DownloadManager.STATUS_SUCCESSFUL -> getString(R.string.download_successful)
            else -> getString(R.string.download_failed)
        }
        tvStatus.setTextColor(if (status == DownloadManager.STATUS_SUCCESSFUL) getColor(R.color.colorPrimary) else Color.RED)

        fab.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
