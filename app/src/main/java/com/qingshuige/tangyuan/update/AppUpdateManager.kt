package com.qingshuige.tangyuan.update

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class UpdateInfo(
    val versionName: String,
    val releaseName: String,
    val releaseNotes: String,
    val publishedAt: String?,
    val apkUrl: String,
    val mirrorUrls: List<String>
)

object AppUpdateManager {
    private val client = OkHttpClient()
    private val gson = Gson()
    private const val LATEST_RELEASE_API =
        "https://api.github.com/repos/grtsinry43/Tangyuan-Android/releases/latest"

    suspend fun checkLatestVersion(currentVersionName: String): UpdateInfo? =
        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(LATEST_RELEASE_API)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", "Tangyuan-Android")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body?.string() ?: return@withContext null
                val latest = gson.fromJson(body, GithubRelease::class.java) ?: return@withContext null
                val latestVersion = latest.tagName?.trim()?.removePrefix("v") ?: return@withContext null
                if (compareVersion(latestVersion, currentVersionName) <= 0) return@withContext null

                val apkAsset = latest.assets
                    ?.firstOrNull { it.name?.endsWith(".apk", ignoreCase = true) == true }
                    ?: latest.assets?.firstOrNull {
                        it.contentType?.contains("android.package-archive", ignoreCase = true) == true
                    }
                    ?: return@withContext null

                val apkUrl = apkAsset.browserDownloadUrl ?: return@withContext null
                UpdateInfo(
                    versionName = latestVersion,
                    releaseName = latest.name ?: "发现新版本",
                    releaseNotes = latest.body ?: "",
                    publishedAt = latest.publishedAt,
                    apkUrl = apkUrl,
                    mirrorUrls = buildMirrorUrls(apkUrl)
                )
            }
        }

    private fun buildMirrorUrls(githubUrl: String): List<String> {
        return listOf(
            githubUrl,
            "https://ghfast.top/$githubUrl",
            "https://gh-proxy.com/$githubUrl"
        ).distinct()
    }

    private fun compareVersion(remote: String, local: String): Int {
        val remoteParts = parseVersion(remote)
        val localParts = parseVersion(local)
        val max = maxOf(remoteParts.size, localParts.size)
        for (i in 0 until max) {
            val r = remoteParts.getOrElse(i) { 0 }
            val l = localParts.getOrElse(i) { 0 }
            if (r != l) return r - l
        }
        return 0
    }

    private fun parseVersion(version: String): List<Int> {
        return version
            .trim()
            .removePrefix("v")
            .split('.')
            .mapNotNull { part ->
                part.takeWhile { it.isDigit() }.toIntOrNull()
            }
            .ifEmpty { listOf(0) }
    }

    private data class GithubRelease(
        @SerializedName("tag_name") val tagName: String?,
        val name: String?,
        val body: String?,
        @SerializedName("published_at") val publishedAt: String?,
        val assets: List<GithubAsset>?
    )

    private data class GithubAsset(
        val name: String?,
        @SerializedName("browser_download_url") val browserDownloadUrl: String?,
        @SerializedName("content_type") val contentType: String?
    )
}
