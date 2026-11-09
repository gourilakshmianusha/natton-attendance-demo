package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Holiday
import com.example.data.model.LeaveRequest
import com.example.ui.MainViewModel
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent

@Composable
fun HolidaysLeavesScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val holidays by viewModel.holidays.collectAsState()
    val leaves by viewModel.leaves.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var holidayFilter by remember { mutableStateOf("ALL") }
    var showAddHolidayDialog by remember { mutableStateOf(false) }
    var showApplyLeaveDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Natton Technology Calendar Policy",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Shift: 09:30 AM – 06:00 PM • Sunday: Official Paid Weekly Holiday",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HolidayVillage, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Official Holidays (${holidays.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Leaves (${leaves.size})", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTabIndex == 0) {
                val filteredHolidays = when (holidayFilter) {
                    "SUNDAY" -> holidays.filter { it.name.contains("Sunday", ignoreCase = true) }
                    "PUBLIC" -> holidays.filter { !it.name.contains("Sunday", ignoreCase = true) }
                    else -> holidays
                }

                // Month Calendar Summary Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Working", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("21 Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sundays (Off)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("5 Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Public Holidays", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("3 Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = StatusHoliday)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Office Shift", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("09:30–18:00", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusPresent)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All (${holidays.size})", "SUNDAY" to "Sundays Only", "PUBLIC" to "Public / National").forEach { (key, label) ->
                        val selected = holidayFilter == key
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { holidayFilter = key }
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Holidays List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredHolidays, key = { it.id }) { holiday ->
                        HolidayCard(holiday = holiday)
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            } else {
                // Leaves List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(leaves, key = { it.id }) { leave ->
                        LeaveCard(
                            leave = leave,
                            onUpdateStatus = { newStatus ->
                                viewModel.updateLeaveStatus(leave.id, newStatus)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        // Contextual FAB
        FloatingActionButton(
            onClick = {
                if (selectedTabIndex == 0) showAddHolidayDialog = true
                else showApplyLeaveDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("holidays_leaves_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedTabIndex == 0) "Add Holiday" else "Apply Leave",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAddHolidayDialog) {
        AddHolidayDialog(
            onDismiss = { showAddHolidayDialog = false },
            onSave = { name, date, type, isMandatory ->
                viewModel.addHoliday(name, date, type, isMandatory)
                showAddHolidayDialog = false
            }
        )
    }

    if (showApplyLeaveDialog) {
        ApplyLeaveDialog(
            onDismiss = { showApplyLeaveDialog = false },
            onSave = { type, start, end, reason ->
                viewModel.applyLeave(type, start, end, reason)
                showApplyLeaveDialog = false
            }
        )
    }
}

@Composable
private fun HolidayCard(holiday: Holiday) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(StatusHoliday.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EventAvailable,
                        contentDescription = null,
                        tint = StatusHoliday,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = holiday.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${holiday.date} • ${holiday.type}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (holiday.isMandatory) StatusHoliday.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (holiday.isMandatory) "Paid Off" else "Optional",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (holiday.isMandatory) StatusHoliday else Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
private fun LeaveCard(
    leave: LeaveRequest,
    onUpdateStatus: (String) -> Unit
) {
    val isApproved = leave.status.equals("Approved", ignoreCase = true)
    val isPending = leave.status.equals("Pending", ignoreCase = true)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = StatusLeave, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = leave.employeeName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            when {
                                isApproved -> StatusPresent.copy(alpha = 0.15f)
                                isPending -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                                else -> Color(0xFFEF4444).copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = leave.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = when {
                                isApproved -> StatusPresent
                                isPending -> Color(0xFFD97706)
                                else -> Color(0xFFEF4444)
                            }
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${leave.leaveType} (${leave.startDate} to ${leave.endDate})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (leave.reason.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reason: ${leave.reason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isPending) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onUpdateStatus("Rejected") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reject", fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onUpdateStatus("Approved") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddHolidayDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, date: String, type: String, isMandatory: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-12-31") }
    var type by remember { mutableStateOf("Company Holiday") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Official Holiday", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Holiday Name (e.g. New Year's Eve)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = type,
                    onValueChange = { type = it },
                    label = { Text("Holiday Type (Public, National, Company)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, date, type, true) },
                enabled = name.isNotBlank()
            ) {
                Text("Add Holiday")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ApplyLeaveDialog(
    onDismiss: () -> Unit,
    onSave: (type: String, start: String, end: String, reason: String) -> Unit
) {
    var leaveType by remember { mutableStateOf("Vacation") }
    var startDate by remember { mutableStateOf("2026-11-20") }
    var endDate by remember { mutableStateOf("2026-11-22") }
    var reason by remember { mutableStateOf("Personal / Family time") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Leave", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = leaveType,
                    onValueChange = { leaveType = it },
                    label = { Text("Leave Type (Sick, Vacation, Casual)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(leaveType, startDate, endDate, reason) }) {
                Text("Submit Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
