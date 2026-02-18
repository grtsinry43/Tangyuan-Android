package com.qingshuige.tangyuan.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.qingshuige.tangyuan.BuildConfig
import com.qingshuige.tangyuan.utils.UIUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class UpdateCoordinator(
    private val activity: ComponentActivity
) {
    var updateDialogInfo by mutableStateOf<UpdateInfo?>(null)
        private set
    var showInstallPermissionDialog by mutableStateOf(false)
        private set
    var showReadyInstallDialog by mutableStateOf(false)
        private set
    var showDownloadProgressDialog by mutableStateOf(false)
        private set
    var downloadProgressValue by mutableStateOf(0f)
        private set
    var downloadProgressText by mutableStateOf("准备下载…")
        private set
    var pendingInstallFile: File? by mutableStateOf(null)
        private set

    private var currentDownloadId: Long = -1L
    private var currentUpdateInfo: UpdateInfo? = null
    private var currentMirrorIndex: Int = 0
    private var targetApkFile: File? = null
    private var receiverRegistered = false
    private var downloadInBackground = false
    private var progressPollingJob: Job? = null
    private var waitingInstallPermissionResult = false

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
            if (downloadId != currentDownloadId || downloadId == -1L) return
            handleDownloadResult(downloadId)
        }
    }

    fun startCheckUpdates() {
        activity.lifecycleScope.launch {
            runCatching {
                AppUpdateManager.checkLatestVersion(BuildConfig.VERSION_NAME)
            }.onSuccess { info ->
                if (info != null && !activity.isFinishing && !activity.isDestroyed) {
                    updateDialogInfo = info
                }
            }
        }
    }

    fun onResume() {
        val file = pendingInstallFile
        if (waitingInstallPermissionResult && file != null && canInstallUnknownApps()) {
            waitingInstallPermissionResult = false
            installApk(file)
        }
        reconcileDownloadCompletion()
    }

    fun onDestroy() {
        stopProgressPolling()
        unregisterDownloadReceiverIfNeeded()
    }

    fun dismissUpdateDialog() {
        updateDialogInfo = null
    }

    fun confirmUpdate(info: UpdateInfo) {
        updateDialogInfo = null
        startUpdateDownload(info, 0)
    }

    fun dismissInstallPermissionDialog() {
        showInstallPermissionDialog = false
    }

    fun goAuthorizeInstallPermission() {
        showInstallPermissionDialog = false
        waitingInstallPermissionResult = true
        val installIntent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${activity.packageName}")
        )
        activity.startActivity(installIntent)
    }

    fun dismissReadyInstallDialog() {
        showReadyInstallDialog = false
    }

    fun installNowFromDialog() {
        showReadyInstallDialog = false
        pendingInstallFile?.let { promptInstall(it) }
    }

    fun hideDownloadToBackground() {
        downloadInBackground = true
        showDownloadProgressDialog = false
        UIUtils.showSuccess("已转为后台下载，可在通知栏查看进度")
    }

    private fun startUpdateDownload(info: UpdateInfo, mirrorIndex: Int, keepBackgroundMode: Boolean = false) {
        val url = info.mirrorUrls.getOrNull(mirrorIndex) ?: return
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val apkFile = File(
            activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "Tangyuan-${info.versionName}.apk"
        )
        if (apkFile.exists()) apkFile.delete()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Tangyuan 更新下载")
            .setDescription("下载中（源 ${mirrorIndex + 1}/${info.mirrorUrls.size}）")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationUri(Uri.fromFile(apkFile))

        currentUpdateInfo = info
        currentMirrorIndex = mirrorIndex
        targetApkFile = apkFile
        if (!keepBackgroundMode) {
            downloadInBackground = false
            showDownloadProgressDialog = true
        }
        downloadProgressValue = 0f
        downloadProgressText = "开始下载（源 ${mirrorIndex + 1}/${info.mirrorUrls.size}）"
        currentDownloadId = downloadManager.enqueue(request)
        startProgressPolling(currentDownloadId)
        registerDownloadReceiverIfNeeded()
        UIUtils.showSuccess("开始下载更新")
    }

    private fun handleDownloadResult(downloadId: Long) {
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            if (statusIndex < 0) return
            when (cursor.getInt(statusIndex)) {
                DownloadManager.STATUS_SUCCESSFUL -> onDownloadSuccessful()
                DownloadManager.STATUS_FAILED -> switchToNextMirror("当前下载源失败")
            }
        }
    }

    private fun promptInstall(file: File) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !canInstallUnknownApps()) {
            pendingInstallFile = file
            showInstallPermissionDialog = true
            return
        }
        waitingInstallPermissionResult = false
        installApk(file)
    }

    private fun canInstallUnknownApps(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            activity.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    private fun installApk(file: File) {
        val apkUri = FileProvider.getUriForFile(
            activity,
            "${activity.packageName}.fileprovider",
            file
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        activity.startActivity(installIntent)
    }

    private fun registerDownloadReceiverIfNeeded() {
        if (receiverRegistered) return
        val filter = android.content.IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity.registerReceiver(downloadReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            activity.registerReceiver(downloadReceiver, filter)
        }
        receiverRegistered = true
    }

    private fun unregisterDownloadReceiverIfNeeded() {
        if (!receiverRegistered) return
        runCatching { activity.unregisterReceiver(downloadReceiver) }
        receiverRegistered = false
    }

    private fun reconcileDownloadCompletion() {
        if (showReadyInstallDialog) return
        val file = targetApkFile
        if (file != null && file.exists()) {
            pendingInstallFile = file
            showReadyInstallDialog = true
            return
        }

        val downloadId = currentDownloadId
        if (downloadId == -1L) return
        val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val snapshot = queryDownloadSnapshot(downloadManager, downloadId) ?: return
        if (snapshot.status == DownloadManager.STATUS_SUCCESSFUL) {
            val apk = targetApkFile
            if (apk != null && apk.exists()) {
                pendingInstallFile = apk
                showReadyInstallDialog = true
            }
        }
    }

    private fun startProgressPolling(downloadId: Long) {
        stopProgressPolling()
        progressPollingJob = activity.lifecycleScope.launch {
            val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val startedAtMs = System.currentTimeMillis()
            var lastProgressBytes = 0L
            var lastProgressAtMs = startedAtMs
            var prevSampleBytes = 0L
            var prevSampleAtMs = startedAtMs
            var pendingSinceMs = startedAtMs
            var slowSinceMs: Long? = null

            while (isActive) {
                val snapshot = queryDownloadSnapshot(downloadManager, downloadId) ?: break
                val total = snapshot.totalBytes
                val downloaded = snapshot.downloadedBytes
                val now = System.currentTimeMillis()
                downloadProgressValue = if (total > 0L) {
                    (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                downloadProgressText = when (snapshot.status) {
                    DownloadManager.STATUS_PENDING -> "排队中…"
                    DownloadManager.STATUS_RUNNING -> {
                        if (total > 0L) "下载中：${(downloadProgressValue * 100).toInt()}%" else "下载中…"
                    }
                    DownloadManager.STATUS_PAUSED -> "已暂停，等待网络…"
                    DownloadManager.STATUS_SUCCESSFUL -> "下载完成，准备安装…"
                    DownloadManager.STATUS_FAILED -> "下载失败，正在切换镜像…"
                    else -> "下载中…"
                }

                when (snapshot.status) {
                    DownloadManager.STATUS_PENDING -> {
                        if (now - pendingSinceMs > UpdateDownloadPolicy.PENDING_TIMEOUT_MS) {
                            switchToNextMirror("下载迟迟未开始")
                            break
                        }
                    }
                    DownloadManager.STATUS_RUNNING -> {
                        if (downloaded > lastProgressBytes) {
                            lastProgressBytes = downloaded
                            lastProgressAtMs = now
                        } else if (now - lastProgressAtMs > UpdateDownloadPolicy.NO_PROGRESS_TIMEOUT_MS) {
                            switchToNextMirror("下载长时间无进度")
                            break
                        }
                        val dt = (now - prevSampleAtMs).coerceAtLeast(1L)
                        val bytesDelta = (downloaded - prevSampleBytes).coerceAtLeast(0L)
                        val speedBps = bytesDelta * 1000L / dt
                        if (speedBps in 1 until UpdateDownloadPolicy.SLOW_SPEED_THRESHOLD_BPS) {
                            if (slowSinceMs == null) slowSinceMs = now
                            if (now - (slowSinceMs ?: now) > UpdateDownloadPolicy.SLOW_TIMEOUT_MS) {
                                switchToNextMirror("下载速度过慢")
                                break
                            }
                        } else {
                            slowSinceMs = null
                        }
                        prevSampleAtMs = now
                        prevSampleBytes = downloaded
                        pendingSinceMs = now
                    }
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        onDownloadSuccessful()
                        break
                    }
                    DownloadManager.STATUS_FAILED -> {
                        switchToNextMirror("当前下载源失败")
                        break
                    }
                }
                if (snapshot.status == DownloadManager.STATUS_SUCCESSFUL) break
                delay(UpdateDownloadPolicy.POLL_INTERVAL_MS)
            }
        }
    }

    private fun stopProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = null
    }

    private fun onDownloadSuccessful() {
        stopProgressPolling()
        showDownloadProgressDialog = false
        val file = targetApkFile
        if (file != null && file.exists()) {
            pendingInstallFile = file
            showReadyInstallDialog = true
        } else {
            UIUtils.showError("更新包不存在，请重试")
        }
    }

    private fun switchToNextMirror(reason: String) {
        stopProgressPolling()
        val info = currentUpdateInfo
        val nextMirror = currentMirrorIndex + 1
        if (info != null && nextMirror < info.mirrorUrls.size) {
            val downloadManager = activity.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            if (currentDownloadId != -1L) {
                runCatching { downloadManager.remove(currentDownloadId) }
            }
            downloadProgressText = "$reason，正在切换下载源…"
            UIUtils.showError("$reason，切换到镜像重试")
            startUpdateDownload(
                info = info,
                mirrorIndex = nextMirror,
                keepBackgroundMode = downloadInBackground
            )
        } else {
            showDownloadProgressDialog = false
            UIUtils.showError("$reason，且无可用镜像")
        }
    }

    private fun queryDownloadSnapshot(
        downloadManager: DownloadManager,
        downloadId: Long
    ): DownloadSnapshot? {
        val query = DownloadManager.Query().setFilterById(downloadId)
        downloadManager.query(query).use { cursor ->
            if (!cursor.moveToFirst()) return null
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val downloadedIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val totalIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            if (statusIndex < 0 || downloadedIndex < 0 || totalIndex < 0) return null
            return DownloadSnapshot(
                status = cursor.getInt(statusIndex),
                downloadedBytes = cursor.getLong(downloadedIndex),
                totalBytes = cursor.getLong(totalIndex)
            )
        }
    }

    private data class DownloadSnapshot(
        val status: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    )
}
