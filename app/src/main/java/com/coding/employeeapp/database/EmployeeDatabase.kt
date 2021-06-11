package com.coding.employeeapp.database

import android.content.Context
import android.os.Build
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Employee::class],version = 1)
abstract class EmployeeDatabase:RoomDatabase() {
    abstract fun employeeDetailDao():EmployeeDetailDao
    abstract fun employeeListDao():EmployeeListDao

    companion object{
        @Volatile
        private var instance:EmployeeDatabase?=null

        fun getInstance(context: Context) = instance?: synchronized(this){
            Room.databaseBuilder(context.applicationContext,EmployeeDatabase::class.java,"employee_database").build().also {
                instance=it
            }
        }
    }


}