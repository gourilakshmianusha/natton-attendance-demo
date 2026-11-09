package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WorkHistory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.AttendanceStatus
import com.example.data.model.EventCategory
import com.example.data.model.EventSeverity
import com.example.data.model.SystemEvent
import com.example.data.model.calculateStayDuration
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuperAdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val isSuperAdminLoggedIn by viewModel.isSuperAdminLoggedIn.collectAsStateWithLifecycle()
    val employees by viewModel.employees.collectAsStateWithLifecycle()
    val todayRecords by viewModel.todayRecords.collectAsStateWithLifecycle()
    val systemEvents by viewModel.systemEvents.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }

    if (!isSuperAdminLoggedIn) {
        SuperAdminLoginCard(
            onLogin = { email, pass -> viewModel.superAdminLogin(email, pass) }
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF0B132B))
        ) {
            // Top Master Header
            SuperAdminHeader(
                onLogout = { viewModel.superAdminLogout() },
                totalEvents = systemEvents.size,
                activeEmployeesCount = todayRecords.count { it.inTime != null && it.outTime == null }
            )

            // Primary Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color(0xFF1C2541),
                contentColor = Color(0xFF64DFDF)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Live Office Stay", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("System Events (${systemEvents.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Generate Events", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> LiveOfficeStayMonitor(
                    employees = employees,
                    todayRecords = todayRecords,
                    onForcePunchOut = { empId ->
                        viewModel.employeeCheckoutAndAutoPunchOut()
                    }
                )
                1 -> SystemEventsFeed(
                    events = systemEvents,
                    onDeleteEvent = { viewModel.deleteSystemEvent(it) },
                    onClearAll = { viewModel.clearAllSystemEvents() }
                )
                2 -> EventGeneratorPane(
                    onBroadcastEvent = { title, desc, cat, sev, dept ->
                        viewModel.generateSuperAdminEvent(
                            title = title,
                            description = desc,
                            category = cat,
                            severity = sev,
                            targetDepartment = dept,
                            author = "Super Admin"
                        )
                        selectedTab = 1 // Switch to feed to see broadcast
                    }
                )
            }
        }
    }
}

@Composable
fun SuperAdminLoginCard(
    onLogin: (String, String) -> Boolean
) {
    var email by remember { mutableStateOf("superadmin@natton.com") }
    var password by remember { mutableStateOf("super123") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B132B))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF48CAE4).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Super Admin Shield",
                        tint = Color(0xFF48CAE4),
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "NATTON TECHNOLOGY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = Color(0xFF64DFDF)
                )

                Text(
                    text = "Super Admin Portal",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Centralized Command Tower • Live Stay Monitoring • Company Event Broadcaster",
                    fontSize = 12.sp,
                    color = Color(0xFF90E0EF),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = null },
                    label = { Text("Super Admin ID / Email", color = Color(0xFFB0C4DE)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF64DFDF))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF64DFDF),
                        unfocusedBorderColor = Color(0xFF3A506B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("super_admin_email_input")
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; errorMessage = null },
                    label = { Text("Master Security Key", color = Color(0xFFB0C4DE)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF64DFDF))
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF64DFDF),
                        unfocusedBorderColor = Color(0xFF3A506B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("super_admin_password_input")
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF6B6B),
                        fontSize = 12.sp
                    )
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = {
                        val success = onLogin(email, password)
                        if (!success) {
                            errorMessage = "Access Denied: Invalid credentials."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("super_admin_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Unlock Command Tower",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        email = "superadmin@natton.com"
                        password = "super123"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("super_admin_demo_credentials_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64DFDF)),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF3A506B))
                    )
                ) {
                    Text("Auto-Fill Demo Credentials (superadmin@natton.com)")
                }
            }
        }
    }
}

