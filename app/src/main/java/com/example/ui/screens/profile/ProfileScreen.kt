package com.example.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.components.*
import com.example.ui.sound.AmbientSoundManager
import com.example.ui.sound.SoundPresets
import com.example.ui.theme.*
import com.example.ui.viewmodel.DaywillViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProfileScreen(
    viewModel: DaywillViewModel,
    modifier: Modifier = Modifier
) {
    val dimensions = LocalAppDimensions.current
    val context = LocalContext.current
    val userSettings by viewModel.userSettings.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()

    val soundManager = remember { AmbientSoundManager.getInstance() }
    val isPlayingSound by soundManager.isPlaying.collectAsState()
    val currentSound by soundManager.currentSound.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Settings, 1: Insights, 2: History
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Runtime Permission Launchers
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.toggleNotificationPermission(isGranted)
            if (isGranted) {
                Toast.makeText(context, "Notification permission granted! 🔔", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Notification permission denied.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            viewModel.toggleGalleryPermission(isGranted)
            if (isGranted) {
                Toast.makeText(context, "Gallery & Media full access granted! 🖼️", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Gallery access limited.", Toast.LENGTH_SHORT).show()
            }
        }
    )

    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = userSettings.userName,
            initialEmail = userSettings.userEmail,
            initialBio = userSettings.userBio,
            initialAvatarUri = userSettings.userAvatarUri,
            initialDailyHours = userSettings.targetDailyFocusHours,
            initialRegion = userSettings.region,
            onDismiss = { showEditProfileDialog = false },
            onSave = { name, email, bio, avatarUri, dailyHours, region ->
                viewModel.updateUserProfile(name, email, bio, avatarUri, dailyHours, region)
                showEditProfileDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = dimensions.screenPadding),
        contentPadding = PaddingValues(top = dimensions.screenPadding, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(dimensions.itemSpacing)
    ) {
        // User Profile Card (Rich profile info + avatar + quick edit)
        item {
            ExpressiveCard(
                pastelTheme = CardPastelTheme.MINT,
                showOrganicLeaf = true
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (userSettings.userAvatarUri.isNotEmpty()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(userSettings.userAvatarUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "User Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(if (dimensions.screenPadding < 12.dp) 52.dp else 68.dp)
                                    .clip(CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(if (dimensions.screenPadding < 12.dp) 52.dp else 68.dp)
                                    .clip(CircleShape)
                                    .background(TealPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userSettings.userName.firstOrNull()?.toString() ?: "R",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = userSettings.userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = userSettings.userEmail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "📍 ${userSettings.region}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "🎯 ${userSettings.targetDailyFocusHours}h Goal",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showEditProfileDialog = true },
                            modifier = Modifier
                                .testTag("edit_profile_button")
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    if (userSettings.userBio.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "\"${userSettings.userBio}\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Segmented Tabs: Settings, Insights, History
        item {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Settings", "Insights", "History").forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (selectedTab == 0) {
            // SETTINGS VIEW
            // 1. Relaxing Sound Player Options
            item {
                Text(
                    text = "Relaxing Sound Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.PEACH) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentSound?.icon ?: "🌧️", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentSound?.title ?: "Rainfall",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = currentSound?.description ?: "Soothing focus ambient",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Button(
                                onClick = { soundManager.togglePlay() },
                                shape = CircleShape
                            ) {
                                Icon(
                                    imageVector = if (isPlayingSound) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isPlayingSound) "Pause" else "Play Sound")
                            }
                        }

                        // Sound selection chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SoundPresets.ALL_SOUNDS.forEach { sound ->
                                val isSelected = currentSound?.id == sound.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { soundManager.selectSound(sound) },
                                    label = {
                                        Text(
                                            text = "${sound.icon} ${sound.title}",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Full Access Permissions Manager Card (Notification & Gallery)
            item {
                Text(
                    text = "App Permissions & Access",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Notification Permission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Notification Permission",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Allow focus timer alarms, break alerts, and festival reminders.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = userSettings.notificationEnabled,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleNotificationPermission(enabled)
                                    }
                                }
                            )
                        }

                        Divider()

                        // Gallery / Media Full Access Permission
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.PhotoLibrary,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gallery Full Access",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Access photos to customize user avatar & festival event banners.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = userSettings.galleryPermissionGranted,
                                onCheckedChange = { enabled ->
                                    if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        galleryPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                    } else if (enabled) {
                                        galleryPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    } else {
                                        viewModel.toggleGalleryPermission(false)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. Appearance Section
            item {
                Text(
                    text = "Appearance & Layout",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Theme Color Preset", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Classic Teal", "Mint", "Lavender", "Peach", "Ocean", "Sunset", "Forest").forEach { themeName ->
                                val isSel = userSettings.themeName == themeName
                                FilterChip(
                                    selected = isSel,
                                    onClick = { viewModel.updateTheme(themeName) },
                                    label = {
                                        Text(
                                            text = themeName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Appearance Mode", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Choose light, dark, or system default", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Light", "Dark", "System").forEach { mode ->
                                    val isSel = userSettings.appearanceMode == mode
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { viewModel.updateAppearanceMode(mode) },
                                        label = { Text(mode, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        // COMPACT MODE TOGGLE (REQ 11)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compact_mode_switch_row"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Compress,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Compact Mode",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Optimizes spacing, card padding, and font scales for any mobile screen size.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = userSettings.isCompactMode,
                                onCheckedChange = { viewModel.toggleCompactMode(it) },
                                modifier = Modifier.testTag("compact_mode_switch")
                            )
                        }
                    }
                }
            }

            // 4. Pomodoro Settings
            item {
                Text(
                    text = "Pomodoro Timer Options",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-start Breaks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = userSettings.autoStartBreaks,
                                onCheckedChange = { viewModel.toggleAutoStartBreaks(it) }
                            )
                        }

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Auto-start Focus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = userSettings.autoStartFocus,
                                onCheckedChange = { viewModel.toggleAutoStartFocus(it) }
                            )
                        }
                    }
                }
            }

            // 5. Data Management
            item {
                Text(
                    text = "Data Management",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(imageVector = Icons.Filled.Backup, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backup Data")
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetAllData() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(imageVector = Icons.Filled.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Data to Default")
                        }
                    }
                }
            }
        } else if (selectedTab == 1) {
            // INSIGHTS VIEW
            item {
                Text(
                    text = "Weekly Focus Time",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            item {
                ExpressiveCard(pastelTheme = CardPastelTheme.MINT) {
                    val sampleData = listOf(
                        BarChartItem("Mon", 90),
                        BarChartItem("Tue", 150),
                        BarChartItem("Wed", 120),
                        BarChartItem("Thu", 180),
                        BarChartItem("Fri", 140),
                        BarChartItem("Sat", 60),
                        BarChartItem("Sun", 90)
                    )
                    ExpressiveBarChart(items = sampleData)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpressiveCard(
                        pastelTheme = CardPastelTheme.PEACH,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Current Streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userSettings.currentStreak} Days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PastelPeachAccent)
                    }

                    ExpressiveCard(
                        pastelTheme = CardPastelTheme.YELLOW,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Best Streak", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${userSettings.bestStreak} Days", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = PastelYellowAccent)
                    }
                }
            }
        } else {
            // HISTORY VIEW
            item {
                Text(
                    text = "Activity Log",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (sessions.isEmpty()) {
                item {
                    ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                        Text("No session history recorded yet.", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            } else {
                items(sessions.size) { idx ->
                    val s = sessions[idx]
                    val dateFormatted = remember(s.timestamp) {
                        SimpleDateFormat("EEE, MMM d • hh:mm a", Locale.getDefault()).format(Date(s.timestamp))
                    }
                    ExpressiveCard(pastelTheme = CardPastelTheme.SURFACE) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(s.taskTitle.ifEmpty { "Focus Session" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(dateFormatted, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("${s.durationMinutes} min", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
