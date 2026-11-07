package com.example.data.model

enum class AttendanceStatus(val label: String, val colorHex: Long) {
    PRESENT("Present", 0xFF10B981),      // Emerald Green
    LATE("Late Arrival", 0xFFF59E0B),    // Amber
    HALF_DAY("Half Day", 0xFFF97316),    // Orange
    ABSENT("Absent", 0xFFEF4444),        // Red
    HOLIDAY("Holiday", 0xFF3B82F6),      // Sky Blue
    ON_LEAVE("On Leave", 0xFF8B5CF6)     // Purple
}

enum class PunchType(val title: String) {
    IN("Punch In"),
    OUT("Punch Out"),
    BREAK_START("Start Break"),
    BREAK_END("End Break")
}

data class Employee(
    val id: String,
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val shiftName: String = "Day Shift (09:00 - 18:00)",
    val shiftStart: String = "09:00",
    val shiftEnd: String = "18:00",
    val workLocation: String = "Office (HQ)",
    val phone: String = "+1 (555) 019-2834",
    val joinDate: String = "2024-03-15",
    val avatarColorHex: Long = 0xFF3B82F6,
    val isActive: Boolean = true
)

data class AttendanceRecord(
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String,                  // YYYY-MM-DD
    val inTime: String? = null,        // HH:mm:ss
    val outTime: String? = null,       // HH:mm:ss
    val status: AttendanceStatus = AttendanceStatus.PRESENT,
    val workHours: Double = 0.0,
    val overtimeHours: Double = 0.0,
    val locationType: String = "Office (HQ)",
    val notes: String = "",
    val breakMinutes: Int = 0,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class Holiday(
    val id: Long = 0,
    val name: String,
    val date: String,                  // YYYY-MM-DD
    val type: String = "Public Holiday",
    val isMandatory: Boolean = true
)

data class LeaveRequest(
    val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val leaveType: String,             // Sick Leave, Casual Leave, Vacation
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "Approved"    // Approved, Pending, Rejected
)

data class EmployeeAttendanceReport(
    val employee: Employee,
    val monthYear: String,
    val totalWorkingDays: Int,
    val presentDays: Int,
    val lateDays: Int,
    val halfDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val leaveDays: Int,
    val attendancePercentage: Float,
    val punctualityPercentage: Float,
    val totalWorkHours: Double,
    val totalOvertimeHours: Double,
    val averageInTime: String,
    val averageOutTime: String,
    val dailyRecords: List<AttendanceRecord>
)

data class OfficeStayResult(
    val grossHours: Double,
    val grossFormatted: String,
    val breakMinutes: Int,
    val netHours: Double,
    val netFormatted: String,
    val overtimeHours: Double,
    val isLate: Boolean,
    val lateMinutes: Int,
    val complianceText: String
)

fun calculateStayDuration(
    inTime: String?,
    outTime: String?,
    shiftStart: String = "09:00",
    breakMinutes: Int = 0
): OfficeStayResult {
    if (inTime.isNullOrEmpty() || outTime.isNullOrEmpty()) {
        return OfficeStayResult(
            grossHours = 0.0,
            grossFormatted = "0 hrs 0 mins",
            breakMinutes = breakMinutes,
            netHours = 0.0,
            netFormatted = "0 hrs 0 mins",
            overtimeHours = 0.0,
            isLate = false,
            lateMinutes = 0,
            complianceText = "Pending Punch Completion"
        )
    }

    val inSec = timeToSeconds(inTime)
    val outSec = timeToSeconds(outTime)

    val grossSec = if (outSec >= inSec) outSec - inSec else (outSec + 86400) - inSec
    val grossHours = ((grossSec / 3600.0) * 100).toLong() / 100.0

    val grossH = grossSec / 3600
    val grossM = (grossSec % 3600) / 60

    val netSec = (grossSec - (breakMinutes * 60)).coerceAtLeast(0)
    val netHours = ((netSec / 3600.0) * 10).toLong() / 10.0
    val netH = netSec / 3600
    val netM = (netSec % 3600) / 60

    val shiftStartSec = timeToSeconds(shiftStart)
    val lateMinutes = if (inSec > (shiftStartSec + 900)) ((inSec - shiftStartSec) / 60).toInt() else 0
    val isLate = lateMinutes > 0

    val overtimeHours = if (netHours > 8.0) ((netHours - 8.0) * 10).toLong() / 10.0 else 0.0

    val complianceText = when {
        netHours >= 8.0 && overtimeHours > 0 -> "Met 8h quota (+${overtimeHours}h OT)"
        netHours >= 8.0 -> "Met 8.0h Standard Shift Quota"
        netHours in 4.0..7.9 -> "Half Day / Partial Shift (${8.0 - netHours}h shortfall)"
        else -> "Under minimum shift duration"
    }

    return OfficeStayResult(
        grossHours = grossHours,
        grossFormatted = "${grossH} hrs ${grossM} mins",
        breakMinutes = breakMinutes,
        netHours = netHours,
        netFormatted = "${netH} hrs ${netM} mins",
        overtimeHours = overtimeHours,
        isLate = isLate,
        lateMinutes = lateMinutes,
        complianceText = complianceText
    )
}

private fun timeToSeconds(timeStr: String): Long {
    val clean = timeStr.trim().substringBefore(" ")
    val parts = clean.split(":")
    val h = parts.getOrNull(0)?.toLongOrNull() ?: 0L
    val m = parts.getOrNull(1)?.toLongOrNull() ?: 0L
    val s = parts.getOrNull(2)?.toLongOrNull() ?: 0L
    return h * 3600 + m * 60 + s
}