@Composable
fun SuperAdminHeader(
    onLogout: () -> Unit,
    totalEvents: Int,
    activeEmployeesCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0077B6)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "NATTON SUPER ADMIN",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64DFDF)
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("LIVE", color = Color(0xFF10B981), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Active in Office: $activeEmployeesCount  •  Events: $totalEvents",
                        fontSize = 11.sp,
                        color = Color(0xFF90E0EF)
                    )
                }
            }

            OutlinedButton(
                onClick = onLogout,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFF6B6B).copy(alpha = 0.4f))
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("super_admin_logout_button")
            ) {
                Text("Sign Out", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun LiveOfficeStayMonitor(
    employees: List<com.example.data.model.Employee>,
    todayRecords: List<com.example.data.model.AttendanceRecord>,
    onForcePunchOut: (String) -> Unit
) {
    // Current time live ticker
    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            delay(1000)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            // Live status banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Real-Time Office Stay Engine",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = currentTimeString,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64DFDF)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Official Shift: 09:30 AM - 06:00 PM (8.5 Gross Hrs) • Late Threshold: 09:40 AM",
                        fontSize = 11.sp,
                        color = Color(0xFFB0C4DE)
                    )
                }
            }
        }

        // Active Employee Cards
        items(employees) { emp ->
            val record = todayRecords.find { it.employeeId == emp.id }
            val inTime = record?.inTime
            val outTime = record?.outTime
            val isPresentNow = inTime != null && outTime == null
            val isCompleted = inTime != null && outTime != null

            val stayResult = if (inTime != null) {
                calculateStayDuration(
                    inTimeStr = inTime,
                    outTimeStr = outTime ?: currentTimeString,
                    shiftStart = emp.shiftStart,
                    breakMinutes = record?.breakMinutes ?: 30
                )
            } else null

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPresentNow) Color(0xFF1C2541) else Color(0xFF161E36)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = if (isPresentNow) Color(0xFF64DFDF).copy(alpha = 0.5f) else Color(0xFF2A3A5B),
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(emp.avatarColorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emp.name.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = emp.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "${emp.id} • ${emp.department}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF90E0EF)
                                )
                            }
                        }

                        // Status Tag
                        val tagColor = when {
                            isPresentNow -> Color(0xFF10B981)
                            isCompleted -> Color(0xFF0077B6)
                            else -> Color(0xFF9E9E9E)
                        }
                        val tagLabel = when {
                            isPresentNow -> "ACTIVE IN OFFICE"
                            isCompleted -> "CHECKED OUT"
                            else -> "NOT ARRIVED"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(tagColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tagLabel,
                                color = tagColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (inTime != null && stayResult != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Arrival (IN)", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(inTime, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (isPresentNow) "Live Stay" else "Final Stay", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(
                                    text = stayResult.grossFormatted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPresentNow) Color(0xFF64DFDF) else Color.White
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Departure (OUT)", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(outTime ?: "--:--", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Net Work", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(
                                    text = "${stayResult.netHours} hrs",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }

                        if (stayResult.isLate) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "⚠️ Arrived late by ${stayResult.lateMinutes} mins (after 09:40 cutoff)",
                                fontSize = 10.sp,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    } else {
                        Text(
                            text = "Awaiting employee sign-in / punch-in.",
                            fontSize = 11.sp,
                            color = Color(0xFF6C757D)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SystemEventsFeed(
    events: List<SystemEvent>,
    onDeleteEvent: (String) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf<EventCategory?>(null) }
    var selectedSeverityFilter by remember { mutableStateOf<EventSeverity?>(null) }

    val filteredEvents = events.filter { ev ->
        (selectedCategoryFilter == null || ev.category == selectedCategoryFilter) &&
        (selectedSeverityFilter == null || ev.severity == selectedSeverityFilter)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        item {
            // Action & Filter Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Audit & Event Feed (${filteredEvents.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (events.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF6B6B)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear All", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            // Category Filter Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedCategoryFilter == null,
                    onClick = { selectedCategoryFilter = null },
                    label = { Text("All Categories", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0077B6),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1C2541),
                        labelColor = Color(0xFFB0C4DE)
                    )
                )

                EventCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = {
                            selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat
                        },
                        label = { Text(cat.label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0077B6),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1C2541),
                            labelColor = Color(0xFFB0C4DE)
                        )
                    )
                }
            }
        }

        if (filteredEvents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541))
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No events match the selected filters. Use 'Generate Events' tab to create broadcasts.",
                            fontSize = 12.sp,
                            color = Color(0xFF90E0EF)
                        )
                    }
                }
            }
        } else {
            items(filteredEvents, key = { it.id }) { event ->
                SystemEventCard(
                    event = event,
                    onDelete = { onDeleteEvent(event.id) }
                )
            }
        }
    }
}

@Composable
fun SystemEventCard(
    event: SystemEvent,
    onDelete: () -> Unit
) {
    val sevColor = Color(event.severity.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(sevColor.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.severity.label.uppercase(),
                            color = sevColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = event.category.label,
                        color = Color(0xFF64DFDF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.timestamp.substringAfter(" "),
                        color = Color(0xFF8D99AE),
                        fontSize = 11.sp
                    )
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Event",
                            tint = Color(0xFF8D99AE),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = event.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = event.description,
                fontSize = 12.sp,
                color = Color(0xFFD0D7DE),
                lineHeight = 17.sp
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Target: ${event.targetDepartment}",
                    fontSize = 10.sp,
                    color = Color(0xFF90E0EF)
                )
                Text(
                    text = "By: ${event.author}",
                    fontSize = 10.sp,
                    color = Color(0xFF8D99AE)
                )
            }
        }
    }
}

