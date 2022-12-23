package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import com.udacity.util.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager

    private var downloadType = DownloadType.CUSTOM
    private var downloadStatus = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createChannel(
            getString(R.string.channel_id),
            getString(R.string.channel_name)
        )

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        custom_button.setOnClickListener {
            if (URLUtil.isValidUrl(downloadType.url))
                download()
            else
                Toast.makeText(
                    this,
                    getString(R.string.nothing_selected_message),
                    Toast.LENGTH_SHORT
                ).show()
        }

        textFieldCustomUrl.editText?.doOnTextChanged { inputText, _, _, _ ->
            downloadType = DownloadType.CUSTOM
            downloadType.url = inputText.toString()
        }

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioGlide -> {
                    clearCustomUrlInput()
                    downloadType = DownloadType.GLIDE
                }
                R.id.radioUdacity -> {
                    clearCustomUrlInput()
                    downloadType = DownloadType.UDACITY
                }
                R.id.radioRetrofit -> {
                    clearCustomUrlInput()
                    downloadType = DownloadType.RETROFIT
                }
                else -> {
                    Log.e("MainActivity", "")
                }
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadID == id) {

                val action = intent.action

                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    val query = DownloadManager.Query()
                    query.setFilterById(intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0));
                    val downloadManager =
                        context!!.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                    val cursor: Cursor = downloadManager.query(query)

                    if (cursor.moveToFirst()) {
                        if (cursor.count > 0) {
                            downloadStatus =
                                cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                        }
                    }
                }

                notificationManager = ContextCompat.getSystemService(
                    this@MainActivity,
                    NotificationManager::class.java
                ) as NotificationManager

                notificationManager.sendNotification(
                    downloadType,
                    downloadStatus,
                    this@MainActivity
                )
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(downloadType.url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = getString(R.string.app_description)

            val notificationManager = getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }
    }

    private fun clearCustomUrlInput() {
        textFieldCustomUrl.editText?.text?.clear()
    }

    companion object {
        enum class DownloadType(var url: String, val fileName: Int) {
            GLIDE(
                "https://github.com/bumptech/glide/archive/refs/heads/master.zip",
                R.string.glide
            ),
            UDACITY(
                "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip",
                R.string.load_app
            ),
            RETROFIT(
                "https://github.com/square/retrofit/archive/refs/heads/master.zip",
                R.string.retrofit
            ),
            CUSTOM(
                "",
                R.string.custom_url
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }
}
