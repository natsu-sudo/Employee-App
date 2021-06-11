package com.coding.employeeapp.database

import androidx.room.Entity
import androidx.room.PrimaryKey


enum class Gender{
    Male,
    Female,
    Others
}

enum class Designation{
    Manager,
    Staff,
    Worker
}



@Entity(tableName = "employees")
data class Employee(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val role:Int,
    val name:String,
    val age:Int,
    val gender:Int,
    val photo:String,
    val phone:String
)