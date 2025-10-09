package com.qingshuige.tangyuan.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.core.graphics.scale

/**
 * 二维码生成工具类
 */
object QRCodeUtils {
    /**
     * 生成二维码Bitmap
     * @param content 二维码内容
     * @param size 二维码尺寸（宽高相同）
     * @param foregroundColor 前景色（默认黑色）
     * @param backgroundColor 背景色（默认白色）
     * @return 生成的二维码Bitmap
     */
    fun generateQRCode(
        content: String,
        size: Int = 512,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE
    ): Bitmap? {
        return try {
            val hints = hashMapOf<EncodeHintType, Any>().apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H)
                put(EncodeHintType.MARGIN, 1)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

            val bitmap = createBitmap(size, size)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y]) foregroundColor else backgroundColor
                }
            }

            bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 生成带Logo的二维码
     * @param content 二维码内容
     * @param size 二维码尺寸
     * @param logo Logo图片
     * @param logoSize Logo尺寸（默认为二维码尺寸的1/5）
     * @return 生成的二维码Bitmap
     */
    fun generateQRCodeWithLogo(
        content: String,
        size: Int = 512,
        logo: Bitmap?,
        logoSize: Int = size / 5
    ): Bitmap? {
        val qrCode = generateQRCode(content, size) ?: return null

        if (logo == null) return qrCode

        return try {
            val combinedBitmap = qrCode.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = android.graphics.Canvas(combinedBitmap)

            // 缩放Logo
            val scaledLogo = logo.scale(logoSize, logoSize)

            // 计算Logo位置（居中）
            val left = (size - logoSize) / 2f
            val top = (size - logoSize) / 2f

            // 绘制Logo
            canvas.drawBitmap(scaledLogo, left, top, null)

            combinedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            qrCode
        }
    }
}
