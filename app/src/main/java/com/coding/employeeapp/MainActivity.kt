package com.coding.employeeapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

 fun createFiles(context: Context, directoryPictures: String, ext: String): File? {
    val timeStamp= SimpleDateFormat("yyyyMMdd:HHmmss", Locale.getDefault()).format(Date())
    //getFileDir is for internal Storage
    val fileDir: File?=context.getExternalFilesDir(directoryPictures)
    val newFile= File(fileDir, "$timeStamp.$ext")
    newFile.createNewFile()
    return newFile
}