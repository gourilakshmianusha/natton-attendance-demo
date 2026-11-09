package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.EventCategory
import com.example.data.model.EventSeverity
import com.example.data.model.Holiday
import com.example.data.model.LeaveRequest
import com.example.data.model.SystemEvent

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String,
    val department: String,
    val shiftName: String,
    val shiftStart: String,
    val shiftEnd: String,
    val workLocation: String,
    val phone: String,
    val joinDate: String,
    val avatarColorHex: Long,
    val isActive: Boolean = true
) {
    fun toDomain(): Employee = Employee(
        id = id,
        name = name,
        email = email,
        role = role,
        department = department,
        shiftName = shiftName,
        shiftStart = shiftStart,
        shiftEnd = shiftEnd,
        workLocation = workLocation,
        phone = phone,
        joinDate = joinDate,
        avatarColorHex = avatarColorHex,
        isActive = isActive
    )

    companion object {
        fun fromDomain(employee: Employee): EmployeeEntity = EmployeeEntity(
            id = employee.id,
            name = employee.name,
            email = employee.email,
            role = employee.role,
            department = employee.department,
            shiftName = employee.shiftName,
            shiftStart = employee.shiftStart,
            shiftEnd = employee.shiftEnd,
            workLocation = employee.workLocation,
            phone = employee.phone,
            joinDate = employee.joinDate,
            avatarColorHex = employee.avatarColorHex,
            isActive = employee.isActive
        )
    }
}

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["employeeId", "date"], unique = true),
        Index(value = ["date"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val date: String,
    val inTime: String?,
    val outTime: String?,
    val status: String,
    val workHours: Double,
    val overtimeHours: Double,
    val locationType: String,
    val notes: String,
    val breakMinutes: Int,
    val latitude: Double?,
    val longitude: Double?
) {
    fun toDomain(): AttendanceRecord = AttendanceRecord(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        date = date,
        inTime = inTime,
        outTime = outTime,
        status = runCatching { AttendanceStatus.valueOf(status) }.getOrDefault(AttendanceStatus.PRESENT),
        workHours = workHours,
        overtimeHours = overtimeHours,
        locationType = locationType,
        notes = notes,
        breakMinutes = breakMinutes,
        latitude = latitude,
        longitude = longitude
    )

    companion object {
        fun fromDomain(record: AttendanceRecord): AttendanceRecordEntity = AttendanceRecordEntity(
            id = record.id,
            employeeId = record.employeeId,
            employeeName = record.employeeName,
            date = record.date,
            inTime = record.inTime,
            outTime = record.outTime,
            status = record.status.name,
            workHours = record.workHours,
            overtimeHours = record.overtimeHours,
            locationType = record.locationType,
            notes = record.notes,
            breakMinutes = record.breakMinutes,
            latitude = record.latitude,
            longitude = record.longitude
        )
    }
}

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: String,
    val type: String,
    val isMandatory: Boolean
) {
    fun toDomain(): Holiday = Holiday(
        id = id,
        name = name,
        date = date,
        type = type,
        isMandatory = isMandatory
    )

    companion object {
        fun fromDomain(holiday: Holiday): HolidayEntity = HolidayEntity(
            id = holiday.id,
            name = holiday.name,
            date = holiday.date,
            type = holiday.type,
            isMandatory = holiday.isMandatory
        )
    }
}

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: String,
    val employeeName: String,
    val leaveType: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String
) {
    fun toDomain(): LeaveRequest = LeaveRequest(
        id = id,
        employeeId = employeeId,
        employeeName = employeeName,
        leaveType = leaveType,
        startDate = startDate,
        endDate = endDate,
        reason = reason,
        status = status
    )

    companion object {
        fun fromDomain(leave: LeaveRequest): LeaveRequestEntity = LeaveRequestEntity(
            id = leave.id,
            employeeId = leave.employeeId,
            employeeName = leave.employeeName,
            leaveType = leave.leaveType,
            startDate = leave.startDate,
            endDate = leave.endDate,
            reason = leave.reason,
            status = leave.status
        )
    }
}

@Entity(tableName = "system_events")
data class SystemEventEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val timestamp: String,
    val targetDepartment: String = "All Departments",
    val author: String = "Super Admin"
) {
    fun toDomain(): SystemEvent = SystemEvent(
        id = id,
        title = title,
        description = description,
        category = try { EventCategory.valueOf(category) } catch (e: Exception) { EventCategory.SYSTEM_ALERT },
        severity = try { EventSeverity.valueOf(severity) } catch (e: Exception) { EventSeverity.INFO },
        timestamp = timestamp,
        targetDepartment = targetDepartment,
        author = author
    )

    companion object {
        fun fromDomain(event: SystemEvent): SystemEventEntity = SystemEventEntity(
            id = event.id,
            title = event.title,
            description = event.description,
            category = event.category.name,
            severity = event.severity.name,
            timestamp = event.timestamp,
            targetDepartment = event.targetDepartment,
            author = event.author
        )
    }
}

