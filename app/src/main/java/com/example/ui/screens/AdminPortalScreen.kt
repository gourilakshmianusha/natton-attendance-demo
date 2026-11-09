package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.Holiday
import com.example.data.model.OfficeStayResult
import com.example.data.model.PunchType
import com.example.data.model.calculateStayDuration
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.BrandSecondary
import com.example.ui.theme.StatusHoliday
import com.example.ui.theme.StatusLate
import com.example.ui.theme.StatusPresent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPortalScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.employees.collectAsState()
    val todayRecords by viewModel.todayRecords.collectAsState()
    val adminSelectedId by viewModel.adminSelectedEmployeeId.collectAsState()
    val isPunching by viewModel.isPunching.collectAsState()
    val holidays by viewModel.holidays.collectAsState()

    var activeAdminTab by remember { mutableStateOf(0) }

    // Live clock for office stay calculation
    var liveSecondsTicking by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            liveSecondsTicking = System.currentTimeMillis() / 1000
            delay(1000L)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))

            // Web-style Admin Header Banner
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BrandPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Natton Technology Admin Desk",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Shift: 09:30 AM – 06:00 PM • Sunday: Weekly Holiday",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (viewModel.isTodaySunday) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Sunday Holiday",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color(0xFF3B82F6),
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            OutlinedButton(
                                onClick = { viewModel.adminLogout() },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp).testTag("admin_logout_button")
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Log Out", fontSize = 11.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Admin Tab Row (Web Sub-Nav)
                    TabRow(
                        selectedTabIndex = activeAdminTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Tab(
                            selected = activeAdminTab == 0,
                            onClick = { activeAdminTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Register Employee", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        )
                        Tab(
                            selected = activeAdminTab == 1,
                            onClick = { activeAdminTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Punch & Stay Calculator", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        )
                        Tab(
                            selected = activeAdminTab == 2,
                            onClick = { activeAdminTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Master Sheet", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }
                        )
                    }
                }
            }
        }

        // TAB 0: REGISTER EMPLOYEE INFORMATION
        if (activeAdminTab == 0) {
            item {
                ActualCalendarHolidaysBanner(
                    holidays = holidays,
                    isTodaySunday = viewModel.isTodaySunday
                )
            }

            item {
                EmployeeRegistrationCard(
                    employeesCount = employees.size,
                    onRegister = { name, email, role, dept, shiftStart, shiftEnd, location, phone, customId ->
                        val shiftName = "Day Shift ($shiftStart - $shiftEnd)"
                        viewModel.addEmployee(name, email, role, dept, shiftName, shiftStart, shiftEnd, location, phone, customId)
                        activeAdminTab = 1 // Automatically navigate to Punch Desk to test punch!
                    }
                )
            }

            item {
                Text(
                    text = "Registered Employee Directory (${employees.size})",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(employees, key = { it.id }) { emp ->
                RegisteredEmployeeRow(
                    employee = emp,
                    onPunchForEmp = {
                        viewModel.selectAdminEmployee(emp.id)
                        activeAdminTab = 1
                    },
                    onDelete = { viewModel.deleteEmployee(emp.id) }
                )
            }
        }

        // TAB 1: PUNCH DESK & AUTOMATIC OFFICE STAY CALCULATOR
        if (activeAdminTab == 1) {
            item {
                val selectedEmployee = employees.find { it.id == adminSelectedId } ?: employees.firstOrNull()
                val selectedRecord = todayRecords.find { it.employeeId == selectedEmployee?.id }

                PunchAndStayCalculatorCard(
                    employees = employees,
                    selectedEmployee = selectedEmployee,
                    record = selectedRecord,
                    isPunching = isPunching,
                    onSelectEmployee = { viewModel.selectAdminEmployee(it) },
                    onPunchIn = {
                        selectedEmployee?.let { viewModel.punchForEmployee(it.id, PunchType.IN) }
                    },
                    onPunchOut = {
                        selectedEmployee?.let { viewModel.punchForEmployee(it.id, PunchType.OUT) }
                    },
                    onBreak = {
                        selectedEmployee?.let { viewModel.punchForEmployee(it.id, PunchType.BREAK_START) }
                    }
                )
            }

            // Interactive Stay Duration Simulator / Overrider
            item {
                OfficeStaySimulatorCard(viewModel = viewModel)
            }
        }

        // TAB 2: MASTER ATTENDANCE SHEET
        if (activeAdminTab == 2) {
            item {
                MasterAttendanceSheetCard(
                    employees = employees,
                    todayRecords = todayRecords,
                    onPunchForEmployee = { empId ->
                        viewModel.selectAdminEmployee(empId)
                        activeAdminTab = 1
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// ----------------- COMPONENT 1: EMPLOYEE REGISTRATION FORM -----------------
@Composable
private fun EmployeeRegistrationCard(
    employeesCount: Int,
    onRegister: (name: String, email: String, role: String, dept: String, shiftStart: String, shiftEnd: String, location: String, phone: String, customId: String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("Engineering") }
    var shiftStart by remember { mutableStateOf("09:30") }
    var shiftEnd by remember { mutableStateOf("18:00") }
    var location by remember { mutableStateOf("Natton HQ - Floor 4") }
    var phone by remember { mutableStateOf("+1 (555) 018-9922") }
    var customId by remember { mutableStateOf("") }

    val nextId = if (customId.isNotBlank()) customId.trim() else "EMP-${1001 + employeesCount}"
    val departments = listOf("Engineering", "Product", "Design", "Human Resources", "Finance", "Operations", "Quality Assurance", "Sales")
    var showDeptMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "New Employee Registration",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Fill in employee credentials to enable automated attendance and time-tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Assigned ID: $nextId",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // Notice Banner with Actual Calendar Holidays & Shift Policies
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = BrandPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Actual Calendar Holidays & Shift Policy",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Sunday: Weekly Off",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6)
                            )
                        }
                    }
                    Text(
                        text = "Standard Natton Office Shift: 09:30 AM to 06:00 PM. Registered employees receive full paid attendance for all official calendar holidays (Nov 11 Veterans Day, Nov 26 Thanksgiving, Dec 25 Christmas, Jan 01 New Year's Day, and all Sundays).",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Name & Email
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Legal Name") },
                    placeholder = { Text("e.g. Jason Scott") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Work Email") },
                    placeholder = { Text("jason@company.com") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_email_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Role & Department
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Job Role / Designation") },
                    placeholder = { Text("e.g. Frontend Architect") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_role_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = department,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Department") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDeptMenu = true }
                            .testTag("reg_dept_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(
                        expanded = showDeptMenu,
                        onDismissRequest = { showDeptMenu = false }
                    ) {
                        departments.forEach { dept ->
                            DropdownMenuItem(
                                text = { Text(dept) },
                                onClick = {
                                    department = dept
                                    showDeptMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Shift Timings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = shiftStart,
                    onValueChange = { shiftStart = it },
                    label = { Text("Shift In Time") },
                    placeholder = { Text("09:00") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_shift_in_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = shiftEnd,
                    onValueChange = { shiftEnd = it },
                    label = { Text("Shift Out Time") },
                    placeholder = { Text("18:00") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_shift_out_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Office Floor / Site") },
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            // Optional Custom Employee ID & Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = customId,
                    onValueChange = { customId = it },
                    label = { Text("Employee ID (Optional)") },
                    placeholder = { Text("Auto: $nextId") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reg_custom_id_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Phone") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onRegister(name, email, role, department, shiftStart, shiftEnd, location, phone, customId.ifBlank { null })
                    }
                },
                enabled = name.isNotBlank() && role.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_employee_registration"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Register Employee & Proceed to Punch Desk", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ----------------- COMPONENT 2: PUNCH DESK & OFFICE STAY CALCULATOR -----------------
@Composable
private fun PunchAndStayCalculatorCard(
    employees: List<Employee>,
    selectedEmployee: Employee?,
    record: AttendanceRecord?,
    isPunching: Boolean,
    onSelectEmployee: (String) -> Unit,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    onBreak: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    val inTime = record?.inTime
    val outTime = record?.outTime
    val isCheckedIn = inTime != null
    val isCheckedOut = outTime != null

    // Auto-calculate office stay duration
    val stayResult = remember(inTime, outTime, record?.breakMinutes) {
        calculateStayDuration(inTime, outTime, selectedEmployee?.shiftStart ?: "09:30", record?.breakMinutes ?: 0)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Employee Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Punch In / Out & Office Stay Console",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Punch in arrival and departure to calculate total office stay hours automatically",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("admin_punch_employee_picker")
                    ) {
                        Text(selectedEmployee?.name ?: "Select", maxLines = 1, fontSize = 12.sp)
                    }

                    DropdownMenu(expanded = showPicker, onDismissRequest = { showPicker = false }) {
                        employees.forEach { emp ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(emp.name, fontWeight = FontWeight.Bold)
                                        Text("${emp.id} • ${emp.department}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    onSelectEmployee(emp.id)
                                    showPicker = false
                                }
                            )
                        }
                    }
                }
            }

            // Employee Badge Box
            selectedEmployee?.let { emp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(emp.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emp.name.firstOrNull()?.toString() ?: "E",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = emp.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "${emp.id} • ${emp.role}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(
                                when {
                                    isCheckedIn && !isCheckedOut -> StatusPresent.copy(alpha = 0.2f)
                                    isCheckedOut -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                    else -> Color.Gray.copy(alpha = 0.2f)
                                }
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = when {
                                isCheckedIn && !isCheckedOut -> "Currently in Office"
                                isCheckedOut -> "Shift Completed"
                                else -> "Not Punched In"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = when {
                                isCheckedIn && !isCheckedOut -> StatusPresent
                                isCheckedOut -> Color(0xFF2563EB)
                                else -> Color.Gray
                            }
                        )
                    }
                }
            }

            // Big Action Punch Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Punch In Button
                Button(
                    onClick = onPunchIn,
                    enabled = !isPunching && !isCheckedIn,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("admin_punch_in_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresent)
                ) {
                    Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PUNCH IN", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        if (inTime != null) {
                            Text("Punched: $inTime", fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }

                // Punch Out Button
                Button(
                    onClick = onPunchOut,
                    enabled = !isPunching && isCheckedIn && !isCheckedOut,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("admin_punch_out_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PUNCH OUT", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        if (outTime != null) {
                            Text("Punched: $outTime", fontSize = 10.sp, color = Color.White.copy(alpha = 0.9f))
                        }
                    }
                }
            }

            // AUTOMATIC STAY DURATION RESULTS CARD
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
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
                        Text(
                            text = "AUTOMATED OFFICE STAY DURATION",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = stayResult.complianceText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (stayResult.netHours >= 8.0) StatusPresent else StatusLate
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StayMetricTile(
                            label = "ARRIVAL (IN)",
                            value = inTime ?: "--:--:--",
                            color = Color(0xFF38BDF8)
                        )
                        StayMetricTile(
                            label = "DEPARTURE (OUT)",
                            value = outTime ?: if (isCheckedIn) "Working..." else "--:--:--",
                            color = Color(0xFF38BDF8)
                        )
                        StayMetricTile(
                            label = "GROSS STAY",
                            value = if (isCheckedOut) stayResult.grossFormatted else if (isCheckedIn) "In Session" else "--",
                            color = StatusPresent
                        )
                        StayMetricTile(
                            label = "EFFECTIVE WORK",
                            value = if (isCheckedOut) "${stayResult.netHours} hrs" else if (isCheckedIn) "Calculating" else "--",
                            color = Color(0xFFFBBF24)
                        )
                    }

                    if (isCheckedOut && stayResult.overtimeHours > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusLate.copy(alpha = 0.2f))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉 Overtime Recorded: +${stayResult.overtimeHours} hours approved for payroll bonus",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusLate
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StayMetricTile(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF94A3B8))
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color, fontFamily = FontFamily.Monospace)
    }
}

// ----------------- COMPONENT 3: OFFICE STAY SIMULATOR -----------------
@Composable
private fun OfficeStaySimulatorCard(viewModel: MainViewModel) {
    val inTime by viewModel.customInTime.collectAsState()
    val outTime by viewModel.customOutTime.collectAsState()
    val breakMin by viewModel.customBreakMinutes.collectAsState()
    val result by viewModel.simulatedStayResult.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Live Stay Hours Calculator / Simulator",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "Test or simulate any arrival and departure time to see instant office stay calculations:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inTime,
                    onValueChange = { viewModel.customInTime.value = it },
                    label = { Text("Test In Time") },
                    placeholder = { Text("09:30:00") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = outTime,
                    onValueChange = { viewModel.customOutTime.value = it },
                    label = { Text("Test Out Time") },
                    placeholder = { Text("18:15:00") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = breakMin.toString(),
                    onValueChange = { viewModel.customBreakMinutes.value = it.toIntOrNull() ?: 0 },
                    label = { Text("Break (mins)") },
                    modifier = Modifier.weight(0.8f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            // Calculation Results
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Total Time in Office:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = result.grossFormatted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Effective Work (${result.breakMinutes}m break):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${result.netHours} hrs (${result.complianceText})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.netHours >= 8.0) StatusPresent else StatusLate
                    )
                }
            }
        }
    }
}

// ----------------- COMPONENT 4: MASTER ATTENDANCE SHEET -----------------
@Composable
private fun MasterAttendanceSheetCard(
    employees: List<Employee>,
    todayRecords: List<AttendanceRecord>,
    onPunchForEmployee: (String) -> Unit
) {
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
                text = "Today's Master Attendance Log Sheet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Real-time records of all registered employees and office stay hours",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            employees.forEach { emp ->
                val rec = todayRecords.find { it.employeeId == emp.id }
                val stayResult = calculateStayDuration(rec?.inTime, rec?.outTime, emp.shiftStart, rec?.breakMinutes ?: 0)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(emp.avatarColorHex)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emp.name.firstOrNull()?.toString() ?: "E", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = emp.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(text = "${emp.id} • ${emp.department}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (rec?.inTime != null) "In: ${rec.inTime}" else "Not In",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (rec?.inTime != null) StatusPresent else Color.Gray
                        )
                        Text(
                            text = if (rec?.outTime != null) "Out: ${rec.outTime}" else if (rec?.inTime != null) "In Office" else "-",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (rec?.outTime != null) stayResult.grossFormatted else if (rec?.inTime != null) "Stay in progress" else "0h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (rec?.outTime != null) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                        rec?.status?.let { StatusBadge(status = it, compact = true) }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            }
        }
    }
}

// ----------------- COMPONENT 5: REGISTERED EMPLOYEE ROW -----------------
@Composable
private fun RegisteredEmployeeRow(
    employee: Employee,
    onPunchForEmp: () -> Unit,
    onDelete: () -> Unit
) {
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(employee.avatarColorHex)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = employee.name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString(""),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = employee.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(employee.id, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Text(
                        text = "${employee.role} • ${employee.department} • Shift: ${employee.shiftStart}-${employee.shiftEnd}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onPunchForEmp,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Punch", fontSize = 11.sp)
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ----------------- COMPONENT 5: ACTUAL CALENDAR HOLIDAYS & OFFICE SCHEDULE BANNER -----------------
@Composable
fun ActualCalendarHolidaysBanner(
    holidays: List<Holiday>,
    isTodaySunday: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Actual Calendar Holidays & Schedule",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Office Hours: 09:30 AM - 06:00 PM • Sunday Off",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isTodaySunday) Color(0xFF3B82F6).copy(alpha = 0.2f)
                            else StatusPresent.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isTodaySunday) "Today: Sunday Holiday" else "Live Calendar Active",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTodaySunday) Color(0xFF3B82F6) else StatusPresent
                    )
                }
            }

            // Official message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = BrandPrimary,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Official Natton Calendar Policy: Office operating hours are strictly 09:30 AM to 06:00 PM (Monday–Saturday). All Sundays are official paid weekly off. Registered employees receive full paid leave on all declared public, national, and festival holidays.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Upcoming Actual Calendar Holidays",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Horizontal Scrollable Actual Holidays Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sampleHolidays = holidays.ifEmpty {
                    listOf(
                        Holiday(name = "Veterans Day", date = "2026-11-11", type = "National Holiday", isMandatory = true),
                        Holiday(name = "Sunday Weekly Off", date = "2026-11-15", type = "Sunday Weekly Holiday", isMandatory = true),
                        Holiday(name = "Sunday Weekly Off", date = "2026-11-22", type = "Sunday Weekly Holiday", isMandatory = true),
                        Holiday(name = "Thanksgiving Day", date = "2026-11-26", type = "Public Holiday", isMandatory = true),
                        Holiday(name = "Day After Thanksgiving", date = "2026-11-27", type = "Company Holiday", isMandatory = true),
                        Holiday(name = "Sunday Weekly Off", date = "2026-11-29", type = "Sunday Weekly Holiday", isMandatory = true),
                        Holiday(name = "Sunday Weekly Off", date = "2026-12-06", type = "Sunday Weekly Holiday", isMandatory = true),
                        Holiday(name = "Sunday Weekly Off", date = "2026-12-13", type = "Sunday Weekly Holiday", isMandatory = true),
                        Holiday(name = "Christmas Day", date = "2026-12-25", type = "Public Holiday", isMandatory = true),
                        Holiday(name = "New Year's Day 2027", date = "2027-01-01", type = "Public Holiday", isMandatory = true)
                    )
                }

                sampleHolidays.take(8).forEach { h ->
                    ActualHolidayChip(holiday = h)
                }
            }
        }
    }
}

@Composable
private fun ActualHolidayChip(holiday: Holiday) {
    val isSunday = holiday.name.contains("Sunday", ignoreCase = true)
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSunday) Color(0xFF3B82F6).copy(alpha = 0.4f) else StatusHoliday.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        if (isSunday) Color(0xFF3B82F6).copy(alpha = 0.15f)
                        else StatusHoliday.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = holiday.date.removePrefix("2026-"),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isSunday) Color(0xFF2563EB) else StatusHoliday
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = holiday.name,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isSunday) "Official Weekly Off" else holiday.type,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

