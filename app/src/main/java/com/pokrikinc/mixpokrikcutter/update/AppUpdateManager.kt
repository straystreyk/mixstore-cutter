package com.pokrikinc.mixpokrikcutter.update

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pokrikinc.mixpokrikcutter.BuildConfig
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.model.AppUpdateInfo
import com.pokrikinc.mixpokrikcutter.data.remote.IAppUpdateApi
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object AppUpdateManager {
    private const val TAG = "AppUpdate"
    private const val UPDATE_METADATA_FILE = "version.json"
    private const val UPDATE_RETROFIT_BASE_URL = "https://127.0.0.1/"
    const val ACTION_INSTALL_DOWNLOADED_UPDATE =
        "com.pokrikinc.mixpokrikcutter.action.INSTALL_DOWNLOADED_UPDATE"
    private const val UPDATE_NOTIFICATION_CHANNEL_ID = "app_update_ready"
    private const val UPDATE_NOTIFICATION_ID = 1001

    data class DownloadProgress(
        val status: Int,
        val downloadedBytes: Long,
        val totalBytes: Long,
        val reason: Int
    ) {
        val progressPercent: Int
            get() = if (downloadedBytes <= 0L || totalBytes <= 0L) {
                0
            } else {
                ((downloadedBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
            }

        val isRunning: Boolean
            get() = status == DownloadManager.STATUS_RUNNING ||
                status == DownloadManager.STATUS_PAUSED ||
                status == DownloadManager.STATUS_PENDING

        val isSuccessful: Boolean
            get() = status == DownloadManager.STATUS_SUCCESSFUL

        val isFailed: Boolean
            get() = status == DownloadManager.STATUS_FAILED
    }

    private val client = OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor { message -> Log.d(TAG, message) }.apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
        }
        .build()
    private val retrofit = Retrofit.Builder()
        .baseUrl(UPDATE_RETROFIT_BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api = retrofit.create(IAppUpdateApi::class.java)

    suspend fun checkForUpdate(baseUrl: String): Result<AppUpdateInfo?> = runCatching {
        val metadataUrl = buildMetadataUrl(baseUrl)
        Log.d(TAG, "Requesting update metadata from $metadataUrl")
        val updateInfo = api.getUpdateInfo(metadataUrl)
        val resolvedApkUrl = resolveApkUrl(baseUrl, updateInfo.apkUrl)
        val normalizedInfo = updateInfo.copy(apkUrl = resolvedApkUrl)
        Log.d(
            TAG,
            "Received metadata. versionCode=${normalizedInfo.versionCode}, " +
                "versionName=${normalizedInfo.versionName}, apkUrl=${normalizedInfo.apkUrl}"
        )

        if (normalizedInfo.versionCode > BuildConfig.VERSION_CODE) {
            normalizedInfo
        } else {
            null
        }
    }

    fun enqueueUpdateDownload(context: Context, updateInfo: AppUpdateInfo): Long {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        getPendingDownloadProgress(context)?.let { progress ->
            if (progress.isRunning) {
                return PreferenceManager.getUpdateDownloadId()
            }
        }
        cleanupPendingApkFile(context)
        val fileName = buildApkFileName(updateInfo)
        val request = DownloadManager.Request(Uri.parse(updateInfo.apkUrl))
            .setTitle("MixCutter ${updateInfo.versionName}")
            .setDescription("Загрузка обновления приложения")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)

        val downloadId = downloadManager.enqueue(request)
        PreferenceManager.setUpdateDownloadId(downloadId)
        PreferenceManager.setPendingUpdateApkName(fileName)
        return downloadId
    }

    fun handleDownloadCompleted(context: Context, downloadId: Long): Boolean {
        if (downloadId != PreferenceManager.getUpdateDownloadId()) {
            return false
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor.use {
            if (!it.moveToFirst()) {
                PreferenceManager.clearPendingUpdate()
                return false
            }

            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            if (status != DownloadManager.STATUS_SUCCESSFUL) {
                PreferenceManager.clearPendingUpdate()
                return false
            }

            val downloadedBytes = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val totalBytes = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )
            Log.d(
                TAG,
                "Download completed. downloadedBytes=$downloadedBytes totalBytes=$totalBytes"
            )
        }

        val apkFile = getPendingApkFile(context)
        if (apkFile == null || !isApkValid(context, apkFile)) {
            Log.e(TAG, "Downloaded APK is invalid: ${apkFile?.absolutePath}")
            PreferenceManager.clearPendingUpdate()
            return false
        }

        return true
    }

    fun getPendingDownloadProgress(context: Context): DownloadProgress? {
        val downloadId = PreferenceManager.getUpdateDownloadId()
        if (downloadId == -1L) {
            return null
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        cursor.use {
            if (!it.moveToFirst()) {
                return null
            }

            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val downloadedBytes = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            )
            val totalBytes = it.getLong(
                it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            )
            val reason = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))

            return DownloadProgress(
                status = status,
                downloadedBytes = downloadedBytes,
                totalBytes = totalBytes,
                reason = reason
            )
        }
    }

    fun requestInstallIfReady(context: Context, openSettingsIfNeeded: Boolean = false): Boolean {
        val downloadProgress = getPendingDownloadProgress(context)
        if (downloadProgress != null && !downloadProgress.isSuccessful) {
            if (downloadProgress.isFailed) {
                PreferenceManager.clearPendingUpdate()
            }
            return false
        }

        val apkUri = getPendingInstallUri(context) ?: return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !canRequestPackageInstallsSafely(context)
        ) {
            if (openSettingsIfNeeded) {
                openUnknownSourcesSettings(context)
            }
            return false
        }

        installApk(context, apkUri)
        return true
    }

    fun openInstallPermissionSettings(context: Context) {
        openUnknownSourcesSettings(context)
    }

    fun shouldRequestInstallPermission(context: Context): Boolean {
        val downloadProgress = getPendingDownloadProgress(context)
        if (downloadProgress != null && !downloadProgress.isSuccessful) {
            return false
        }

        if (getPendingInstallUri(context) == null) {
            return false
        }

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !canRequestPackageInstallsSafely(context)
    }

    private fun buildMetadataUrl(baseUrl: String): String {
        val normalizedBaseUrl = ensureTrailingSlash(baseUrl)
        return normalizedBaseUrl + UPDATE_METADATA_FILE
    }

    private fun resolveApkUrl(baseUrl: String, apkUrl: String): String {
        apkUrl.toHttpUrlOrNull()?.let { return it.toString() }

        val baseHttpUrl = ensureTrailingSlash(baseUrl).toHttpUrlOrNull()
            ?: error("Invalid base URL: $baseUrl")

        return baseHttpUrl.resolve(apkUrl)?.toString()
            ?: error("Invalid apkUrl: $apkUrl")
    }

    private fun ensureTrailingSlash(value: String): String =
        value.trim().trimEnd('/') + "/"

    private fun buildApkFileName(updateInfo: AppUpdateInfo): String {
        val versionPart = updateInfo.versionName
            .ifBlank { updateInfo.versionCode.toString() }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
        return "mixcutter-$versionPart.apk"
    }

    private fun getPendingApkFile(context: Context): File? {
        val fileName = PreferenceManager.getPendingUpdateApkName() ?: return null
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
        return File(dir, fileName)
    }

    private fun getPendingInstallUri(context: Context): Uri? {
        val apkFile = getPendingApkFile(context) ?: return null
        if (!apkFile.exists()) {
            PreferenceManager.clearPendingUpdate()
            return null
        }

        if (!isApkValid(context, apkFile)) {
            Log.e(TAG, "Refusing to install invalid APK file: ${apkFile.absolutePath}")
            PreferenceManager.clearPendingUpdate()
            return null
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            apkFile
        )
        Log.d(TAG, "Using FileProvider URI for install: $apkUri")
        return apkUri
    }

    private fun installApk(context: Context, apkUri: Uri) {
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(installIntent)
        PreferenceManager.clearPendingUpdate()
    }

    private fun isApkValid(context: Context, apkFile: File): Boolean {
        if (!apkFile.exists() || apkFile.length() <= 0L) {
            return false
        }

        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageArchiveInfo(
                apkFile.absolutePath,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
        }

        Log.d(
            TAG,
            "APK validation. file=${apkFile.absolutePath} size=${apkFile.length()} valid=${packageInfo != null}"
        )
        return packageInfo != null
    }

    fun hasPendingUpdate(): Boolean = PreferenceManager.getPendingUpdateApkName() != null

    fun showUpdateReadyNotification(context: Context) {
        ensureNotificationChannel(context)

        val installIntent = Intent(context, MainActivity::class.java).apply {
            action = ACTION_INSTALL_DOWNLOADED_UPDATE
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val pendingIntent = PendingIntent.getActivity(context, 0, installIntent, flags)

        val notification = NotificationCompat.Builder(context, UPDATE_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.update_ready_notification_title))
            .setContentText(context.getString(R.string.update_ready_notification_text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(UPDATE_NOTIFICATION_ID, notification)
        }.onFailure {
            Log.e(TAG, "Failed to show update ready notification", it)
        }
    }

    fun getDownloadStatusMessage(context: Context): String? {
        val progress = getPendingDownloadProgress(context) ?: return null
        return when (progress.status) {
            DownloadManager.STATUS_PENDING -> "Ожидание начала загрузки"
            DownloadManager.STATUS_RUNNING -> {
                if (progress.totalBytes > 0L) {
                    "Скачано: ${progress.progressPercent}%"
                } else {
                    "Загрузка обновления"
                }
            }
            DownloadManager.STATUS_PAUSED -> "Загрузка приостановлена, будет повтор"
            DownloadManager.STATUS_FAILED -> "Ошибка загрузки обновления"
            DownloadManager.STATUS_SUCCESSFUL -> "Загрузка завершена"
            else -> null
        }
    }

    private fun canRequestPackageInstallsSafely(context: Context): Boolean =
        runCatching { context.packageManager.canRequestPackageInstalls() }
            .onFailure { Log.e(TAG, "Failed to check package install permission", it) }
            .getOrDefault(false)

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            UPDATE_NOTIFICATION_CHANNEL_ID,
            context.getString(R.string.update_ready_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.update_ready_notification_channel_description)
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun cleanupPendingApkFile(context: Context) {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val oldFiles = dir?.listFiles { file ->
            file.isFile && file.name.startsWith("mixcutter-") && file.name.endsWith(".apk")
        }.orEmpty()

        oldFiles.forEach { apkFile ->
            if (!apkFile.delete()) {
                Log.w(TAG, "Failed to delete old update file: ${apkFile.absolutePath}")
            }
        }
        PreferenceManager.clearPendingUpdate()
    }

    private fun openUnknownSourcesSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
