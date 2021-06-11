package com.coding.employeeapp.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EmployeeListDao {

    @Query("Select * from employees order by name ")
    fun getEmployeeList():LiveData<List<Employee>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListOfEmployee(employee: List<Employee>)
}