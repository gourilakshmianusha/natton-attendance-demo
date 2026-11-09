package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.AttendanceLogScreen
import com.example.ui.screens.EmployeeDirectoryScreen
import com.example.ui.screens.EmployeeReportScreen
import com.example.ui.screens.HolidaysLeavesScreen
import com.example.ui.screens.PunchClockScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusPresent

enum class AppDestination(val label: String, val icon: ImageVector, val tag: String) {
    ADMIN("Admin Desk", Icons.Default.AdminPanelSettings, "nav_admin"),
    SUPER_ADMIN("Super Admin", Icons.Default.Security, "nav_super_admin"),
    PUNCH("Clock", Icons.Default.Fingerprint, "nav_punch"),
    REPORTS("Reports", Icons.Default.Assessment, "nav_reports"),
    LOGS("Logs", Icons.Default.History, "nav_logs"),
    EMPLOYEES("Staff", Icons.Default.Badge, "nav_employees"),
    HOLIDAYS("Calendar", Icons.Default.CalendarMonth, "nav_holidays")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.ADMIN) }
    var forceWebDesktopMode by remember { mutableStateOf(false) }
    var showDeploymentInfoDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val isAdminLoggedIn by viewModel.isAdminLoggedIn.collectAsState()

    // Handle back button on secondary screens
    BackHandler(enabled = currentDestination != AppDestination.ADMIN) {
        currentDestination = AppDestination.ADMIN
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    if (showDeploymentInfoDialog) {
        AlertDialog(
            onDismissRequest = { showDeploymentInfoDialog = false },
            icon = {
                Icon(Icons.Default.Devices, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Natton Technology • Web & App Access", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "This enterprise system is built to operate both as a full Web Application Portal and as a native Mobile App:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("1. Web Application Portal", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                text = "Directly accessible in browser via the Shared App URL. Features a full sidebar navigation rail, multi-column dashboard, office stay duration calculator, master attendance sheet, and CSV export.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = StatusPresent, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("2. Direct Mobile App (APK)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Text(
                                text = "To install as a native mobile app on Android phones or tablets, click the top Settings menu in Google AI Studio -> 'Download APK' to download and install the package directly.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "Tip: Tap 'Web View' or 'Mobile View' in the top bar to toggle between the desktop web portal and handheld app layout at any time.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDeploymentInfoDialog = false },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Got it")
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp
        val isWebLayout = isWideScreen || forceWebDesktopMode

        if (isWebLayout) {
            // Tablet / Desktop Adaptive Two-Pane Web Portal Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Side Navigation Rail (Web Sidebar style)
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = if (isWideScreen) 96.dp else 72.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 14.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.natton_logo),
                                contentDescription = "Natton Technology",
                                modifier = Modifier
                                    .size(if (isWideScreen) 46.dp else 36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Natton Tech",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandPrimary,
                                    fontSize = if (isWideScreen) 11.sp else 9.sp
                                )
                            )
                            Text(
                                text = "Web Portal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                ) {
                    AppDestination.values().forEach { destination ->
                        NavigationRailItem(
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = {
                                Text(
                                    destination.label,
                                    fontSize = 9.sp,
                                    fontWeight = if (currentDestination == destination) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag(destination.tag),
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = BrandPrimary,
                                indicatorColor = BrandPrimary
                            )
                        )
                    }
                }

                // Desktop / Tablet Content Pane
                Scaffold(
                    modifier = Modifier.weight(1f),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.natton_logo),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Natton Technology Web Portal",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(StatusPresent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "09:30 AM - 06:00 PM",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = StatusPresent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                }
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { forceWebDesktopMode = false },
                                        shape = RoundedCornerShape(20.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Mobile View", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { showDeploymentInfoDialog = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Info, contentDescription = "Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background
                            )
                        )
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        Crossfade(targetState = currentDestination, label = "desktop_content") { dest ->
                            when (dest) {
                                AppDestination.ADMIN -> {
                                    if (isAdminLoggedIn) {
                                        AdminPortalScreen(viewModel = viewModel)
                                    } else {
                                        AdminLoginScreen(
                                            viewModel = viewModel,
                                            onLoginSuccess = { /* Automatically navigates due to state change */ }
                                        )
                                    }
                                }
                                AppDestination.SUPER_ADMIN -> SuperAdminScreen(viewModel = viewModel)
                                AppDestination.PUNCH -> PunchClockScreen(viewModel = viewModel)
                                AppDestination.LOGS -> AttendanceLogScreen(viewModel = viewModel)
                                AppDestination.REPORTS -> EmployeeReportScreen(viewModel = viewModel)
                                AppDestination.EMPLOYEES -> EmployeeDirectoryScreen(
                                    viewModel = viewModel,
                                    onNavigateToReport = { currentDestination = AppDestination.REPORTS }
                                )
                                AppDestination.HOLIDAYS -> HolidaysLeavesScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        } else {
            // Mobile Phone Web-Styled Layout
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(
                                    painter = painterResource(id = R.drawable.natton_logo),
                                    contentDescription = "Natton Technology",
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Natton Technology",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable { forceWebDesktopMode = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Web View", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { showDeploymentInfoDialog = true },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = "App Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        AppDestination.values().forEach { destination ->
                            NavigationBarItem(
                                selected = currentDestination == destination,
                                onClick = { currentDestination = destination },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = destination.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = destination.label,
                                        fontSize = 10.sp,
                                        fontWeight = if (currentDestination == destination) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag(destination.tag),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = BrandPrimary,
                                    indicatorColor = BrandPrimary
                                )
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Crossfade(targetState = currentDestination, label = "mobile_content") { dest ->
                        when (dest) {
                            AppDestination.ADMIN -> {
                                if (isAdminLoggedIn) {
                                    AdminPortalScreen(viewModel = viewModel)
                                } else {
                                    AdminLoginScreen(
                                        viewModel = viewModel,
                                        onLoginSuccess = { /* Automatically navigates due to state change */ }
                                    )
                                }
                            }
                            AppDestination.SUPER_ADMIN -> SuperAdminScreen(viewModel = viewModel)
                            AppDestination.PUNCH -> PunchClockScreen(viewModel = viewModel)
                            AppDestination.LOGS -> AttendanceLogScreen(viewModel = viewModel)
                            AppDestination.REPORTS -> EmployeeReportScreen(viewModel = viewModel)
                            AppDestination.EMPLOYEES -> EmployeeDirectoryScreen(
                                viewModel = viewModel,
                                onNavigateToReport = { currentDestination = AppDestination.REPORTS }
                            )
                            AppDestination.HOLIDAYS -> HolidaysLeavesScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
