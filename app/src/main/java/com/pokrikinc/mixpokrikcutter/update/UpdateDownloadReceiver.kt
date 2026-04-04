package com.pokrikinc.mixpokrikcutter.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class UpdateDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            return
        }

        val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (downloadId == -1L) {
            return
        }

        if (AppUpdateManager.handleDownloadCompleted(context, downloadId)) {
            AppUpdateManager.showUpdateReadyNotification(context)
        }
    }
}
