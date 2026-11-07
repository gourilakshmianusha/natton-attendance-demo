package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveRequestEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SampleDataSeeder {

    suspend fun seedIfNeeded(db: AppDatabase) = withContext(Dispatchers.IO) {
        val employeeCount = db.employeeDao().getEmployeeCount()
        if (employeeCount > 0) return@withContext

        // 1. Seed Employees
        val employees = listOf(
            EmployeeEntity(
                id = "EMP-1001",
                name = "Sarah Jenkins",
                email = "sarah.jenkins@attendflow.com",
                role = "Principal Android Engineer",
                department = "Engineering",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Office (HQ - Fl 4)",
                phone = "+1 (555) 234-5678",
                joinDate = "2023-01-15",
                avatarColorHex = 0xFF2563EB,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1002",
                name = "Marcus Vance",
                email = "marcus.vance@attendflow.com",
                role = "Staff Backend Architect",
                department = "Engineering",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Remote (California)",
                phone = "+1 (555) 345-6789",
                joinDate = "2023-04-10",
                avatarColorHex = 0xFF059669,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1003",
                name = "Elena Rostova",
                email = "elena.rostova@attendflow.com",
                role = "Lead UI/UX Designer",
                department = "Design",
                shiftName = "Flex Shift (10:00 - 19:00)",
                shiftStart = "10:00",
                shiftEnd = "19:00",
                workLocation = "Office (HQ - Fl 3)",
                phone = "+1 (555) 456-7890",
                joinDate = "2023-08-01",
                avatarColorHex = 0xFF7C3AED,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1004",
                name = "David Chen",
                email = "david.chen@attendflow.com",
                role = "Director of Product",
                department = "Product",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Hybrid (Office/Home)",
                phone = "+1 (555) 567-8901",
                joinDate = "2022-11-20",
                avatarColorHex = 0xFFD97706,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1005",
                name = "Aaliyah Patel",
                email = "aaliyah.patel@attendflow.com",
                role = "People & Culture Lead",
                department = "Human Resources",
                shiftName = "General (09:30 - 18:30)",
                shiftStart = "09:30",
                shiftEnd = "18:30",
                workLocation = "Office (HQ - Fl 2)",
                phone = "+1 (555) 678-9012",
                joinDate = "2024-02-01",
                avatarColorHex = 0xFFDB2777,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1006",
                name = "Robert Taylor",
                email = "robert.taylor@attendflow.com",
                role = "Senior Financial Controller",
                department = "Finance",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Office (HQ - Fl 5)",
                phone = "+1 (555) 789-0123",
                joinDate = "2023-06-18",
                avatarColorHex = 0xFF0284C7,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1007",
                name = "Chloe Zhao",
                email = "chloe.zhao@attendflow.com",
                role = "QA Test Automation Lead",
                department = "Quality Assurance",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Office (HQ - Fl 4)",
                phone = "+1 (555) 890-1234",
                joinDate = "2024-05-12",
                avatarColorHex = 0xFF10B981,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1008",
                name = "Jordan Blake",
                email = "jordan.blake@attendflow.com",
                role = "Cloud & Security Engineer",
                department = "Engineering",
                shiftName = "Day Shift (09:00 - 18:00)",
                shiftStart = "09:00",
                shiftEnd = "18:00",
                workLocation = "Remote (Austin)",
                phone = "+1 (555) 901-2345",
                joinDate = "2024-01-08",
                avatarColorHex = 0xFF6366F1,
                isActive = true
            )
        )
        db.employeeDao().insertAllEmployees(employees)

        // 2. Seed 2026 Holidays
        val holidays = listOf(
            HolidayEntity(name = "New Year's Day", date = "2026-01-01", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Martin Luther King Jr. Day", date = "2026-01-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Presidents' Day", date = "2026-02-16", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Memorial Day", date = "2026-05-25", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Juneteenth National Day", date = "2026-06-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Independence Day", date = "2026-07-04", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Labor Day", date = "2026-09-07", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Indigenous Peoples' Day", date = "2026-10-12", type = "Company Observance", isMandatory = false),
            HolidayEntity(name = "Veterans Day", date = "2026-11-11", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Thanksgiving Day", date = "2026-11-26", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Day After Thanksgiving", date = "2026-11-27", type = "Company Holiday", isMandatory = true),
            HolidayEntity(name = "Christmas Eve", date = "2026-12-24", type = "Half Day Observance", isMandatory = false),
            HolidayEntity(name = "Christmas Day", date = "2026-12-25", type = "Public Holiday", isMandatory = true)
        )
        db.holidayDao().insertAllHolidays(holidays)

        // 3. Seed Leaves
        val leaves = listOf(
            LeaveRequestEntity(
                employeeId = "EMP-1001",
                employeeName = "Sarah Jenkins",
                leaveType = "Vacation",
                startDate = "2026-11-16",
                endDate = "2026-11-18",
                reason = "Family annual reunion",
                status = "Approved"
            ),
            LeaveRequestEntity(
                employeeId = "EMP-1003",
                employeeName = "Elena Rostova",
                leaveType = "Sick Leave",
                startDate = "2026-11-04",
                endDate = "2026-11-04",
                reason = "Dental recovery procedure",
                status = "Approved"
            ),
            LeaveRequestEntity(
                employeeId = "EMP-1004",
                employeeName = "David Chen",
                leaveType = "Casual Leave",
                startDate = "2026-11-20",
                endDate = "2026-11-21",
                reason = "Personal administrative errands",
                status = "Pending"
            )
        )
        db.leaveDao().insertAllLeaves(leaves)

        // 4. Seed Historical Attendance Records for previous 30 days
        val attendanceList = mutableListOf<AttendanceRecordEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        // Today is 2026-11-07. Let's seed the last 25 days
        for (i in 0..25) {
            val dayCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dateFormat.format(dayCal.time)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)

            // Skip Sunday (1) and Saturday (7) for normal working attendance
            val isWeekend = (dayOfWeek == Calendar.SUNDAY || dayOfWeek == Calendar.SATURDAY)

            for (emp in employees) {
                if (isWeekend) continue

                // Check if this date has a holiday
                val holiday = holidays.find { it.date == dateStr }
                if (holiday != null) {
                    attendanceList.add(
                        AttendanceRecordEntity(
                            employeeId = emp.id,
                            employeeName = emp.name,
                            date = dateStr,
                            inTime = null,
                            outTime = null,
                            status = "HOLIDAY",
                            workHours = 0.0,
                            overtimeHours = 0.0,
                            locationType = "Holiday: ${holiday.name}",
                            notes = "Official Paid Holiday",
                            breakMinutes = 0,
                            latitude = null,
                            longitude = null
                        )
                    )
                    continue
                }

                // Check if leave applies
                if (emp.id == "EMP-1003" && dateStr == "2026-11-04") {
                    attendanceList.add(
                        AttendanceRecordEntity(
                            employeeId = emp.id,
                            employeeName = emp.name,
                            date = dateStr,
                            inTime = null,
                            outTime = null,
                            status = "ON_LEAVE",
                            workHours = 0.0,
                            overtimeHours = 0.0,
                            locationType = "Leave",
                            notes = "Approved Sick Leave",
                            breakMinutes = 0,
                            latitude = null,
                            longitude = null
                        )
                    )
                    continue
                }

                // Generate realistic in-time and out-time based on employee shift
                val randomSeed = (emp.id.hashCode() + i * 37).let { if (it < 0) -it else it }
                val variation = randomSeed % 10

                val (inTime, outTime, status, hours, ot) = when {
                    variation == 8 -> {
                        // Late arrival
                        val inT = "09:34:12"
                        val outT = "18:45:00"
                        Tuple5(inT, outT, "LATE", 8.4, 0.4)
                    }
                    variation == 9 && i > 5 -> {
                        // Half day or absent
                        if (randomSeed % 2 == 0) {
                            Tuple5("09:05:00", "13:08:00", "HALF_DAY", 4.0, 0.0)
                        } else {
                            Tuple5(null, null, "ABSENT", 0.0, 0.0)
                        }
                    }
                    else -> {
                        // On time present
                        val inMin = 50 + (randomSeed % 15) // 08:50 to 09:05
                        val inStr = if (inMin >= 60) "09:0${inMin - 60}:18" else "08:$inMin:22"
                        val outMin = 10 + (randomSeed % 35)
                        val outStr = "18:$outMin:45"
                        val workH = 8.0 + ((outMin) / 60.0)
                        val otH = if (workH > 8.0) ((workH - 8.0) * 10).toLong() / 10.0 else 0.0
                        Tuple5(inStr, outStr, "PRESENT", ((workH * 10).toLong() / 10.0), otH)
                    }
                }

                attendanceList.add(
                    AttendanceRecordEntity(
                        employeeId = emp.id,
                        employeeName = emp.name,
                        date = dateStr,
                        inTime = inTime,
                        outTime = outTime,
                        status = status,
                        workHours = hours,
                        overtimeHours = ot,
                        locationType = emp.workLocation,
                        notes = if (status == "LATE") "Traffic congestion on freeway" else if (status == "HALF_DAY") "Doctor appointment in afternoon" else "Normal work day",
                        breakMinutes = if (hours > 5.0) 45 else 0,
                        latitude = 37.7749,
                        longitude = -122.4194
                    )
                )
            }
        }

        db.attendanceDao().insertAllAttendance(attendanceList)
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
