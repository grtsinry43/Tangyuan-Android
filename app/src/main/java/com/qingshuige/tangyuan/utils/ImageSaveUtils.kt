package com.qingshuige.tangyuan.utils

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImageSaveUtils {
    
    suspend fun saveImageToGallery(
        context: Context,
        imageUrl: String,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
                
            val drawable = imageLoader.execute(request).drawable
            val bitmap = (drawable as? BitmapDrawable)?.bitmap 
                ?: return@withContext Result.failure(Exception("无法获取图片"))
            
            val displayName = fileName ?: "Tangyuan_${System.currentTimeMillis()}.jpg"
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToMediaStore(context, bitmap, displayName)
            } else {
                saveImageToExternalStorage(bitmap, displayName)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun saveImageToMediaStore(
        context: Context,
        bitmap: Bitmap,
        displayName: String
    ): Result<String> {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Tangyuan")
        }
        
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return Result.failure(Exception("无法创建文件"))
        
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            Result.success("图片已保存到相册")
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
    
    private fun saveImageToExternalStorage(
        bitmap: Bitmap,
        displayName: String
    ): Result<String> {
        val picturesDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Tangyuan"
        )
        
        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }
        
        val imageFile = File(picturesDir, displayName)
        
        return try {
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            Result.success("图片已保存到 ${imageFile.absolutePath}")
        } catch (e: IOException) {
            Result.failure(e)
        }
    }
}