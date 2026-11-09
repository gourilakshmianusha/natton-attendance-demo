package com.example

import com.example.data.api.PunchApiRequest
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Employee
import com.example.data.model.EmployeeAttendanceReport
import com.example.data.model.calculateStayDuration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val sampleEmployee = Employee(
        id = "EMP-1001",
        name = "Sarah Jenkins",
        email = "sarah.jenkins@natton.com",
        role = "Principal Android Engineer",
        department = "Engineering",
        shiftName = "Day Shift (09:30 - 18:00)",
        shiftStart = "09:30",
        shiftEnd = "18:00"
    )

    @Test
    fun testNattonOfficeStayDurationCalculation() {
        // Office hours: 09:30 to 18:00 (8h 30m gross stay, 45m break => 7h 45m net)
        val result = calculateStayDuration(
            inTime = "09:30:00",
            outTime = "18:00:00",
            shiftStart = "09:30",
            breakMinutes = 30
        )

        assertEquals("8 hrs 30 mins", result.grossFormatted)
        assertEquals(8.0, result.netHours, 0.05)
        assertEquals("8 hrs 0 mins", result.netFormatted)
        assertFalse(result.isLate)
        assertEquals("Met Natton 9:30-6:00 Shift Standard", result.complianceText)
    }

    @Test
    fun testOvertimeAndLateCalculation() {
        // Arrival past 09:40 for 09:30 shift is considered late
        val result = calculateStayDuration(
            inTime = "09:45:00",
            outTime = "19:15:00",
            shiftStart = "09:30",
            breakMinutes = 30
        )

        assertEquals("9 hrs 30 mins", result.grossFormatted)
        assertTrue(result.isLate)
        assertEquals(1.0, result.overtimeHours, 0.05)
        assertTrue(result.complianceText.contains("OT"))
    }

    @Test
    fun testAttendanceReportCalculations() {
        val dailyRecords = listOf(
            AttendanceRecord(
                id = 1L,
                employeeId = "EMP-1001",
                employeeName = "Sarah Jenkins",
                date = "2026-11-02",
                inTime = "09:25:00",
                outTime = "18:15:00",
                status = AttendanceStatus.PRESENT,
                workHours = 8.5,
                overtimeHours = 0.5
            ),
            AttendanceRecord(
                id = 2L,
                employeeId = "EMP-1001",
                employeeName = "Sarah Jenkins",
                date = "2026-11-03",
                inTime = "09:48:00",
                outTime = "18:30:00",
                status = AttendanceStatus.LATE,
                workHours = 8.2,
                overtimeHours = 0.2
            )
        )

        val report = EmployeeAttendanceReport(
            employee = sampleEmployee,
            monthYear = "2026-11",
            totalWorkingDays = 22,
            presentDays = 1,
            lateDays = 1,
            halfDays = 0,
            absentDays = 0,
            holidayDays = 4, // Includes Sunday weekly holidays
            leaveDays = 0,
            attendancePercentage = 9.1f,
            punctualityPercentage = 50.0f,
            totalWorkHours = 16.7,
            totalOvertimeHours = 0.7,
            averageInTime = "09:36 AM",
            averageOutTime = "06:22 PM",
            dailyRecords = dailyRecords
        )

        assertEquals("EMP-1001", report.employee.id)
        assertEquals(1, report.presentDays)
        assertEquals(1, report.lateDays)
        assertEquals(16.7, report.totalWorkHours, 0.01)
        assertEquals(4, report.holidayDays)
    }

    @Test
    fun testPunchApiRequestPayload() {
        val punchReq = PunchApiRequest(
            employeeId = "EMP-1001",
            punchType = "IN",
            location = "Natton HQ - Floor 4",
            notes = "Morning office arrival"
        )
        assertEquals("EMP-1001", punchReq.employeeId)
        assertEquals("IN", punchReq.punchType)
    }
}