@Composable
fun EventGeneratorPane(
    onBroadcastEvent: (String, String, EventCategory, EventSeverity, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(EventCategory.COMPANY_ANNOUNCEMENT) }
    var selectedSeverity by remember { mutableStateOf(EventSeverity.INFO) }
    var targetDepartment by remember { mutableStateOf("All Departments") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            // Quick 1-Tap Presets
            Text(
                text = "⚡ Instant 1-Tap Event Presets",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64DFDF)
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickPresetItem(
                    title = "📢 Early Office Dismissal (04:30 PM)",
                    subtitle = "Notify all staff of early departure today with full stay credited.",
                    category = EventCategory.COMPANY_ANNOUNCEMENT,
                    severity = EventSeverity.SUCCESS,
                    onClick = {
                        onBroadcastEvent(
                            "Company Early Dismissal at 04:30 PM",
                            "All employees are permitted to check out early at 04:30 PM today. Full 8.5-hour shift credit will be applied.",
                            EventCategory.COMPANY_ANNOUNCEMENT,
                            EventSeverity.SUCCESS,
                            "All Departments"
                        )
                    }
                )

                QuickPresetItem(
                    title = "⚡ Sunday Weekly Holiday Reminder",
                    subtitle = "Automated reminder: office closed every Sunday per company policy.",
                    category = EventCategory.HOLIDAY_NOTICE,
                    severity = EventSeverity.INFO,
                    onClick = {
                        onBroadcastEvent(
                            "Sunday Paid Weekly Holiday Notification",
                            "Reminder: Sundays are official paid weekly holidays at Natton Technology. Shifts resume Monday at 09:30 AM.",
                            EventCategory.HOLIDAY_NOTICE,
                            EventSeverity.INFO,
                            "All Departments"
                        )
                    }
                )

                QuickPresetItem(
                    title = "⚠️ Shift Compliance Audit (09:30 AM - 06:00 PM)",
                    subtitle = "Broadcast attendance compliance alert regarding 09:40 AM late cutoff.",
                    category = EventCategory.ADMIN_AUDIT,
                    severity = EventSeverity.WARNING,
                    onClick = {
                        onBroadcastEvent(
                            "Attendance Adherence & Punctuality Audit",
                            "Super Admin audit: Check-ins after 09:40 AM are automatically flagged as Late Arrival. Please ensure timely punch-in.",
                            EventCategory.ADMIN_AUDIT,
                            EventSeverity.WARNING,
                            "All Departments"
                        )
                    }
                )

                QuickPresetItem(
                    title = "🌐 Quarterly Town Hall Announcement",
                    subtitle = "All-hands company meeting in main conference hall.",
                    category = EventCategory.COMPANY_ANNOUNCEMENT,
                    severity = EventSeverity.INFO,
                    onClick = {
                        onBroadcastEvent(
                            "Natton Quarterly All-Hands Town Hall",
                            "Join executive leadership today at 04:00 PM in Conference Room A or virtual stream for company milestones & product roadmaps.",
                            EventCategory.COMPANY_ANNOUNCEMENT,
                            EventSeverity.INFO,
                            "All Departments"
                        )
                    }
                )

                QuickPresetItem(
                    title = "🚨 Emergency IT & Network Maintenance",
                    subtitle = "Scheduled router and database maintenance notice.",
                    category = EventCategory.SYSTEM_ALERT,
                    severity = EventSeverity.CRITICAL,
                    onClick = {
                        onBroadcastEvent(
                            "Critical Network Maintenance Window",
                            "IT Infrastructure will undergo scheduled maintenance from 06:15 PM to 07:00 PM. Local time-clocks will buffer punches automatically.",
                            EventCategory.SYSTEM_ALERT,
                            EventSeverity.CRITICAL,
                            "All Departments"
                        )
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "📝 Custom Event Broadcaster",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Event Title", color = Color(0xFFB0C4DE)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF64DFDF),
                            unfocusedBorderColor = Color(0xFF3A506B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("super_admin_event_title_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Event Details & Message", color = Color(0xFFB0C4DE)) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF64DFDF),
                            unfocusedBorderColor = Color(0xFF3A506B)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("super_admin_event_desc_input")
                    )

                    Spacer(Modifier.height(10.dp))

                    Text("Event Severity", fontSize = 11.sp, color = Color(0xFF90E0EF))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        EventSeverity.values().forEach { sev ->
                            val isSelected = selectedSeverity == sev
                            val color = Color(sev.colorHex)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) color else Color(0xFF0F172A))
                                    .clickable { selectedSeverity = sev }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sev.label,
                                    color = if (isSelected) Color.White else Color(0xFF8D99AE),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = targetDepartment,
                        onValueChange = { targetDepartment = it },
                        label = { Text("Target Department", color = Color(0xFFB0C4DE)) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF64DFDF),
                            unfocusedBorderColor = Color(0xFF3A506B)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onBroadcastEvent(
                                    title.trim(),
                                    description.trim(),
                                    selectedCategory,
                                    selectedSeverity,
                                    targetDepartment.trim()
                                )
                                title = ""
                                description = ""
                            }
                        },
                        enabled = title.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077B6)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("broadcast_event_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Broadcast System Event", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickPresetItem(
    title: String,
    subtitle: String,
    category: EventCategory,
    severity: EventSeverity,
    onClick: () -> Unit
) {
    val sevColor = Color(severity.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C2541)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF90E0EF),
                    lineHeight = 15.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(sevColor.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BROADCAST",
                    color = sevColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
