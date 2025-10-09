package com.qingshuige.tangyuan.utils

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.qingshuige.tangyuan.R
import com.qingshuige.tangyuan.model.PostCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

/**
 * 分享卡片生成工具类
 * 完全符合糖原社区设计系统的分享卡片
 */
object ShareCardGenerator {
    // 卡片尺寸
    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1920
    private const val PADDING = 60

    // 颜色定义（来自糖原设计系统）
    private const val PRIMARY_COLOR = 0xFF2E7CF6.toInt()
    private const val SURFACE_COLOR = 0xFFFFFFFF.toInt()
    private const val ON_SURFACE_COLOR = 0xFF1A1C1E.toInt()
    private const val ON_SURFACE_VARIANT_COLOR = 0xFF6B7280.toInt()
    private const val TERTIARY_COLOR = 0xFF7B68EE.toInt()
    private const val OUTLINE_COLOR = 0xFFE5E7EB.toInt()

    // 圆角定义（来自糖原设计系统，dp转px：1dp≈3px at 1080px width）
    private const val RADIUS_SMALL = 24f    // 8dp
    private const val RADIUS_LARGE = 48f    // 16dp

    /**
     * 生成分享卡片
     */
    suspend fun generateShareCard(
        context: Context,
        postCard: PostCard,
        deepLink: String
    ): Bitmap = withContext(Dispatchers.IO) {
        val bitmap = createBitmap(CARD_WIDTH, CARD_HEIGHT)
        val canvas = Canvas(bitmap)

//        // 加载作者头像
//        val avatarBitmap = loadAvatarBitmap(context, postCard.authorAvatar)

        // 绘制背景
        drawBackground(canvas)

        // 绘制Logo水印（使用drawable）
        drawLogoWatermark(context, canvas)

        var currentY = PADDING + 40

        // 绘制作者信息（去掉顶部标题，直接开始）
        currentY = drawAuthorSection(canvas, postCard,currentY)

        currentY += 80

        // 绘制内容卡片
        currentY = drawContentCard(canvas, postCard, currentY)

        currentY += 80

        // 绘制二维码区域
        drawQRCodeSection(canvas, deepLink, currentY)

        bitmap
    }

