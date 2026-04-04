package com.qingshuige.tangyuan.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.qingshuige.tangyuan.navigation.Screen
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

private data class RailNavItem(
    val screen: Screen,
    val icon: ImageVector,
)

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun TangyuanNavigationRail(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        RailNavItem(Screen.Talk, Icons.Filled.ChatBubble),
        RailNavItem(Screen.Topic, Icons.Filled.ListAlt),
        RailNavItem(Screen.Message, Icons.Filled.Notifications),
        RailNavItem(Screen.User, Icons.Filled.Person)
    )

    val endBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    NavigationRail(
        modifier = modifier
            .fillMaxHeight()
            .hazeChild(
                state = hazeState,
                style = HazeMaterials.thick(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                )
            )
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = endBorderColor,
                    start = Offset(size.width, 0f),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            },
        containerColor = Color.Transparent
    ) {
        Spacer(Modifier.weight(1f))
        items.forEach { item ->
            val isSelected = currentScreen == item.screen
            NavigationRailItem(
                selected = isSelected,
                onClick = { onScreenSelected(item.screen) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.screen.title
                    )
                },
                label = {
                    Text(
                        item.screen.title,
                        fontSize = 10.sp,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            )
        }
        Spacer(Modifier.weight(1f))
    }
}
