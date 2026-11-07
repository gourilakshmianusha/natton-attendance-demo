package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: T? = null,
    @Json(name = "message") val message: String? = null,
    @Json(name = "timestamp") val timestamp: String = ""
)

@JsonClass(generateAdapter = true)
data class PunchApiRequest(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "punch_type") val punchType: String,      // IN, OUT, BREAK_START, BREAK_END
    @Json(name = "location") val location: String = "Office (HQ)",
    @Json(name = "notes") val notes: String = "",
    @Json(name = "timestamp") val timestamp: String? = null,
    @Json(name = "latitude") val latitude: Double? = null,
    @Json(name = "longitude") val longitude: Double? = null
)

@JsonClass(generateAdapter = true)
data class PunchApiResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "punch_type") val punchType: String,
    @Json(name = "date") val date: String,
    @Json(name = "in_time") val inTime: String?,
    @Json(name = "out_time") val outTime: String?,
    @Json(name = "total_hours_today") val totalHoursToday: Double,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class EmployeeDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "role") val role: String,
    @Json(name = "department") val department: String,
    @Json(name = "shift_name") val shiftName: String,
    @Json(name = "shift_start") val shiftStart: String,
    @Json(name = "shift_end") val shiftEnd: String,
    @Json(name = "work_location") val workLocation: String,
    @Json(name = "phone") val phone: String,
    @Json(name = "is_active") val isActive: Boolean = true
)

@JsonClass(generateAdapter = true)
data class AttendanceRecordDto(
    @Json(name = "id") val id: Long,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "employee_name") val employeeName: String,
    @Json(name = "date") val date: String,
    @Json(name = "in_time") val inTime: String?,
    @Json(name = "out_time") val outTime: String?,
    @Json(name = "status") val status: String,
    @Json(name = "work_hours") val workHours: Double,
    @Json(name = "overtime_hours") val overtimeHours: Double,
    @Json(name = "location_type") val locationType: String,
    @Json(name = "notes") val notes: String
)

@JsonClass(generateAdapter = true)
data class EmployeeReportDto(
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "employee_name") val employeeName: String,
    @Json(name = "department") val department: String,
    @Json(name = "month_year") val monthYear: String,
    @Json(name = "total_working_days") val totalWorkingDays: Int,
    @Json(name = "present_days") val presentDays: Int,
    @Json(name = "late_days") val lateDays: Int,
    @Json(name = "half_days") val halfDays: Int,
    @Json(name = "absent_days") val absentDays: Int,
    @Json(name = "holiday_days") val holidayDays: Int,
    @Json(name = "leave_days") val leaveDays: Int,
    @Json(name = "attendance_percentage") val attendancePercentage: Float,
    @Json(name = "punctuality_percentage") val punctualityPercentage: Float,
    @Json(name = "total_work_hours") val totalWorkHours: Double,
    @Json(name = "total_overtime_hours") val totalOvertimeHours: Double,
    @Json(name = "average_in_time") val averageInTime: String,
    @Json(name = "average_out_time") val averageOutTime: String,
    @Json(name = "records_count") val recordsCount: Int
)

@JsonClass(generateAdapter = true)
data class HolidayDto(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "date") val date: String,
    @Json(name = "type") val type: String,
    @Json(name = "is_mandatory") val isMandatory: Boolean
)

@JsonClass(generateAdapter = true)
data class LeaveRequestDto(
    @Json(name = "id") val id: Long = 0,
    @Json(name = "employee_id") val employeeId: String,
    @Json(name = "employee_name") val employeeName: String,
    @Json(name = "leave_type") val leaveType: String,
    @Json(name = "start_date") val startDate: String,
    @Json(name = "end_date") val endDate: String,
    @Json(name = "reason") val reason: String,
    @Json(name = "status") val status: String = "Approved"
)
