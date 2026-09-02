package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TealPrimary

enum class NavDestination(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home, "home"),
    CALENDAR("Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "calendar"),
    POMODORO("Pomodoro", Icons.Filled.Timer, Icons.Outlined.Timer, "pomodoro"),
    FESTIVALS("Festivals", Icons.Filled.Festival, Icons.Outlined.Festival, "festivals"),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person, "profile")
}

@Composable
fun FloatingBottomBar(
    currentDestination: NavDestination,
    onDestinationSelected: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = dimensions.screenPadding, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.12f)
                )
                .clip(RoundedCornerShape(36.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = if (dimensions.screenPadding < 12.dp) 6.dp else 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavDestination.entries.forEach { destination ->
                    val isSelected = destination == currentDestination

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        animationSpec = spring(),
                        label = "pillBgColor"
                    )

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        animationSpec = spring(),
                        label = "contentColor"
                    )

                    Box(
                        modifier = Modifier
                            .testTag("nav_tab_${destination.route}")
                            .clip(CircleShape)
                            .background(pillBgColor)
                            .clickable { onDestinationSelected(destination) }
                            .padding(
                                horizontal = if (isSelected) (if (dimensions.screenPadding < 12.dp) 12.dp else 16.dp) else (if (dimensions.screenPadding < 12.dp) 8.dp else 12.dp),
                                vertical = if (dimensions.screenPadding < 12.dp) 8.dp else 10.dp
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) destination.selectedIcon else destination.unselectedIcon,
                                contentDescription = destination.title,
                                tint = contentColor,
                                modifier = Modifier.size(if (dimensions.screenPadding < 12.dp) 20.dp else 24.dp)
                            )

                            if (isSelected && dimensions.screenPadding >= 12.dp) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = destination.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
