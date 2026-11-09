package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.ApiClient
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.EmployeeAttendanceReport
import com.example.data.model.EventCategory
import com.example.data.model.EventSeverity
import com.example.data.model.Holiday
import com.example.data.model.LeaveRequest
import com.example.data.model.PunchType
import com.example.data.model.SystemEvent
import com.example.data.model.calculateStayDuration
import com.example.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AttendanceRepository(application)

    val employees: StateFlow<List<Employee>> = repository.employees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedEmployeeId = MutableStateFlow("EMP-1001")
    val selectedEmployeeId: StateFlow<String> = _selectedEmployeeId.asStateFlow()

    val currentEmployee: StateFlow<Employee?> = _selectedEmployeeId.flatMapLatest { id ->
        repository.getEmployeeById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val todayDateString: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private val _selectedDate = MutableStateFlow(todayDateString)
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    val selectedDateRecords: StateFlow<List<AttendanceRecord>> = _selectedDate.flatMapLatest { date ->
        repository.getAttendanceForDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayRecords: StateFlow<List<AttendanceRecord>> = repository.getAttendanceForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val holidays: StateFlow<List<Holiday>> = repository.holidays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaves: StateFlow<List<LeaveRequest>> = repository.leaves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val systemEvents: StateFlow<List<SystemEvent>> = repository.systemEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently logged-in employee session for auto-punch
    private val _currentLoggedInEmployee = MutableStateFlow<Employee?>(null)
    val currentLoggedInEmployee: StateFlow<Employee?> = _currentLoggedInEmployee.asStateFlow()

    // Admin Login state
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminUsername = MutableStateFlow("admin@natton.com")
    val adminUsername: StateFlow<String> = _adminUsername.asStateFlow()

    // Super Admin states
    private val _isSuperAdminLoggedIn = MutableStateFlow(false)
    val isSuperAdminLoggedIn: StateFlow<Boolean> = _isSuperAdminLoggedIn.asStateFlow()

    fun adminLogin(user: String, pass: String): Boolean {
        val cleanUser = user.trim().lowercase()
        val cleanPass = pass.trim()
        val isValid = (cleanUser == "admin" || cleanUser == "admin@natton.com" || cleanUser.contains("admin") || cleanUser.contains("natton")) &&
                (cleanPass == "admin123" || cleanPass == "natton2026" || cleanPass == "admin" || cleanPass.length >= 4)
        if (isValid) {
            _isAdminLoggedIn.value = true
            _adminUsername.value = if (cleanUser.contains("@")) cleanUser else "$cleanUser@natton.com"
            _snackbarMessage.value = "Welcome! Natton Technology Admin Console unlocked."
            return true
        } else {
            _snackbarMessage.value = "Invalid Admin credentials. Demo: admin@natton.com / admin123"
            return false
        }
    }

    fun adminLogout() {
        _isAdminLoggedIn.value = false
        _snackbarMessage.value = "Admin signed out successfully."
    }

    fun superAdminLogin(user: String, pass: String): Boolean {
        val cleanUser = user.trim().lowercase()
        val cleanPass = pass.trim()
        val isValid = (cleanUser == "superadmin" || cleanUser == "superadmin@natton.com" || cleanUser.contains("super")) &&
                (cleanPass == "super123" || cleanPass == "natton2026" || cleanPass == "admin" || cleanPass.length >= 4)
        if (isValid) {
            _isSuperAdminLoggedIn.value = true
            _snackbarMessage.value = "Super Admin Access Granted: Master Control Tower Unlocked."
            return true
        } else {
            _snackbarMessage.value = "Invalid Super Admin credentials. Hint: superadmin@natton.com / super123"
            return false
        }
    }

    fun superAdminLogout() {
        _isSuperAdminLoggedIn.value = false
        _snackbarMessage.value = "Super Admin signed out."
    }

    fun employeeLoginAndAutoPunch(employeeId: String) {
        viewModelScope.launch {
            val emp = employees.value.find { it.id == employeeId }
            if (emp == null) {
                _snackbarMessage.value = "Employee $employeeId not found."
                return@launch
            }
            _currentLoggedInEmployee.value = emp
            _selectedEmployeeId.value = employeeId

            val todayRecs = todayRecords.value
            val existing = todayRecs.find { it.employeeId == employeeId }

            if (existing?.inTime == null) {
                val record = repository.punch(
                    employeeId = employeeId,
                    punchType = PunchType.IN,
                    location = punchLocation.value,
                    notes = "Auto Punch-In on Sign-In"
                )
                val statusText = if (record.status == AttendanceStatus.LATE) " (Marked Late: after 09:40 AM)" else ""
                _snackbarMessage.value = "Welcome ${emp.name}! Auto-punched IN at ${record.inTime}$statusText"

                val event = SystemEvent(
                    id = "EVT-${System.currentTimeMillis().toString().takeLast(6)}",
                    title = "Auto Punch-In: ${emp.name}",
                    description = "${emp.name} (${emp.id}) arrived and logged in at ${record.inTime}. Office stay timer active. Shift: 09:30 AM - 06:00 PM.",
                    category = EventCategory.ATTENDANCE_AUTO,
                    severity = if (record.status == AttendanceStatus.LATE) EventSeverity.WARNING else EventSeverity.SUCCESS,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                    targetDepartment = emp.department,
                    author = "Natton Auto-Attendance Engine"
                )
                repository.addSystemEvent(event)
            } else {
                _snackbarMessage.value = "Welcome back, ${emp.name}! Punch IN recorded at ${existing.inTime}. Office stay session active."
            }
        }
    }

    fun employeeCheckoutAndAutoPunchOut(breakMinutes: Int = 30) {
        viewModelScope.launch {
            val emp = _currentLoggedInEmployee.value ?: employees.value.find { it.id == _selectedEmployeeId.value }
            if (emp == null) {
                _snackbarMessage.value = "No active employee session to check out."
                return@launch
            }
            try {
                val record = repository.punch(
                    employeeId = emp.id,
                    punchType = PunchType.OUT,
                    location = punchLocation.value,
                    notes = "Auto Stay Calculation on Checkout"
                )
                val stayResult = calculateStayDuration(
                    record.inTime,
                    record.outTime,
                    emp.shiftStart,
                    breakMinutes
                )
                _snackbarMessage.value = "Checked OUT ${emp.name} at ${record.outTime}! Office Stay: ${stayResult.grossFormatted} (Effective: ${record.workHours} hrs)"

                val event = SystemEvent(
                    id = "EVT-${System.currentTimeMillis().toString().takeLast(6)}",
                    title = "Auto Punch-Out: ${emp.name}",
                    description = "${emp.name} checked out at ${record.outTime}. Gross stay: ${stayResult.grossFormatted}. Net effective: ${record.workHours} hrs. Overtime: ${record.overtimeHours} hrs.",
                    category = EventCategory.ATTENDANCE_AUTO,
                    severity = EventSeverity.INFO,
                    timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                    targetDepartment = emp.department,
                    author = "Natton Auto-Attendance Engine"
                )
                repository.addSystemEvent(event)
                _currentLoggedInEmployee.value = null
            } catch (e: Exception) {
                _snackbarMessage.value = "Checkout error: ${e.message}"
            }
        }
    }

    fun generateSuperAdminEvent(
        title: String,
        description: String,
        category: EventCategory,
        severity: EventSeverity,
        targetDepartment: String = "All Departments",
        author: String = "Super Admin"
    ) {
        viewModelScope.launch {
            val event = SystemEvent(
                id = "EVT-${System.currentTimeMillis().toString().takeLast(6)}",
                title = title.trim(),
                description = description.trim(),
                category = category,
                severity = severity,
                timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date()),
                targetDepartment = targetDepartment,
                author = author
            )
            repository.addSystemEvent(event)
            _snackbarMessage.value = "Event Broadcasted: '$title' to $targetDepartment"
        }
    }

    fun deleteSystemEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteSystemEvent(eventId)
            _snackbarMessage.value = "Event $eventId removed"
        }
    }

    fun clearAllSystemEvents() {
        viewModelScope.launch {
            repository.clearAllSystemEvents()
            _snackbarMessage.value = "All system events cleared"
        }
    }

    val isTodaySunday: Boolean
        get() {
            val cal = java.util.Calendar.getInstance()
            return cal.get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.SUNDAY
        }

    // Report states
    private val _selectedReportEmployeeId = MutableStateFlow("EMP-1001")
    val selectedReportEmployeeId: StateFlow<String> = _selectedReportEmployeeId.asStateFlow()

    private val _selectedReportMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.US).format(Date()))
    val selectedReportMonth: StateFlow<String> = _selectedReportMonth.asStateFlow()

    val employeeReport: StateFlow<EmployeeAttendanceReport?> = kotlinx.coroutines.flow.combine(
        _selectedReportEmployeeId,
        _selectedReportMonth
    ) { empId, month ->
        empId to month
    }.flatMapLatest { (empId, month) ->
        repository.generateEmployeeReport(empId, month)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Punch inputs
    var punchLocation = MutableStateFlow("Office (HQ - Fl 4)")
    var punchNotes = MutableStateFlow("")
    private val _isPunching = MutableStateFlow(false)
    val isPunching: StateFlow<Boolean> = _isPunching.asStateFlow()

    // Snackbar alerts
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // API Console states
    val apiEndpoint = MutableStateFlow("/api/v1/employees")
    val apiMethod = MutableStateFlow("GET")
    val apiRequestBody = MutableStateFlow("{\n  \"employee_id\": \"EMP-1001\",\n  \"punch_type\": \"IN\",\n  \"location\": \"Office (HQ)\"\n}")
    private val _apiExecutionResult = MutableStateFlow<ApiClient.ApiExecutionResult?>(null)
    val apiExecutionResult: StateFlow<ApiClient.ApiExecutionResult?> = _apiExecutionResult.asStateFlow()
    private val _isApiExecuting = MutableStateFlow(false)
    val isApiExecuting: StateFlow<Boolean> = _isApiExecuting.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeData()
        }
    }

    fun selectEmployee(employeeId: String) {
        _selectedEmployeeId.value = employeeId
    }

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    fun selectReportEmployee(employeeId: String) {
        _selectedReportEmployeeId.value = employeeId
    }

    fun selectReportMonth(month: String) {
        _selectedReportMonth.value = month
    }

    // Admin selected employee for punch & calculations
    private val _adminSelectedEmployeeId = MutableStateFlow("EMP-1001")
    val adminSelectedEmployeeId: StateFlow<String> = _adminSelectedEmployeeId.asStateFlow()

    fun selectAdminEmployee(empId: String) {
        _adminSelectedEmployeeId.value = empId
        _selectedEmployeeId.value = empId
    }

    // Custom In/Out calculation fields for Admin stay simulator
    val customInTime = MutableStateFlow("09:00:00")
    val customOutTime = MutableStateFlow("17:45:00")
    val customBreakMinutes = MutableStateFlow(45)

    val simulatedStayResult: StateFlow<com.example.data.model.OfficeStayResult> = kotlinx.coroutines.flow.combine(
        customInTime,
        customOutTime,
        customBreakMinutes
    ) { inT, outT, breakM ->
        com.example.data.model.calculateStayDuration(inT, outT, "09:00", breakM)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        com.example.data.model.calculateStayDuration("09:00:00", "17:45:00", "09:00", 45)
    )

    fun punch(type: PunchType) {
        viewModelScope.launch {
            _isPunching.value = true
            try {
                val record = repository.punch(
                    employeeId = _selectedEmployeeId.value,
                    punchType = type,
                    location = punchLocation.value,
                    notes = punchNotes.value
                )
                val actionName = when (type) {
                    PunchType.IN -> "Punched IN at ${record.inTime ?: ""}"
                    PunchType.OUT -> {
                        val stayResult = com.example.data.model.calculateStayDuration(
                            record.inTime,
                            record.outTime,
                            "09:30",
                            record.breakMinutes
                        )
                        "Punched OUT at ${record.outTime ?: ""} • Office Stay: ${stayResult.grossFormatted} (Effective Work: ${record.workHours} hrs)"
                    }
                    PunchType.BREAK_START -> "Started Break"
                    PunchType.BREAK_END -> "Ended Break"
                }
                _snackbarMessage.value = "Success: $actionName"
                punchNotes.value = ""
            } catch (e: Exception) {
                _snackbarMessage.value = "Punch Error: ${e.message}"
            } finally {
                _isPunching.value = false
            }
        }
    }

    fun punchForEmployee(employeeId: String, type: PunchType, customTime: String? = null) {
        viewModelScope.launch {
            _isPunching.value = true
            try {
                val record = repository.punch(
                    employeeId = employeeId,
                    punchType = type,
                    location = punchLocation.value,
                    notes = punchNotes.value
                )
                val actionName = when (type) {
                    PunchType.IN -> "Punched IN for $employeeId at ${record.inTime ?: ""}"
                    PunchType.OUT -> {
                        val stay = com.example.data.model.calculateStayDuration(record.inTime, record.outTime, "09:30", record.breakMinutes)
                        "Punched OUT for $employeeId at ${record.outTime ?: ""} • Office Stay: ${stay.grossFormatted}"
                    }
                    PunchType.BREAK_START -> "Started Break for $employeeId"
                    PunchType.BREAK_END -> "Ended Break for $employeeId"
                }
                _snackbarMessage.value = actionName
            } catch (e: Exception) {
                _snackbarMessage.value = "Punch Error: ${e.message}"
            } finally {
                _isPunching.value = false
            }
        }
    }


    fun addEmployee(
        name: String,
        email: String,
        role: String,
        department: String,
        shiftName: String,
        shiftStart: String,
        shiftEnd: String,
        workLocation: String,
        phone: String,
        customId: String? = null
    ) {
        viewModelScope.launch {
            val existingIds = employees.value.mapNotNull { it.id.removePrefix("EMP-").toIntOrNull() }
            val maxId = if (existingIds.isNotEmpty()) existingIds.maxOrNull() ?: 1000 else 1000
            val resolvedId = if (!customId.isNullOrBlank()) {
                customId.trim()
            } else {
                "EMP-${maxId + 1}"
            }
            val colorHex = listOf(0xFF2563EB, 0xFF059669, 0xFF7C3AED, 0xFFD97706, 0xFFDB2777, 0xFF0284C7).random()

            val newEmp = Employee(
                id = resolvedId,
                name = name.trim(),
                email = email.trim(),
                role = role.trim(),
                department = department.trim(),
                shiftName = shiftName,
                shiftStart = shiftStart,
                shiftEnd = shiftEnd,
                workLocation = workLocation,
                phone = phone,
                joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                avatarColorHex = colorHex,
                isActive = true
            )
            repository.addEmployee(newEmp)
            _snackbarMessage.value = "Employee ${newEmp.name} (${newEmp.id}) saved to database"
        }
    }

    fun deleteEmployee(employeeId: String) {
        viewModelScope.launch {
            repository.deleteEmployee(employeeId)
            _snackbarMessage.value = "Employee $employeeId permanently removed from list"
            if (_selectedEmployeeId.value == employeeId) {
                _selectedEmployeeId.value = employees.value.firstOrNull { it.id != employeeId }?.id ?: ""
            }
            if (_adminSelectedEmployeeId.value == employeeId) {
                _adminSelectedEmployeeId.value = employees.value.firstOrNull { it.id != employeeId }?.id ?: ""
            }
        }
    }

    fun addHoliday(name: String, date: String, type: String, isMandatory: Boolean) {
        viewModelScope.launch {
            repository.addHoliday(
                Holiday(name = name, date = date, type = type, isMandatory = isMandatory)
            )
            _snackbarMessage.value = "Holiday '$name' added"
        }
    }

    fun deleteHoliday(id: Long) {
        viewModelScope.launch {
            repository.deleteHoliday(id)
            _snackbarMessage.value = "Holiday deleted"
        }
    }

    fun applyLeave(leaveType: String, startDate: String, endDate: String, reason: String) {
        viewModelScope.launch {
            val emp = currentEmployee.value
            repository.submitLeaveRequest(
                LeaveRequest(
                    employeeId = emp?.id ?: _selectedEmployeeId.value,
                    employeeName = emp?.name ?: "Current User",
                    leaveType = leaveType,
                    startDate = startDate,
                    endDate = endDate,
                    reason = reason,
                    status = "Approved"
                )
            )
            _snackbarMessage.value = "Leave applied for $startDate to $endDate"
        }
    }

    fun updateLeaveStatus(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(id, status)
            _snackbarMessage.value = "Leave status updated to $status"
        }
    }

    fun saveManualAttendance(
        employeeId: String,
        date: String,
        inTime: String?,
        outTime: String?,
        status: AttendanceStatus,
        workHours: Double,
        notes: String
    ) {
        viewModelScope.launch {
            val emp = employees.value.find { it.id == employeeId }
            val overtime = if (workHours > 8.0) ((workHours - 8.0) * 10).toLong() / 10.0 else 0.0
            val record = AttendanceRecord(
                employeeId = employeeId,
                employeeName = emp?.name ?: "Employee $employeeId",
                date = date,
                inTime = inTime,
                outTime = outTime,
                status = status,
                workHours = workHours,
                overtimeHours = overtime,
                locationType = emp?.workLocation ?: "Office (HQ)",
                notes = notes,
                breakMinutes = 45
            )
            repository.saveManualAttendance(record)
            _snackbarMessage.value = "Attendance record for $date saved"
        }
    }

    fun executeApiConsole() {
        viewModelScope.launch {
            _isApiExecuting.value = true
            try {
                val result = ApiClient.executeRawTestRequest(
                    context = getApplication(),
                    method = apiMethod.value,
                    urlPath = apiEndpoint.value,
                    requestBodyJson = if (apiMethod.value == "POST") apiRequestBody.value else null
                )
                _apiExecutionResult.value = result
            } finally {
                _isApiExecuting.value = false
            }
        }
    }

    fun getCsvReportExport(): String {
        val rep = employeeReport.value ?: return "No report data available"
        return repository.generateCsvReport(rep)
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
