package com.example.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface AttendanceApiService {
    @GET("api/v1/employees")
    suspend fun getEmployees(): Response<ApiResponse<List<EmployeeDto>>>

    @GET("api/v1/employees/{id}")
    suspend fun getEmployeeById(@Path("id") id: String): Response<ApiResponse<EmployeeDto>>

    @POST("api/v1/attendance/punch")
    suspend fun submitPunch(@Body request: PunchApiRequest): Response<ApiResponse<PunchApiResponse>>

    @GET("api/v1/attendance/logs")
    suspend fun getAttendanceLogs(
        @Query("date") date: String? = null,
        @Query("employee_id") employeeId: String? = null
    ): Response<ApiResponse<List<AttendanceRecordDto>>>

    @GET("api/v1/reports/employee/{id}")
    suspend fun getEmployeeReport(
        @Path("id") employeeId: String,
        @Query("month") month: String? = null
    ): Response<ApiResponse<EmployeeReportDto>>

    @GET("api/v1/holidays")
    suspend fun getHolidays(): Response<ApiResponse<List<HolidayDto>>>

    @POST("api/v1/leaves/request")
    suspend fun submitLeaveRequest(@Body request: LeaveRequestDto): Response<ApiResponse<LeaveRequestDto>>
}
