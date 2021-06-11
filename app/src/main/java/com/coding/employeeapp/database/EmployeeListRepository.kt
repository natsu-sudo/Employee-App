package com.coding.employeeapp.database

import android.content.Context
import androidx.lifecycle.LiveData

class EmployeeListRepository(context: Context) {
    private val listRepo=EmployeeDatabase.getInstance(context).employeeListDao()

    fun getList():LiveData<List<Employee>>{
        return listRepo.getEmployeeList()
    }

    suspend fun insertEmployee(employee: List<Employee>){
        listRepo.insertListOfEmployee(employee)
    }


}