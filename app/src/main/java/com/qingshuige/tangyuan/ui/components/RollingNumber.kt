package com.qingshuige.tangyuan.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

/**
 * 滚动数字效果组件
 */
@Composable
fun RollingNumber(
    number: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold
    )
) {
    var targetNumber by remember { mutableIntStateOf(number) }

    SideEffect {
        targetNumber = number
    }

    val numberString = targetNumber.toString()

    Row(modifier = modifier) {
        numberString.forEach { char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                    (slideInVertically(
                        animationSpec = tween(durationMillis = 300),
                        initialOffsetY = { it }
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = 300)
                    )).togetherWith(
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 300),
                            targetOffsetY = { -it }
                        ) + fadeOut(
                            animationSpec = tween(durationMillis = 300)
                        )
                    ).using(
                        SizeTransform(clip = false)
                    )
                },
                label = "rolling_number_animation"
            ) { targetChar ->
                Text(
                    text = targetChar.toString(),
                    style = style
                )
            }
        }
    }
}
