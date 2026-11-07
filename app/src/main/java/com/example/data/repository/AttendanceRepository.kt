package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveRequestEntity
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.EmployeeAttendanceReport
import com.example.data.model.Holiday
import com.example.data.model.LeaveRequest
import com.example.data.model.PunchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AttendanceRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val employeeDao = db.employeeDao()
    private val attendanceDao = db.attendanceDao()
    private val holidayDao = db.holidayDao()
    private val leaveDao = db.leaveDao()

    suspend fun initializeData() {
        SampleDataSeeder.seedIfNeeded(db)
    }

    val employees: Flow<List<Employee>> = employeeDao.getAllEmployees().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val allAttendance: Flow<List<AttendanceRecord>> = attendanceDao.getAllAttendanceRecords().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val holidays: Flow<List<Holiday>> = holidayDao.getAllHolidays().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    val leaves: Flow<List<LeaveRequest>> = leaveDao.getAllLeaves().map { list ->
        list.map { it.toDomain() }
    }.flowOn(Dispatchers.IO)

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(date).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)

    fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForEmployee(employeeId).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)

    fun getEmployeeById(id: String): Flow<Employee?> =
        employeeDao.getEmployeeById(id).map { it?.toDomain() }.flowOn(Dispatchers.IO)

    suspend fun punch(
        employeeId: String,
        punchType: PunchType,
        location: String,
        notes: String = ""
    ): AttendanceRecord = withContext(Dispatchers.IO) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val timeNow = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        val emp = employeeDao.getEmployeeByIdOnce(employeeId)?.toDomain()
            ?: throw IllegalArgumentException("Employee $employeeId not found")

        val existing = attendanceDao.getAttendanceForEmployeeAndDate(employeeId, today)

        val inTime: String?
        val outTime: String?
        var status = AttendanceStatus.PRESENT
        var workHours = existing?.workHours ?: 0.0
        var breakMinutes = existing?.breakMinutes ?: 0

        when (punchType) {
            PunchType.IN -> {
                inTime = timeNow
                outTime = existing?.outTime
                // Determine if late
                val shiftStart = emp.shiftStart
                val hourMin = timeNow.substring(0, 5)
                status = if (hourMin > shiftStart && hourMin > "09:15") AttendanceStatus.LATE else AttendanceStatus.PRESENT
            }
            PunchType.OUT -> {
                inTime = existing?.inTime ?: "09:00:00"
                outTime = timeNow
                status = existing?.let {
                    runCatching { AttendanceStatus.valueOf(it.status) }.getOrDefault(AttendanceStatus.PRESENT)
                } ?: AttendanceStatus.PRESENT

                // Calculate work hours
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                val inDate = timeFormat.parse(inTime)
                val outDate = timeFormat.parse(outTime)
                if (inDate != null && outDate != null && outDate.time > inDate.time) {
                    val rawHours = (outDate.time - inDate.time) / (1000.0 * 3600.0)
                    val netHours = (rawHours - (breakMinutes / 60.0)).coerceAtLeast(0.0)
                    workHours = ((netHours * 10).toLong()) / 10.0
                }
            }
            PunchType.BREAK_START -> {
                inTime = existing?.inTime ?: timeNow
                outTime = existing?.outTime
                status = existing?.let {
                    runCatching { AttendanceStatus.valueOf(it.status) }.getOrDefault(AttendanceStatus.PRESENT)
                } ?: AttendanceStatus.PRESENT
            }
            PunchType.BREAK_END -> {
                inTime = existing?.inTime ?: "09:00:00"
                outTime = existing?.outTime
                breakMinutes += 30
                status = existing?.let {
                    runCatching { AttendanceStatus.valueOf(it.status) }.getOrDefault(AttendanceStatus.PRESENT)
                } ?: AttendanceStatus.PRESENT
            }
        }

        val overtime = if (workHours > 8.0) ((workHours - 8.0) * 10).toLong() / 10.0 else 0.0

        val recordEntity = AttendanceRecordEntity(
            id = existing?.id ?: 0L,
            employeeId = employeeId,
            employeeName = emp.name,
            date = today,
            inTime = inTime,
            outTime = outTime,
            status = status.name,
            workHours = workHours,
            overtimeHours = overtime,
            locationType = location,
            notes = if (notes.isNotEmpty()) notes else (existing?.notes ?: ""),
            breakMinutes = breakMinutes,
            latitude = 37.7749,
            longitude = -122.4194
        )

        val id = attendanceDao.insertAttendance(recordEntity)
        recordEntity.copy(id = if (existing != null) existing.id else id).toDomain()
    }

    fun generateEmployeeReport(
        employeeId: String,
        monthPrefix: String
    ): Flow<EmployeeAttendanceReport?> = combine(
        employeeDao.getEmployeeById(employeeId),
        attendanceDao.getAttendanceForEmployeeAndMonth(employeeId, monthPrefix)
    ) { empEntity, recordsEntities ->
        if (empEntity == null) return@combine null

        val employee = empEntity.toDomain()
        val records = recordsEntities.map { it.toDomain() }

        val presentDays = records.count { it.status == AttendanceStatus.PRESENT }
        val lateDays = records.count { it.status == AttendanceStatus.LATE }
        val halfDays = records.count { it.status == AttendanceStatus.HALF_DAY }
        val absentDays = records.count { it.status == AttendanceStatus.ABSENT }
        val holidayDays = records.count { it.status == AttendanceStatus.HOLIDAY }
        val leaveDays = records.count { it.status == AttendanceStatus.ON_LEAVE }

        val totalWorkingDays = 22 // Standard monthly business days
        val effectiveAttendance = presentDays + lateDays + (halfDays * 0.5)
        val attendancePct = if (totalWorkingDays > 0) {
            ((effectiveAttendance / totalWorkingDays.toDouble()) * 100.0).toFloat().coerceIn(0f, 100f)
        } else 0f

        val totalAttended = presentDays + lateDays + halfDays
        val punctualityPct = if (totalAttended > 0) {
            ((presentDays.toFloat() / totalAttended.toFloat()) * 100f).coerceIn(0f, 100f)
        } else 100f

        val totalWorkHours = ((records.sumOf { it.workHours } * 10).toLong()) / 10.0
        val totalOvertime = ((records.sumOf { it.overtimeHours } * 10).toLong()) / 10.0

        val inTimes = records.mapNotNull { it.inTime }
        val outTimes = records.mapNotNull { it.outTime }

        val avgIn = if (inTimes.isNotEmpty()) {
            val totalSeconds = inTimes.mapNotNull { timeStrToSeconds(it) }.average().toLong()
            secondsToTimeStr(totalSeconds)
        } else "--:--"

        val avgOut = if (outTimes.isNotEmpty()) {
            val totalSeconds = outTimes.mapNotNull { timeStrToSeconds(it) }.average().toLong()
            secondsToTimeStr(totalSeconds)
        } else "--:--"

        EmployeeAttendanceReport(
            employee = employee,
            monthYear = monthPrefix,
            totalWorkingDays = totalWorkingDays,
            presentDays = presentDays,
            lateDays = lateDays,
            halfDays = halfDays,
            absentDays = absentDays,
            holidayDays = holidayDays,
            leaveDays = leaveDays,
            attendancePercentage = attendancePct,
            punctualityPercentage = punctualityPct,
            totalWorkHours = totalWorkHours,
            totalOvertimeHours = totalOvertime,
            averageInTime = avgIn,
            averageOutTime = avgOut,
            dailyRecords = records
        )
    }.flowOn(Dispatchers.IO)

    fun generateCsvReport(report: EmployeeAttendanceReport): String {
        val sb = StringBuilder()
        sb.appendLine("ATTENDFLOW EMPLOYEE ATTENDANCE REPORT")
        sb.appendLine("Employee Name,${report.employee.name}")
        sb.appendLine("Employee ID,${report.employee.id}")
        sb.appendLine("Department,${report.employee.department}")
        sb.appendLine("Role,${report.employee.role}")
        sb.appendLine("Report Period,${report.monthYear}")
        sb.appendLine("Attendance %,${String.format(Locale.US, "%.1f%%", report.attendancePercentage)}")
        sb.appendLine("Punctuality %,${String.format(Locale.US, "%.1f%%", report.punctualityPercentage)}")
        sb.appendLine("Total Days Present,${report.presentDays}")
        sb.appendLine("Late Arrivals,${report.lateDays}")
        sb.appendLine("Half Days,${report.halfDays}")
        sb.appendLine("Absences,${report.absentDays}")
        sb.appendLine("Paid Holidays,${report.holidayDays}")
        sb.appendLine("Approved Leaves,${report.leaveDays}")
        sb.appendLine("Total Work Hours,${report.totalWorkHours} hrs")
        sb.appendLine("Total Overtime,${report.totalOvertimeHours} hrs")
        sb.appendLine("Avg In Time,${report.averageInTime}")
        sb.appendLine("Avg Out Time,${report.averageOutTime}")
        sb.appendLine()
        sb.appendLine("Date,In Time,Out Time,Status,Work Hours,Overtime,Location,Notes")
        report.dailyRecords.forEach { r ->
            sb.appendLine("${r.date},${r.inTime ?: "-"},${r.outTime ?: "-"},${r.status.label},${r.workHours},${r.overtimeHours},\"${r.locationType}\",\"${r.notes}\"")
        }
        return sb.toString()
    }

    suspend fun addEmployee(employee: Employee) = withContext(Dispatchers.IO) {
        employeeDao.insertEmployee(EmployeeEntity.fromDomain(employee))
    }

    suspend fun updateEmployee(employee: Employee) = withContext(Dispatchers.IO) {
        employeeDao.updateEmployee(EmployeeEntity.fromDomain(employee))
    }

    suspend fun deleteEmployee(employeeId: String) = withContext(Dispatchers.IO) {
        employeeDao.deleteEmployee(employeeId)
    }

    suspend fun addHoliday(holiday: Holiday) = withContext(Dispatchers.IO) {
        holidayDao.insertHoliday(HolidayEntity.fromDomain(holiday))
    }

    suspend fun deleteHoliday(id: Long) = withContext(Dispatchers.IO) {
        holidayDao.deleteHoliday(id)
    }

    suspend fun submitLeaveRequest(leave: LeaveRequest) = withContext(Dispatchers.IO) {
        leaveDao.insertLeave(LeaveRequestEntity.fromDomain(leave))
    }

    suspend fun updateLeaveStatus(id: Long, status: String) = withContext(Dispatchers.IO) {
        leaveDao.updateLeaveStatus(id, status)
    }

    suspend fun saveManualAttendance(record: AttendanceRecord) = withContext(Dispatchers.IO) {
        attendanceDao.insertAttendance(AttendanceRecordEntity.fromDomain(record))
    }

    private fun timeStrToSeconds(timeStr: String): Long? {
        val parts = timeStr.split(":")
        if (parts.size < 2) return null
        val h = parts[0].toLongOrNull() ?: 0L
        val m = parts[1].toLongOrNull() ?: 0L
        val s = if (parts.size >= 3) parts[2].toLongOrNull() ?: 0L else 0L
        return h * 3600 + m * 60 + s
    }

    private fun secondsToTimeStr(seconds: Long): String {
        val h = (seconds / 3600) % 24
        val m = (seconds % 3600) / 60
        val isPm = h >= 12
        val displayH = when {
            h == 0L -> 12
            h > 12 -> h - 12
            else -> h
        }
        return String.format(Locale.US, "%02d:%02d %s", displayH, m, if (isPm) "PM" else "AM")
    }
}
