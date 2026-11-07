package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Terminal
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.AdminPortalScreen
import com.example.ui.screens.ApiConsoleScreen
import com.example.ui.screens.AttendanceLogScreen
import com.example.ui.screens.EmployeeDirectoryScreen
import com.example.ui.screens.EmployeeReportScreen
import com.example.ui.screens.HolidaysLeavesScreen
import com.example.ui.screens.PunchClockScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusPresent

enum class AppDestination(val label: String, val icon: ImageVector, val tag: String) {
    ADMIN("Admin Desk", Icons.Default.AdminPanelSettings, "nav_admin"),
    PUNCH("Clock", Icons.Default.Fingerprint, "nav_punch"),
    REPORTS("Reports", Icons.Default.Assessment, "nav_reports"),
    LOGS("Logs", Icons.Default.History, "nav_logs"),
    EMPLOYEES("Staff", Icons.Default.Badge, "nav_employees"),
    HOLIDAYS("Calendar", Icons.Default.CalendarMonth, "nav_holidays"),
    API_CONSOLE("API", Icons.Default.Terminal, "nav_api")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    var currentDestination by remember { mutableStateOf(AppDestination.ADMIN) }
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val currentEmp by viewModel.currentEmployee.collectAsState()

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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isWideScreen) {
            // Tablet / Desktop Adaptive Two-Pane Web Portal Layout
            Row(modifier = Modifier.fillMaxSize()) {
                // Side Navigation Rail (Web Sidebar style)
                NavigationRail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(min = 90.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    header = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(BrandPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "AttendFlow",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "AttendFlow",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandPrimary,
                                    fontSize = 12.sp
                                )
                            )
                            Text(
                                text = "Web Portal",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
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
                            label = { Text(destination.label, fontSize = 10.sp, fontWeight = if (currentDestination == destination) FontWeight.Bold else FontWeight.Normal) },
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
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AttendFlow Enterprise Web Dashboard",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(StatusPresent.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = "Live Tracking Active",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = StatusPresent,
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            },
                            actions = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(end = 16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(text = "Admin Manager", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = "HQ Operations", fontSize = 10.sp, color = Color.Gray)
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
                                AppDestination.ADMIN -> AdminPortalScreen(viewModel = viewModel)
                                AppDestination.PUNCH -> PunchClockScreen(viewModel = viewModel)
                                AppDestination.LOGS -> AttendanceLogScreen(viewModel = viewModel)
                                AppDestination.REPORTS -> EmployeeReportScreen(viewModel = viewModel)
                                AppDestination.EMPLOYEES -> EmployeeDirectoryScreen(
                                    viewModel = viewModel,
                                    onNavigateToReport = { currentDestination = AppDestination.REPORTS }
                                )
                                AppDestination.HOLIDAYS -> HolidaysLeavesScreen(viewModel = viewModel)
                                AppDestination.API_CONSOLE -> ApiConsoleScreen(viewModel = viewModel)
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
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BrandPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AttendFlow Web",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        },
                        actions = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
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
                            AppDestination.ADMIN -> AdminPortalScreen(viewModel = viewModel)
                            AppDestination.PUNCH -> PunchClockScreen(viewModel = viewModel)
                            AppDestination.LOGS -> AttendanceLogScreen(viewModel = viewModel)
                            AppDestination.REPORTS -> EmployeeReportScreen(viewModel = viewModel)
                            AppDestination.EMPLOYEES -> EmployeeDirectoryScreen(
                                viewModel = viewModel,
                                onNavigateToReport = { currentDestination = AppDestination.REPORTS }
                            )
                            AppDestination.HOLIDAYS -> HolidaysLeavesScreen(viewModel = viewModel)
                            AppDestination.API_CONSOLE -> ApiConsoleScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}
