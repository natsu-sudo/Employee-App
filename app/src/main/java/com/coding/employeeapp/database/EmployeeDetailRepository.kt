package com.coding.employeeapp.database

import android.content.Context
import androidx.lifecycle.LiveData

class EmployeeDetailRepository(context: Context) {
    private val employeeDetailRepository=EmployeeDatabase.getInstance(context).employeeDetailDao()

    suspend fun updateDataBase(employee: Employee){
        employeeDetailRepository.updateEmployee(employee)
    }

    suspend fun insertEmployeeDataBase(employee: Employee):Long{
        return employeeDetailRepository.insertEmployee(employee)
    }

    suspend fun deleteEmployee(employee: Employee){
        employeeDetailRepository.deleteEmployee(employee)
    }

    fun getEmployeeDetail(id:Long): LiveData<Employee> {
        return employeeDetailRepository.getEmployeeDetail(id)
    }



}