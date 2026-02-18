package com.qingshuige.tangyuan.update

object UpdateDownloadPolicy {
    const val PENDING_TIMEOUT_MS = 15_000L
    const val NO_PROGRESS_TIMEOUT_MS = 12_000L
    const val SLOW_TIMEOUT_MS = 18_000L
    const val SLOW_SPEED_THRESHOLD_BPS = 24 * 1024L
    const val POLL_INTERVAL_MS = 600L
}
