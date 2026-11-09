package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HolidayVillage
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmployeeAttendanceReport
import com.example.ui.MainViewModel
import com.example.ui.components.AttendanceDonutChart
import com.example.ui.components.StatusBadge
import com.example.ui.components.WeeklyHoursBarChart
import com.example.ui.theme.StatusAbsent
import com.example.ui.theme.StatusHalfDay
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusLeave
import com.example.ui.theme.StatusPresent
import java.util.Locale

@Composable
fun EmployeeReportScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val employees by viewModel.employees.collectAsState()
    val reportEmployeeId by viewModel.selectedReportEmployeeId.collectAsState()
    val reportMonth by viewModel.selectedReportMonth.collectAsState()
    val report by viewModel.employeeReport.collectAsState()

    var showEmployeeMenu by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportContent by remember { mutableStateOf("") }

    val months = listOf(
        "2026-11" to "November 2026 (Current)",
        "2026-10" to "October 2026",
        "2026-09" to "September 2026"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Report Header Filter Card
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
                        text = "Natton Technology - Attendance & Hours Report",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Office Shift: 09:30 AM – 06:00 PM • Sunday: Weekly Holiday",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Employee Selector
                        Box(modifier = Modifier.weight(1.2f)) {
                            OutlinedButton(
                                onClick = { showEmployeeMenu = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("report_employee_selector"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                val current = employees.find { it.id == reportEmployeeId }
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = current?.name ?: "Select Employee",
                                    fontSize = 12.sp,
                                    maxLines = 1
                                )
                            }

                            DropdownMenu(
                                expanded = showEmployeeMenu,
                                onDismissRequest = { showEmployeeMenu = false }
                            ) {
                                employees.forEach { emp ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(emp.name, fontWeight = FontWeight.Bold)
                                                Text("${emp.id} • ${emp.department}", fontSize = 11.sp, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            viewModel.selectReportEmployee(emp.id)
                                            showEmployeeMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        // Month Selector Chips
                        Box(modifier = Modifier.weight(1f)) {
                            val currentMonthLabel = months.find { it.first == reportMonth }?.second?.substringBefore(" ") ?: "Nov 2026"
                            var showMonthMenu by remember { mutableStateOf(false) }

                            OutlinedButton(
                                onClick = { showMonthMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(currentMonthLabel, fontSize = 12.sp, maxLines = 1)
                            }

                            DropdownMenu(
                                expanded = showMonthMenu,
                                onDismissRequest = { showMonthMenu = false }
                            ) {
                                months.forEach { (mVal, mLabel) ->
                                    DropdownMenuItem(
                                        text = { Text(mLabel) },
                                        onClick = {
                                            viewModel.selectReportMonth(mVal)
                                            showMonthMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Export / Share Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                exportContent = viewModel.getCsvReportExport()
                                showExportDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_csv_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export CSV Report", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val csv = viewModel.getCsvReportExport()
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Attendance Report", csv))
                                exportContent = csv
                                showExportDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_report_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share / Copy", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        if (report == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No attendance data found for the selected period", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            val r = report!!

            // Core KPI Metric Tiles
            item {
                ReportKpiGrid(report = r)
            }

            // In-Time & Out-Time Analysis Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Punctuality & Timings Summary",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TimingStatCard(
                                icon = Icons.Default.Login,
                                label = "Avg In Time",
                                value = r.averageInTime,
                                subtext = "Shift Start: ${r.employee.shiftStart}",
                                color = StatusPresent
                            )
                            TimingStatCard(
                                icon = Icons.Default.Logout,
                                label = "Avg Out Time",
                                value = r.averageOutTime,
                                subtext = "Shift End: ${r.employee.shiftEnd}",
                                color = Color(0xFF3B82F6)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            TimingStatCard(
                                icon = Icons.Default.Schedule,
                                label = "Total Hours",
                                value = "${r.totalWorkHours} hrs",
                                subtext = "Standard: ${r.totalWorkingDays * 8} hrs",
                                color = MaterialTheme.colorScheme.primary
                            )
                            TimingStatCard(
                                icon = Icons.Default.HourglassBottom,
                                label = "Total Overtime",
                                value = "+${r.totalOvertimeHours} hrs",
                                subtext = "Approved OT Pay",
                                color = StatusLate
                            )
                        }
                    }
                }
            }

            // Visual Charts Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Attendance Breakdown (${r.monthYear})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        AttendanceDonutChart(report = r)
                        Spacer(modifier = Modifier.height(20.dp))
                        WeeklyHoursBarChart(records = r.dailyRecords)
                    }
                }
            }

            // Detailed Day-by-Day Audit Table Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Attendance Punch Log (${r.dailyRecords.size} Days)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Daily Records List
            items(r.dailyRecords) { record ->
                DailyReportRow(record = record)
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Attendance Report Export", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Formatted CSV data ready for payroll and HR audits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .padding(10.dp)
                    ) {
                        LazyColumn {
                            item {
                                Text(
                                    text = exportContent,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Attendance Report CSV", exportContent))
                    showExportDialog = false
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportKpiGrid(report: EmployeeAttendanceReport) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Days Present",
                value = "${report.presentDays} Days",
                subtitle = String.format(Locale.US, "%.1f%% Rate", report.attendancePercentage),
                color = StatusPresent,
                icon = Icons.Default.CheckCircle,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Paid Holidays",
                value = "${report.holidayDays} Days",
                subtitle = "Official Holidays",
                color = StatusHoliday,
                icon = Icons.Default.HolidayVillage,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Late Arrivals",
                value = "${report.lateDays} Days",
                subtitle = String.format(Locale.US, "%.0f%% Punctual", report.punctualityPercentage),
                color = StatusLate,
                icon = Icons.Default.Schedule,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Half Days",
                value = "${report.halfDays} Days",
                subtitle = "Half Shifts",
                color = StatusHalfDay,
                icon = Icons.Default.HourglassBottom,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiCard(
                title = "Absences",
                value = "${report.absentDays} Days",
                subtitle = "Unscheduled",
                color = StatusAbsent,
                icon = Icons.Default.TimerOff,
                modifier = Modifier.weight(1f)
            )
            KpiCard(
                title = "Approved Leaves",
                value = "${report.leaveDays} Days",
                subtitle = "Vacation / Sick",
                color = StatusLeave,
                icon = Icons.Default.EventBusy,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = color
            )
        }
    }
}

@Composable
private fun TimingStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    subtext: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Color.Gray
        )
    }
}

@Composable
private fun DailyReportRow(record: AttendanceRecord) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.date,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(status = record.status, compact = true)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "In: ${record.inTime ?: "--"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Out: ${record.outTime ?: "--"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (record.workHours > 0) {
                        Text(
                            text = "${record.workHours} hrs",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (record.overtimeHours > 0) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(StatusLate.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "+${record.overtimeHours}h OT",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = StatusLate
                    )
                }
            }
        }
    }
}
