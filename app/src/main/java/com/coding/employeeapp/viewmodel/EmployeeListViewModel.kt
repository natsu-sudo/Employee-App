package com.coding.employeeapp.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coding.employeeapp.database.Employee
import com.coding.employeeapp.database.EmployeeListRepository
import kotlinx.coroutines.launch

class EmployeeListViewModel(context: Context):ViewModel(){
    private val listRepo=EmployeeListRepository(context)

    val getList=getEmployeeList()

     private fun getEmployeeList(): LiveData<List<Employee>> {
        return listRepo.getList()
    }

    fun insertEmployeeList(list:List<Employee>){
        viewModelScope.launch {
            listRepo.insertEmployee(list)
        }
    }
}