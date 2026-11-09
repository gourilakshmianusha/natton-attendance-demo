package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.data.model.calculateStayDuration
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.PunchType
import com.example.ui.MainViewModel
import com.example.ui.components.LiveClockWidget
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent

@Composable
fun PunchClockScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentEmp by viewModel.currentEmployee.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val isPunching by viewModel.isPunching.collectAsState()
    val punchLocation by viewModel.punchLocation.collectAsState()
    val punchNotes by viewModel.punchNotes.collectAsState()

    val myRecord = todayRecords.find { it.employeeId == currentEmp?.id }
    val isCheckedIn = myRecord?.inTime != null
    val isCheckedOut = myRecord?.outTime != null

    var showEmployeePicker by remember { mutableStateOf(false) }

    var currentTimeString by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeString = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
            delay(1000)
        }
    }

    val liveStayResult = if (isCheckedIn) {
        calculateStayDuration(
            inTimeStr = myRecord?.inTime,
            outTimeStr = myRecord?.outTime ?: currentTimeString,
            shiftStart = currentEmp?.shiftStart ?: "09:30",
            breakMinutes = myRecord?.breakMinutes ?: 30
        )
    } else null

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCheckedIn && !isCheckedOut) 1.05f else 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Employee Switcher Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("employee_profile_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(currentEmp?.avatarColorHex ?: 0xFF2563EB)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentEmp?.name?.split(" ")?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("") ?: "EMP",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentEmp?.name ?: "Select Employee",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${currentEmp?.role ?: ""} • ${currentEmp?.department ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { showEmployeePicker = true },
                            modifier = Modifier.testTag("switch_employee_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Switch", fontSize = 12.sp)
                        }

                        DropdownMenu(
                            expanded = showEmployeePicker,
                            onDismissRequest = { showEmployeePicker = false }
                        ) {
                            employees.forEach { emp ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(emp.name, fontWeight = FontWeight.Medium)
                                            Text("${emp.id} • ${emp.department}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                        }
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(emp.avatarColorHex)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = emp.name.firstOrNull()?.toString() ?: "E",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    },
                                    onClick = {
                                        viewModel.selectEmployee(emp.id)
                                        showEmployeePicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Automated Sign-In & Office Stay Tracker Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_punch_stay_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCheckedIn && !isCheckedOut) Color(0xFF1C2541) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = if (isCheckedIn && !isCheckedOut) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF64DFDF).copy(alpha = 0.5f)) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isCheckedIn && !isCheckedOut) Icons.Default.Schedule else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isCheckedIn && !isCheckedOut) Color(0xFF64DFDF) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (!isCheckedIn) "Auto-Punch Ready on Sign-In"
                                else if (!isCheckedOut) "Live In-Office Stay Session"
                                else "Office Stay Summary",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isCheckedIn && !isCheckedOut) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (isCheckedIn && !isCheckedOut) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TRACKING STAY",
                                    color = Color(0xFF10B981),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    if (!isCheckedIn) {
                        Text(
                            text = "Standard Shift: 09:30 AM - 06:00 PM (8.5 hrs). Tapping PUNCH IN will automatically log your arrival time and begin real-time office stay calculation.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else if (liveStayResult != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0B132B).copy(alpha = 0.8f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Arrival (IN)", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(myRecord?.inTime ?: "--:--", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text(if (!isCheckedOut) "Stay Elapsed" else "Total Stay", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text(liveStayResult.grossFormatted, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64DFDF))
                            }
                            Column {
                                Text("Break Deduct", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text("30 mins", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column {
                                Text("Effective Work", fontSize = 10.sp, color = Color(0xFF90E0EF))
                                Text("${liveStayResult.netHours} hrs", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        val progress = (liveStayResult.grossHours / 8.5).toFloat().coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (progress >= 1f) Color(0xFF10B981) else Color(0xFF0077B6),
                            trackColor = Color(0xFF3A506B)
                        )

                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${liveStayResult.complianceText} (Target: 8.5 Gross Hrs)",
                                fontSize = 10.sp,
                                color = if (liveStayResult.isLate) Color(0xFFF59E0B) else Color(0xFF90E0EF)
                            )
                            if (liveStayResult.overtimeHours > 0) {
                                Text(
                                    text = "Overtime: +${liveStayResult.overtimeHours} hrs",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Clock Widget
        item {
            LiveClockWidget(
                isCheckedIn = isCheckedIn,
                isCheckedOut = isCheckedOut,
                inTime = myRecord?.inTime,
                outTime = myRecord?.outTime,
                shiftName = currentEmp?.shiftName ?: "Day Shift (09:00 - 18:00)"
            )
        }

        // Main Punch In / Out Hero Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (!isCheckedIn) "Ready to Start Your Shift?"
                        else if (!isCheckedOut) "You are Clocked In"
                        else "Shift Completed for Today",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (!isCheckedIn) "Punch in to register your attendance and start working"
                        else if (!isCheckedOut) "Punch out when ending your working day or taking lunch"
                        else "Recorded ${myRecord?.workHours ?: 0.0} total work hours today",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Big Circular Punch Button
                    Box(
                        modifier = Modifier
                            .size(170.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = if (!isCheckedIn) {
                                        listOf(StatusPresent, Color(0xFF059669))
                                    } else if (!isCheckedOut) {
                                        listOf(Color(0xFFEF4444), Color(0xFFDC2626))
                                    } else {
                                        listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                                    }
                                )
                            )
                            .border(
                                width = 4.dp,
                                color = Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable(enabled = !isPunching && (!isCheckedOut || isCheckedIn)) {
                                if (!isCheckedIn) {
                                    val empId = currentEmp?.id ?: "EMP-1001"
                                    viewModel.employeeLoginAndAutoPunch(empId)
                                } else if (!isCheckedOut) {
                                    viewModel.employeeCheckoutAndAutoPunchOut()
                                }
                            }
                            .testTag("main_punch_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (!isCheckedIn) Icons.Default.Login
                                else if (!isCheckedOut) Icons.Default.Logout
                                else Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (!isCheckedIn) "PUNCH IN"
                                else if (!isCheckedOut) "PUNCH OUT"
                                else "COMPLETED",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp
                                ),
                                color = Color.White
                            )
                            if (isCheckedIn && !isCheckedOut) {
                                Text(
                                    text = "Tap to finish",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Secondary Quick Actions (Break Start / End)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.punch(PunchType.BREAK_START) },
                            enabled = isCheckedIn && !isCheckedOut && !isPunching,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_start_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Coffee, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Break", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.punch(PunchType.BREAK_END) },
                            enabled = isCheckedIn && !isCheckedOut && !isPunching,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("break_end_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("End Break", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Location & Notes Customization
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Punch Options & Work Mode",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Location Selector Pills
                    val locations = listOf(
                        "Office (HQ - Fl 4)" to Icons.Default.Business,
                        "Remote (WFH)" to Icons.Default.Home,
                        "Client Site" to Icons.Default.LocationOn
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        locations.forEach { (loc, icon) ->
                            val selected = punchLocation == loc
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.punchLocation.value = loc }
                                    .padding(vertical = 8.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = loc.substringBefore(" ("),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = punchNotes,
                        onValueChange = { viewModel.punchNotes.value = it },
                        placeholder = { Text("Add optional note (e.g. Traffic delay, Client demo)", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("punch_notes_field"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }

        // Today's Company Attendance Snapshot
        item {
            val presentCount = todayRecords.count { it.status == AttendanceStatus.PRESENT }
            val lateCount = todayRecords.count { it.status == AttendanceStatus.LATE }
            val leaveCount = todayRecords.count { it.status == AttendanceStatus.ON_LEAVE }
            val totalEmployees = employees.size

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Company Today Snapshot",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatTile(label = "Present", value = "$presentCount", color = StatusPresent)
                        StatTile(label = "Late", value = "$lateCount", color = StatusLate)
                        StatTile(label = "On Leave", value = "$leaveCount", color = Color(0xFF8B5CF6))
                        StatTile(label = "Total Staff", value = "$totalEmployees", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
