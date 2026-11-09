package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceRecordEntity
import com.example.data.local.EmployeeEntity
import com.example.data.local.HolidayEntity
import com.example.data.local.LeaveRequestEntity
import com.example.data.local.SystemEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SampleDataSeeder {

    suspend fun ensureActualHolidaysSeeded(db: AppDatabase) = withContext(Dispatchers.IO) {
        val existingHolidays = db.holidayDao().getAllHolidaysOnce()
        val existingDates = existingHolidays.map { it.date }.toSet()
        val allActualHolidays = listOf(
            HolidayEntity(name = "New Year's Day", date = "2026-01-01", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Martin Luther King Jr. Day", date = "2026-01-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Presidents' Day", date = "2026-02-16", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Memorial Day", date = "2026-05-25", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Juneteenth National Day", date = "2026-06-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Independence Day", date = "2026-07-04", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Labor Day", date = "2026-09-07", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Natton Foundation Day", date = "2026-10-15", type = "Natton Company Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-11-01", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-11-08", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Veterans Day", date = "2026-11-11", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-11-15", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-11-22", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Thanksgiving Day", date = "2026-11-26", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Day After Thanksgiving", date = "2026-11-27", type = "Company Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-11-29", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-12-06", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-12-13", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-12-20", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "Christmas Day", date = "2026-12-25", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Sunday Weekly Off", date = "2026-12-27", type = "Sunday Weekly Holiday", isMandatory = true),
            HolidayEntity(name = "New Year's Day 2027", date = "2027-01-01", type = "Public Holiday", isMandatory = true)
        )
        val missing = allActualHolidays.filter { it.date !in existingDates }
        if (missing.isNotEmpty()) {
            db.holidayDao().insertAllHolidays(missing)
        }
    }

    suspend fun ensureInitialEventsSeeded(db: AppDatabase) = withContext(Dispatchers.IO) {
        val initialEvents = listOf(
            SystemEventEntity(
                id = "EVT-001",
                title = "Natton Office Shift Protocol Active",
                description = "Automated punch timings and office stay calculator initialized for standard 09:30 AM – 06:00 PM shift.",
                category = "SYSTEM_ALERT",
                severity = "SUCCESS",
                timestamp = "2026-11-09 09:30:00",
                targetDepartment = "All Departments",
                author = "Natton System"
            ),
            SystemEventEntity(
                id = "EVT-002",
                title = "Sunday Weekly Off & Calendar Holidays Sync",
                description = "All upcoming calendar holidays and Sunday weekly offs registered in attendance matrix.",
                category = "HOLIDAY_NOTICE",
                severity = "INFO",
                timestamp = "2026-11-09 09:30:05",
                targetDepartment = "All Departments",
                author = "Super Admin"
            ),
            SystemEventEntity(
                id = "EVT-003",
                title = "Super Admin Live Telemetry Online",
                description = "Super admin console listening for automatic login arrivals, checkout departures, and broadcast events.",
                category = "ADMIN_AUDIT",
                severity = "INFO",
                timestamp = "2026-11-09 09:30:10",
                targetDepartment = "Operations",
                author = "Super Admin"
            )
        )
        db.eventDao().insertAllEvents(initialEvents)
    }

    suspend fun seedIfNeeded(context: Context, db: AppDatabase) = withContext(Dispatchers.IO) {
        ensureActualHolidaysSeeded(db)
        ensureInitialEventsSeeded(db)

        val prefs = context.getSharedPreferences("natton_tech_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("has_seeded_initial_data", false)) {
            return@withContext
        }
        val employeeCount = db.employeeDao().getEmployeeCount()
        if (employeeCount > 0) {
            prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
            return@withContext
        }

        // 1. Seed Natton Technology Employees
        val employees = listOf(
            EmployeeEntity(
                id = "EMP-1001",
                name = "Sarah Jenkins",
                email = "sarah.jenkins@natton.com",
                role = "Principal Software Engineer",
                department = "Engineering",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton HQ - Floor 4",
                phone = "+1 (555) 234-5678",
                joinDate = "2023-01-15",
                avatarColorHex = 0xFF2563EB,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1002",
                name = "Marcus Vance",
                email = "marcus.vance@natton.com",
                role = "Cloud Infrastructure Architect",
                department = "Engineering",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Remote (Natton Tech Lab)",
                phone = "+1 (555) 345-6789",
                joinDate = "2023-04-10",
                avatarColorHex = 0xFF059669,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1003",
                name = "Elena Rostova",
                email = "elena.rostova@natton.com",
                role = "Lead UI/UX Designer",
                department = "Design",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton HQ - Floor 3",
                phone = "+1 (555) 456-7890",
                joinDate = "2023-08-01",
                avatarColorHex = 0xFF7C3AED,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1004",
                name = "David Chen",
                email = "david.chen@natton.com",
                role = "Director of Technology",
                department = "Management",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton Executive Suite",
                phone = "+1 (555) 567-8901",
                joinDate = "2022-11-20",
                avatarColorHex = 0xFFD97706,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1005",
                name = "Aaliyah Patel",
                email = "aaliyah.patel@natton.com",
                role = "HR & Operations Lead",
                department = "Human Resources",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton HQ - Floor 2",
                phone = "+1 (555) 678-9012",
                joinDate = "2024-02-01",
                avatarColorHex = 0xFFDB2777,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1006",
                name = "Robert Taylor",
                email = "robert.taylor@natton.com",
                role = "Financial Controller",
                department = "Finance",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton HQ - Floor 5",
                phone = "+1 (555) 789-0123",
                joinDate = "2023-06-18",
                avatarColorHex = 0xFF0284C7,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1007",
                name = "Chloe Zhao",
                email = "chloe.zhao@natton.com",
                role = "QA Automation Lead",
                department = "Quality Assurance",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton HQ - Floor 4",
                phone = "+1 (555) 890-1234",
                joinDate = "2024-05-12",
                avatarColorHex = 0xFF10B981,
                isActive = true
            ),
            EmployeeEntity(
                id = "EMP-1008",
                name = "Jordan Blake",
                email = "jordan.blake@natton.com",
                role = "DevOps & Cyber Security",
                department = "Engineering",
                shiftName = "Natton Shift (09:30 AM - 06:00 PM)",
                shiftStart = "09:30",
                shiftEnd = "18:00",
                workLocation = "Natton Tech Lab",
                phone = "+1 (555) 901-2345",
                joinDate = "2024-01-08",
                avatarColorHex = 0xFF6366F1,
                isActive = true
            )
        )
        db.employeeDao().insertAllEmployees(employees)

        // 2. Seed Official 2026 Company Holidays & Sunday Weekly Off Policy
        val holidays = listOf(
            HolidayEntity(name = "New Year's Day", date = "2026-01-01", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Martin Luther King Jr. Day", date = "2026-01-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Presidents' Day", date = "2026-02-16", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Memorial Day", date = "2026-05-25", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Juneteenth National Day", date = "2026-06-19", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Independence Day", date = "2026-07-04", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Labor Day", date = "2026-09-07", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Natton Foundation Day", date = "2026-10-15", type = "Natton Company Holiday", isMandatory = true),
            HolidayEntity(name = "Veterans Day", date = "2026-11-11", type = "National Holiday", isMandatory = true),
            HolidayEntity(name = "Thanksgiving Day", date = "2026-11-26", type = "Public Holiday", isMandatory = true),
            HolidayEntity(name = "Day After Thanksgiving", date = "2026-11-27", type = "Company Holiday", isMandatory = true),
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
                reason = "Medical checkup",
                status = "Approved"
            )
        )
        db.leaveDao().insertAllLeaves(leaves)

        // 4. Seed Historical Attendance Records for previous 25 days with Sunday Weekly Holiday
        val attendanceList = mutableListOf<AttendanceRecordEntity>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()

        for (i in 0..25) {
            val dayCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val dateStr = dateFormat.format(dayCal.time)
            val dayOfWeek = dayCal.get(Calendar.DAY_OF_WEEK)

            // Sunday Holiday handling
            if (dayOfWeek == Calendar.SUNDAY) {
                for (emp in employees) {
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
                            locationType = "Natton Office",
                            notes = "Sunday - Official Weekly Holiday",
                            breakMinutes = 0,
                            latitude = null,
                            longitude = null
                        )
                    )
                }
                continue
            }

            // Saturday off
            if (dayOfWeek == Calendar.SATURDAY) continue

            for (emp in employees) {
                // Check if this date has a public holiday
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

                // Generate realistic in-time and out-time based on Natton 9:30 AM - 6:00 PM shift
                val randomSeed = (emp.id.hashCode() + i * 37).let { if (it < 0) -it else it }
                val variation = randomSeed % 10

                val (inTime, outTime, status, hours, ot) = when {
                    variation == 8 -> {
                        // Late arrival past 09:30 (e.g. 09:48)
                        val inT = "09:48:15"
                        val outT = "18:40:00"
                        Tuple5(inT, outT, "LATE", 8.2, 0.2)
                    }
                    variation == 9 && i > 5 -> {
                        // Half day or absent
                        if (randomSeed % 2 == 0) {
                            Tuple5("09:25:00", "13:30:00", "HALF_DAY", 4.0, 0.0)
                        } else {
                            Tuple5(null, null, "ABSENT", 0.0, 0.0)
                        }
                    }
                    else -> {
                        // On time present between 09:15 and 09:30 AM, leaving at 18:00 - 18:35 PM
                        val inMin = 15 + (randomSeed % 15) // 09:15 to 09:29 AM
                        val inStr = String.format(Locale.US, "09:%02d:12", inMin)
                        val outMin = (randomSeed % 30) // 18:00 to 18:29 PM
                        val outStr = String.format(Locale.US, "18:%02d:45", outMin)
                        val workH = 8.0 + (outMin / 60.0)
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
                        notes = if (status == "LATE") "Traffic delay (Arrived after 09:30 AM)" else "Natton Shift 09:30-18:00",
                        breakMinutes = if (hours > 5.0) 45 else 0,
                        latitude = 37.7749,
                        longitude = -122.4194
                    )
                )
            }
        }

        db.attendanceDao().insertAllAttendance(attendanceList)
        prefs.edit().putBoolean("has_seeded_initial_data", true).apply()
    }

    private data class Tuple5<A, B, C, D, E>(val a: A, val b: B, val c: C, val d: D, val e: E)
}
