package com.example.data.api

import android.content.Context
import com.example.data.local.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MockApiInterceptor(private val context: Context) : Interceptor {

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url
        val path = url.encodedPath
        val method = request.method

        // If request is directed to external live URL other than attendflow mock domain, proceed normally
        if (!url.host.contains("attendflow") && !url.host.contains("localhost") && !url.host.contains("10.0.2.2")) {
            return chain.proceed(request)
        }

        val db = AppDatabase.getDatabase(context)
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).format(Date())

        val responseJson = when {
            // GET /api/v1/employees
            method == "GET" && path == "/api/v1/employees" -> {
                val employees = runBlocking { db.employeeDao().getAllEmployees().first() }
                val jsonArray = JSONArray()
                employees.forEach { emp ->
                    val obj = JSONObject().apply {
                        put("id", emp.id)
                        put("name", emp.name)
                        put("email", emp.email)
                        put("role", emp.role)
                        put("department", emp.department)
                        put("shift_name", emp.shiftName)
                        put("shift_start", emp.shiftStart)
                        put("shift_end", emp.shiftEnd)
                        put("work_location", emp.workLocation)
                        put("phone", emp.phone)
                        put("is_active", emp.isActive)
                    }
                    jsonArray.put(obj)
                }
                JSONObject().apply {
                    put("success", true)
                    put("data", jsonArray)
                    put("message", "Fetched ${employees.size} employees successfully")
                    put("timestamp", now)
                }
            }

            // GET /api/v1/employees/{id}
            method == "GET" && path.startsWith("/api/v1/employees/") -> {
                val empId = path.substringAfterLast("/")
                val employee = runBlocking { db.employeeDao().getEmployeeByIdOnce(empId) }
                if (employee != null) {
                    val empObj = JSONObject().apply {
                        put("id", employee.id)
                        put("name", employee.name)
                        put("email", employee.email)
                        put("role", employee.role)
                        put("department", employee.department)
                        put("shift_name", employee.shiftName)
                        put("shift_start", employee.shiftStart)
                        put("shift_end", employee.shiftEnd)
                        put("work_location", employee.workLocation)
                        put("phone", employee.phone)
                        put("is_active", employee.isActive)
                    }
                    JSONObject().apply {
                        put("success", true)
                        put("data", empObj)
                        put("message", "Employee details found")
                        put("timestamp", now)
                    }
                } else {
                    JSONObject().apply {
                        put("success", false)
                        put("message", "Employee with ID $empId not found")
                        put("timestamp", now)
                    }
                }
            }

            // POST /api/v1/attendance/punch
            method == "POST" && path == "/api/v1/attendance/punch" -> {
                val bodyString = runCatching {
                    val buffer = okio.Buffer()
                    request.body?.writeTo(buffer)
                    buffer.readUtf8()
                }.getOrDefault("{}")

                val reqJson = JSONObject(bodyString)
                val employeeId = reqJson.optString("employee_id", "EMP-1001")
                val punchType = reqJson.optString("punch_type", "IN")
                val location = reqJson.optString("location", "Office (HQ)")
                val notes = reqJson.optString("notes", "")

                val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val timeNow = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())

                val emp = runBlocking { db.employeeDao().getEmployeeByIdOnce(employeeId) }
                val empName = emp?.name ?: "Employee $employeeId"

                val existing = runBlocking { db.attendanceDao().getAttendanceForEmployeeAndDate(employeeId, todayDate) }

                val inTime = if (punchType == "IN" || existing == null) timeNow else existing.inTime ?: timeNow
                val outTime = if (punchType == "OUT") timeNow else existing?.outTime

                var workHours = existing?.workHours ?: 0.0
                if (inTime != null && outTime != null) {
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                    val inDate = timeFormat.parse(inTime)
                    val outDate = timeFormat.parse(outTime)
                    if (inDate != null && outDate != null && outDate.time > inDate.time) {
                        workHours = ((outDate.time - inDate.time) / (1000.0 * 3600.0) * 10).toLong() / 10.0
                    }
                }

                val status = if (punchType == "IN") {
                    val shiftStart = emp?.shiftStart ?: "09:00"
                    val hourMin = timeNow.substring(0, 5)
                    if (hourMin > shiftStart && hourMin > "09:15") "LATE" else "PRESENT"
                } else existing?.status ?: "PRESENT"

                val updatedEntity = com.example.data.local.AttendanceRecordEntity(
                    id = existing?.id ?: 0L,
                    employeeId = employeeId,
                    employeeName = empName,
                    date = todayDate,
                    inTime = inTime,
                    outTime = outTime,
                    status = status,
                    workHours = workHours,
                    overtimeHours = if (workHours > 8.0) ((workHours - 8.0) * 10).toLong() / 10.0 else 0.0,
                    locationType = location,
                    notes = notes,
                    breakMinutes = existing?.breakMinutes ?: 0,
                    latitude = null,
                    longitude = null
                )

                runBlocking { db.attendanceDao().insertAttendance(updatedEntity) }

                val punchData = JSONObject().apply {
                    put("success", true)
                    put("message", "Punch $punchType recorded successfully at $timeNow")
                    put("employee_id", employeeId)
                    put("punch_type", punchType)
                    put("date", todayDate)
                    put("in_time", inTime)
                    put("out_time", outTime)
                    put("total_hours_today", workHours)
                    put("status", status)
                }

                JSONObject().apply {
                    put("success", true)
                    put("data", punchData)
                    put("message", "Attendance registered")
                    put("timestamp", now)
                }
            }

            // GET /api/v1/attendance/logs
            method == "GET" && path == "/api/v1/attendance/logs" -> {
                val queryDate = url.queryParameter("date")
                val records = runBlocking {
                    if (!queryDate.isNullOrEmpty()) {
                        db.attendanceDao().getAttendanceForDate(queryDate).first()
                    } else {
                        db.attendanceDao().getAllAttendanceRecords().first()
                    }
                }
                val arr = JSONArray()
                records.forEach { rec ->
                    arr.put(JSONObject().apply {
                        put("id", rec.id)
                        put("employee_id", rec.employeeId)
                        put("employee_name", rec.employeeName)
                        put("date", rec.date)
                        put("in_time", rec.inTime)
                        put("out_time", rec.outTime)
                        put("status", rec.status)
                        put("work_hours", rec.workHours)
                        put("overtime_hours", rec.overtimeHours)
                        put("location_type", rec.locationType)
                        put("notes", rec.notes)
                    })
                }
                JSONObject().apply {
                    put("success", true)
                    put("data", arr)
                    put("message", "Fetched ${records.size} attendance records")
                    put("timestamp", now)
                }
            }

            // GET /api/v1/reports/employee/{id}
            method == "GET" && path.startsWith("/api/v1/reports/employee/") -> {
                val empId = path.substringAfterLast("/")
                val month = url.queryParameter("month") ?: SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
                val emp = runBlocking { db.employeeDao().getEmployeeByIdOnce(empId) }
                val records = runBlocking { db.attendanceDao().getAttendanceForEmployeeAndMonth(empId, month).first() }

                val presentCount = records.count { it.status == "PRESENT" }
                val lateCount = records.count { it.status == "LATE" }
                val halfDayCount = records.count { it.status == "HALF_DAY" }
                val absentCount = records.count { it.status == "ABSENT" }
                val holidayCount = records.count { it.status == "HOLIDAY" }
                val leaveCount = records.count { it.status == "ON_LEAVE" }
                val totalHours = records.sumOf { it.workHours }
                val totalOt = records.sumOf { it.overtimeHours }

                val workingDays = 22
                val effectivePresent = presentCount + lateCount + (halfDayCount * 0.5)
                val attPercent = if (workingDays > 0) ((effectivePresent / workingDays.toDouble()) * 100.0).toFloat() else 0f
                val totalAttended = presentCount + lateCount + halfDayCount
                val punctPercent = if (totalAttended > 0) ((presentCount.toFloat() / totalAttended.toFloat()) * 100f) else 100f

                val inTimes = records.mapNotNull { it.inTime }
                val outTimes = records.mapNotNull { it.outTime }
                val avgIn = if (inTimes.isNotEmpty()) inTimes.first() else "09:00:00"
                val avgOut = if (outTimes.isNotEmpty()) outTimes.last() else "18:00:00"

                val reportObj = JSONObject().apply {
                    put("employee_id", empId)
                    put("employee_name", emp?.name ?: "Employee $empId")
                    put("department", emp?.department ?: "General")
                    put("month_year", month)
                    put("total_working_days", workingDays)
                    put("present_days", presentCount)
                    put("late_days", lateCount)
                    put("half_days", halfDayCount)
                    put("absent_days", absentCount)
                    put("holiday_days", holidayCount)
                    put("leave_days", leaveCount)
                    put("attendance_percentage", attPercent)
                    put("punctuality_percentage", punctPercent)
                    put("total_work_hours", totalHours)
                    put("total_overtime_hours", totalOt)
                    put("average_in_time", avgIn)
                    put("average_out_time", avgOut)
                    put("records_count", records.size)
                }

                JSONObject().apply {
                    put("success", true)
                    put("data", reportObj)
                    put("message", "Generated monthly attendance report")
                    put("timestamp", now)
                }
            }

            // GET /api/v1/holidays
            method == "GET" && path == "/api/v1/holidays" -> {
                val holidays = runBlocking { db.holidayDao().getAllHolidays().first() }
                val arr = JSONArray()
                holidays.forEach { h ->
                    arr.put(JSONObject().apply {
                        put("id", h.id)
                        put("name", h.name)
                        put("date", h.date)
                        put("type", h.type)
                        put("is_mandatory", h.isMandatory)
                    })
                }
                JSONObject().apply {
                    put("success", true)
                    put("data", arr)
                    put("message", "Fetched ${holidays.size} holidays")
                    put("timestamp", now)
                }
            }

            else -> {
                JSONObject().apply {
                    put("success", false)
                    put("message", "Endpoint not found: $method $path")
                    put("timestamp", now)
                }
            }
        }

        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseJson.toString().toResponseBody(jsonMediaType))
            .addHeader("Content-Type", "application/json; charset=utf-8")
            .addHeader("X-NattonTech-Version", "1.0.0")
            .build()
    }
}
