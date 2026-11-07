package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY name ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    fun getEmployeeById(id: String): Flow<EmployeeEntity?>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeByIdOnce(id: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEmployees(employees: List<EmployeeEntity>)

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE id = :id")
    suspend fun deleteEmployee(id: String)

    @Query("SELECT COUNT(*) FROM employees")
    suspend fun getEmployeeCount(): Int
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records ORDER BY date DESC, inTime DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE date = :date ORDER BY inTime ASC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getAttendanceForEmployeeAndMonth(employeeId: String, monthPrefix: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceForEmployeeAndDate(employeeId: String, date: String): AttendanceRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceRecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAttendance(records: List<AttendanceRecordEntity>)

    @Update
    suspend fun updateAttendance(record: AttendanceRecordEntity)

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteAttendance(id: Long)

    @Query("SELECT COUNT(*) FROM attendance_records")
    suspend fun getAttendanceCount(): Int
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY date ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Query("SELECT * FROM holidays WHERE date LIKE :yearPrefix || '%' ORDER BY date ASC")
    fun getHolidaysForYear(yearPrefix: String): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHolidays(holidays: List<HolidayEntity>)

    @Query("DELETE FROM holidays WHERE id = :id")
    suspend fun deleteHoliday(id: Long)

    @Query("SELECT COUNT(*) FROM holidays")
    suspend fun getHolidayCount(): Int
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leave_requests ORDER BY startDate DESC")
    fun getAllLeaves(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE employeeId = :employeeId ORDER BY startDate DESC")
    fun getLeavesForEmployee(employeeId: String): Flow<List<LeaveRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveRequestEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLeaves(leaves: List<LeaveRequestEntity>)

    @Query("UPDATE leave_requests SET status = :status WHERE id = :id")
    suspend fun updateLeaveStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM leave_requests")
    suspend fun getLeaveCount(): Int
}