    /**
     * 加载作者头像
     */
    private suspend fun loadAvatarBitmap(context: Context, avatarUrl: String): Bitmap? {
        return try {
            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(avatarUrl)
                .size(200, 200)
                .build()

            val result = imageLoader.execute(request)
            if (result is SuccessResult) {
                (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 绘制渐变背景
     */
    private fun drawBackground(canvas: Canvas) {
        // 精美的三色渐变
        val gradient = LinearGradient(
            0f, 0f,
            0f, CARD_HEIGHT.toFloat(),
            intArrayOf(
                0xFFF5F8FF.toInt(),  // 极浅蓝
                0xFFFFFFFF.toInt(),  // 纯白
                0xFFFAFAFC.toInt()   // 浅灰蓝
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        canvas.drawRect(
            0f, 0f,
            CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(),
            Paint().apply { shader = gradient }
        )
    }

    /**
     * 绘制Logo水印（使用drawable中的logo图标）
     */
    private fun drawLogoWatermark(context: Context, canvas: Canvas) {
        try {
            // 加载logo drawable
            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)
            if (logoDrawable != null) {
                // 转换为Bitmap
                val logoSize = 800  // 大尺寸水印
                val logoBitmap = logoDrawable.toBitmap(logoSize, logoSize)

                // 创建半透明Paint
                val watermarkPaint = Paint().apply {
                    alpha = 25  // 极度半透明 (约10%)
                    isAntiAlias = true
                }

                // 旋转并绘制Logo水印
                canvas.save()
                canvas.rotate(-15f, CARD_WIDTH / 2f, CARD_HEIGHT / 2f)

                val left = CARD_WIDTH / 2f - logoSize / 2f
                val top = CARD_HEIGHT / 2f - logoSize / 2f

                canvas.drawBitmap(logoBitmap, left, top, watermarkPaint)
                canvas.restore()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 绘制作者信息区域
     */
    private fun drawAuthorSection(canvas: Canvas, postCard: PostCard, startY: Int): Int {
        var currentY = startY

        // 作者信息区域
        val avatarSize = 140
        val avatarLeft = PADDING.toFloat()
        val avatarTop = currentY.toFloat()

//        // 绘制作者头像
//        if (avatarBitmap != null) {
//            // 绘制头像背景圆（阴影）
//            val shadowPaint = Paint().apply {
//                color = 0x25000000
//                maskFilter = BlurMaskFilter(25f, BlurMaskFilter.Blur.NORMAL)
//            }
//            canvas.drawCircle(
//                avatarLeft + avatarSize / 2f + 5f,
//                avatarTop + avatarSize / 2f + 5f,
//                avatarSize / 2f,
//                shadowPaint
//            )
//
//            // 绘制头像边框
//            val borderPaint = Paint().apply {
//                shader = LinearGradient(
//                    avatarLeft, avatarTop,
//                    avatarLeft + avatarSize, avatarTop + avatarSize,
//                    PRIMARY_COLOR,
//                    TERTIARY_COLOR,
//                    Shader.TileMode.CLAMP
//                )
//                style = Paint.Style.STROKE
//                strokeWidth = 8f
//                isAntiAlias = true
//            }
//
//            canvas.drawCircle(
//                avatarLeft + avatarSize / 2f,
//                avatarTop + avatarSize / 2f,
//                avatarSize / 2f,
//                borderPaint
//            )
//
//            // 裁剪并绘制头像
//            canvas.save()
//            val clipPath = Path().apply {
//                addCircle(
//                    avatarLeft + avatarSize / 2f,
//                    avatarTop + avatarSize / 2f,
//                    avatarSize / 2f - 4f,
//                    Path.Direction.CW
//                )
//            }
//            canvas.clipPath(clipPath)
//
//            val scaledAvatar = Bitmap.createScaledBitmap(avatarBitmap, avatarSize, avatarSize, true)
//            canvas.drawBitmap(scaledAvatar, avatarLeft, avatarTop, null)
//            canvas.restore()
//        }

        // 作者名称和分类信息
        val nameLeft = 120f
        var textY = avatarTop + 60f

        // 作者名称
        val namePaint = TextPaint().apply {
            color = ON_SURFACE_COLOR
            textSize = 56f  // titleLarge
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("@${postCard.authorName}", nameLeft, textY, namePaint)
        textY += 80f

        // 分类标签
        val categoryPaint = TextPaint().apply {
            color = TERTIARY_COLOR
            textSize = 38f  // labelMedium
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // 绘制分类标签背景
        val categoryBgPaint = Paint().apply {
            color = 0xFFEDE7FF.toInt()  // tertiaryContainer
            isAntiAlias = true
        }

        val categoryWidth = categoryPaint.measureText(postCard.categoryName) + 52f
        val categoryRect = RectF(
            nameLeft,
            textY - 42f,
            nameLeft + categoryWidth,
            textY + 18f
        )

        canvas.drawRoundRect(categoryRect, RADIUS_SMALL, RADIUS_SMALL, categoryBgPaint)
        canvas.drawText(postCard.categoryName, nameLeft + 26f, textY, categoryPaint)

        currentY += avatarSize + 40

        // 绘制分割线
        val dividerPaint = Paint().apply {
            shader = LinearGradient(
                PADDING.toFloat(), 0f,
                (CARD_WIDTH - PADDING).toFloat(), 0f,
                intArrayOf(0x00_2E7CF6, PRIMARY_COLOR, TERTIARY_COLOR, 0x00_7B68EE),
                null,
                Shader.TileMode.CLAMP
            )
            strokeWidth = 4f
        }

        canvas.drawLine(
            PADDING.toFloat(),
            currentY.toFloat(),
            CARD_WIDTH - PADDING.toFloat(),
            currentY.toFloat(),
            dividerPaint
        )

        return currentY
    }

    /**
     * 绘制内容卡片
     */
    private fun drawContentCard(canvas: Canvas, postCard: PostCard, startY: Int): Int {
        val cardLeft = PADDING.toFloat()
        val cardRight = (CARD_WIDTH - PADDING).toFloat()
        val cardTop = startY.toFloat()
        val cardPadding = 50f

        // 计算卡片高度
        val contentLines = 6
        val cardHeight = cardPadding * 2 + contentLines * 72 + 100

        val cardRect = RectF(cardLeft, cardTop, cardRight, cardTop + cardHeight)

        // 绘制卡片阴影
        val shadowPaint = Paint().apply {
            color = 0x1A000000
            maskFilter = BlurMaskFilter(40f, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.drawRoundRect(
            RectF(
                cardLeft + 8f,
                cardTop + 8f,
                cardRight + 8f,
                cardTop + cardHeight + 8f
            ),
            RADIUS_LARGE,
            RADIUS_LARGE,
            shadowPaint
        )

        // 绘制卡片背景
        val cardBgPaint = Paint().apply {
            color = SURFACE_COLOR
            isAntiAlias = true
        }

        canvas.drawRoundRect(cardRect, RADIUS_LARGE, RADIUS_LARGE, cardBgPaint)

        // 绘制卡片边框
        val borderPaint = Paint().apply {
            color = OUTLINE_COLOR
            style = Paint.Style.STROKE
            strokeWidth = 3f
            isAntiAlias = true
        }

        canvas.drawRoundRect(cardRect, RADIUS_LARGE, RADIUS_LARGE, borderPaint)

        // 绘制内容文本
        var contentY = cardTop + cardPadding + 56f

        val contentPaint = TextPaint().apply {
            color = ON_SURFACE_COLOR
            textSize = 50f  // bodyLarge
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            letterSpacing = 0.02f
            isAntiAlias = true
        }

        val contentWidth = (cardRight - cardLeft - cardPadding * 2).toInt()
        val lines = wrapText(postCard.textContent, contentPaint, contentWidth)
        val maxLines = min(lines.size, contentLines)

        for (i in 0 until maxLines) {
            val line = if (i == maxLines - 1 && lines.size > maxLines) {
                lines[i].take(lines[i].length - 3) + "..."
            } else {
                lines[i]
            }

            canvas.drawText(line, cardLeft + cardPadding, contentY, contentPaint)
            contentY += 72f
        }

        contentY += 20f

        // 绘制时间
        val timePaint = TextPaint().apply {
            color = ON_SURFACE_VARIANT_COLOR
            textSize = 38f  // bodySmall
            isAntiAlias = true
        }

        canvas.drawText(
            postCard.getTimeDisplayText(),
            cardLeft + cardPadding,
            contentY,
            timePaint
        )

        return (cardTop + cardHeight + 40).toInt()
    }

    /**
     * 绘制二维码区域
     */
    private fun drawQRCodeSection(canvas: Canvas, deepLink: String, startY: Int) {
        val qrCodeSize = 440
        val containerPadding = 50f
        val containerSize = qrCodeSize + containerPadding * 2

        val centerX = CARD_WIDTH / 2f
        val containerLeft = centerX - containerSize / 2f
        val containerTop = startY.toFloat()

        // 绘制容器阴影
        val shadowPaint = Paint().apply {
            color = 0x22000000
            maskFilter = BlurMaskFilter(35f, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.drawRoundRect(
            RectF(
                containerLeft + 8f,
                containerTop + 8f,
                containerLeft + containerSize + 8f,
                containerTop + containerSize + 8f
            ),
            RADIUS_LARGE,
            RADIUS_LARGE,
            shadowPaint
        )

        // 绘制容器背景
        val containerPaint = Paint().apply {
            color = SURFACE_COLOR
            isAntiAlias = true
        }

        val containerRect = RectF(
            containerLeft,
            containerTop,
            containerLeft + containerSize,
            containerTop + containerSize
        )

        canvas.drawRoundRect(containerRect, RADIUS_LARGE, RADIUS_LARGE, containerPaint)

        // 绘制渐变边框
        val borderPaint = Paint().apply {
            shader = LinearGradient(
                containerLeft, containerTop,
                containerLeft + containerSize, containerTop + containerSize,
                PRIMARY_COLOR,
                TERTIARY_COLOR,
                Shader.TileMode.CLAMP
            )
            style = Paint.Style.STROKE
            strokeWidth = 5f
            isAntiAlias = true
        }

        canvas.drawRoundRect(containerRect, RADIUS_LARGE, RADIUS_LARGE, borderPaint)

        // 绘制二维码
        val qrCode = QRCodeUtils.generateQRCode(
            content = deepLink,
            size = qrCodeSize,
            foregroundColor = ON_SURFACE_COLOR,
            backgroundColor = SURFACE_COLOR
        )

        if (qrCode != null) {
            val qrLeft = centerX - qrCodeSize / 2f
            val qrTop = containerTop + containerPadding
            canvas.drawBitmap(qrCode, qrLeft, qrTop, null)
        }

        // 绘制底部提示
        val hintY = containerTop + containerSize + 85f

        val hintPaint = TextPaint().apply {
            color = ON_SURFACE_VARIANT_COLOR
            textSize = 42f  // bodyMedium
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        canvas.drawText(
            "扫码查看完整内容",
            centerX,
            hintY,
            hintPaint
        )

        // 绘制Logo标识
        val logoPaint = TextPaint().apply {
            color = PRIMARY_COLOR
            textSize = 38f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText(
            "糖原社区 · TANGYUAN",
            centerX,
            CARD_HEIGHT - PADDING.toFloat(),
            logoPaint
        )
    }

    /**
     * 文本换行工具
     */
    private fun wrapText(text: String, paint: TextPaint, maxWidth: Int): List<String> {
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (char in text) {
            val testLine = currentLine + char
            val width = paint.measureText(testLine)

            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = char.toString()
            } else {
                currentLine = testLine
            }
        }

        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        return lines
    }
}
