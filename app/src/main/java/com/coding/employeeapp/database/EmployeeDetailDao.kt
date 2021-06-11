package com.coding.employeeapp.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EmployeeDetailDao {

    @Query("Select * from employees where id == :id" )
    fun getEmployeeDetail(id:Long):LiveData<Employee>

    @Update
    suspend fun updateEmployee(employee: Employee)

    @Delete
    suspend fun deleteEmployee(employee:Employee)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertEmployee(employee: Employee):Long
}