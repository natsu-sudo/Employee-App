package com.coding.employeeapp.viewmodel

import android.content.Context
import androidx.lifecycle.*
import com.coding.employeeapp.database.Employee
import com.coding.employeeapp.database.EmployeeDetailRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmployeeDetailViewModel(context: Context) :ViewModel(){

    private val detail=EmployeeDetailRepository(context)
    private var _employeeId=MutableLiveData<Long>()

    fun setEmployeeId(id:Long){
        _employeeId.value=id
    }

    val employeeDetail: LiveData<Employee> =Transformations.switchMap(_employeeId,::getDetail)

    private fun getDetail(id: Long): LiveData<Employee> {
        return detail.getEmployeeDetail(id)
    }

    fun saveEmployeeDetail(employee: Employee){
        viewModelScope.launch {
            _employeeId.value=detail.insertEmployeeDataBase(employee)
        }
    }

    fun deleteEmployeeDetail(employee: Employee){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                detail.deleteEmployee(employee)
            }
        }
    }

    fun updateEmployeeDetail(employee: Employee){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                detail.updateDataBase(employee)
            }
        }
    }

}