package com.coding.employeeapp.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.coding.employeeapp.BuildConfig
import com.coding.employeeapp.R
import com.coding.employeeapp.createFiles
import com.coding.employeeapp.database.Designation
import com.coding.employeeapp.database.Employee
import com.coding.employeeapp.database.Gender
import com.coding.employeeapp.databinding.FragmentDetailBinding
import com.coding.employeeapp.viewmodel.EmployeeDetailViewModel
import com.coding.employeeapp.viewmodel.ViewModelFactory
import com.google.android.material.snackbar.Snackbar
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


private const val TAG = "FragmentDetailFragment"

class FragmentDetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var employeeDetailViewModel: EmployeeDetailViewModel
    //For simplicity this variable is here but should be moved to ViewModel
    private var selectedPhotoPath: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        employeeDetailViewModel = activity?.run {
            ViewModelProvider(
                viewModelStore,
                ViewModelFactory(this)
            )[EmployeeDetailViewModel::class.java]
        }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val employeeId = FragmentDetailFragmentArgs.fromBundle(requireArguments()).id
        employeeDetailViewModel.setEmployeeId(employeeId)
        val arrayAge = mutableListOf<Int>()
        for (age in 18 until 66) {
            arrayAge.add(age)
        }
        val arrayGenderAdapter =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, arrayAge)
        binding.employeeAge.adapter = arrayGenderAdapter
        val designation = mutableListOf<String>()
        Designation.values().forEach {
            designation.add(it.name)
        }
        binding.employeeRole.adapter =
            ArrayAdapter(view.context, android.R.layout.simple_spinner_dropdown_item, designation)
        employeeDetailViewModel.employeeDetail.observe(viewLifecycleOwner, Observer {
            it?.let { list ->
                setData(list)
            }
        })


        binding.saveEmployee.setOnClickListener {
            if (employeeId == 0L) {
                saveEmployeeDetail()
            } else {
                updateEmployeeDetail(employeeId)
            }
        }
        //reset Image
        binding.employeeImage.setOnClickListener {
            binding.employeeImage.setImageResource(R.drawable.blank_photo)
            binding.employeeImage.tag = ""

        }

        binding.capture.setOnClickListener {
            clickPhotoAfterPermission(it)
        }

        binding.uploadFromMobile.setOnClickListener {
            pickPhoto()
        }
        binding.topAppBar.setOnMenuItemClickListener {
            shareDataToOtherApp()
            true
        }


    }

    private fun shareDataToOtherApp() {
        val name = binding.employeeName.text.toString()
        val role = binding.employeeRole.selectedItem.toString()
        val age = binding.employeeAge.selectedItemPosition + 18

        val selectedStatusButton =   binding.employeeGender.findViewById<RadioButton>(binding.employeeGender.checkedRadioButtonId)
        var gender = selectedStatusButton?.text?:""

        val shareText = getString(R.string.share_text, name,role,age,gender)

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_data_title))
        startActivity(shareIntent)
    }

    private fun pickPhoto() {
        //checking for any app which we can use to select file from app
        val pickPhotoIntent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoIntent.resolveActivity(requireActivity().packageManager)?.also {
            takePictureFromFile.launch(pickPhotoIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickPhotoAfterPermission(it: View) {
        //if Permission Granted Click Photo
        if (ActivityCompat.checkSelfPermission(it.context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            clickPhoto()
        } else {//Ask for Permission
            Log.d(TAG, "clickPhotoAfterPermission: ")
            requestCameraPermission()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickPhoto() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePickuteIntent->
            takePickuteIntent.resolveActivity(requireActivity().packageManager).also {
                val photoFile: File?=try {
                    createFiles(requireContext(), Environment.DIRECTORY_PICTURES, "jpg")
                }catch (ex: IOException){
                    Toast.makeText(
                        context,
                        getString(R.string.error, ex.toString()),
                        Toast.LENGTH_SHORT
                    ).show()
                    null
                }
                photoFile?.also {
                    selectedPhotoPath=it.absolutePath
                    Log.d(TAG, "clickPhoto: " + it.absolutePath)
                    val photoUri:Uri=FileProvider.getUriForFile(
                        requireActivity(), BuildConfig.APPLICATION_ID + ".fileprovider",
                        it
                    )
                    takePickuteIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    cameraCaptureLauncher.launch(takePickuteIntent)

                }
            }
        }
    }

    private fun compressImage(absolutePath: String) {
        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                val imageFile=File(absolutePath)
                val compressedImageFile = Compressor.compress(requireActivity(), imageFile)
                val bitmap = BitmapFactory.decodeFile(compressedImageFile.path)
                if (imageFile.delete()){
                    val new=bitmapToFile(bitmap,absolutePath)
                    Log.d(TAG, "compressImage: "+new?.exists())

                }
            }
        }
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestCameraPermission() {
        requestPermissionLauncher.launch(
            Manifest.permission.CAMERA
        )
    }


    // Register the permissions callback, which handles the user's response to the
    // system permissions dialog. Save the return value, an instance of
    // ActivityResultLauncher. You can use either a val, as shown in this snippet,
    // or a lateinit var in your onAttach() or onCreate() method.
    @RequiresApi(Build.VERSION_CODES.O)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your
                // app.
                clickPhoto()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
                Log.d(TAG, "Permission Granted")
                Snackbar.make(
                    binding.capture,
                    getString(R.string.permission_msg),
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction("Ok") {

                    }
                    .show()
            }
        }

    private fun updateEmployeeDetail(employeeId: Long) {
        val roleId =
            binding.employeeGender.findViewById<RadioButton>(binding.employeeGender.checkedRadioButtonId)
        Log.d(TAG, "updateEmployeeDetail: " + roleId.text)
        var gender = -1
        when (roleId.text) {
            Gender.Others.name -> {
                gender = Gender.Others.ordinal
            }
            Gender.Female.name -> {
                gender = Gender.Female.ordinal
            }
            Gender.Male.name -> {
                gender = Gender.Male.ordinal
            }
        }
        val name = binding.employeeName.text.toString()
        val age = binding.employeeAge.selectedItemPosition+18
        val role = binding.employeeRole.selectedItemPosition
        val photo =binding.employeeImage.tag as? String?:""
        val number=binding.phoneNumber.text.toString()
        val employee = Employee(employeeId, role, name, age, gender, photo,number)
        employeeDetailViewModel.updateEmployeeDetail(employee)
        activity?.onBackPressed()
    }

    private fun saveEmployeeDetail() {
        val roleId =
            binding.employeeGender.findViewById<RadioButton>(binding.employeeGender.checkedRadioButtonId)
        var gender = -1
        Log.d(TAG, "saveEmployeeDetail: $roleId")
        when (roleId.text) {
            Gender.Others.name -> {
                gender = Gender.Others.ordinal
            }
            Gender.Female.name -> {
                gender = Gender.Female.ordinal
            }
            Gender.Male.name -> {
                gender = Gender.Male.ordinal
            }
        }
        val name = binding.employeeName.text.toString()
        val age = binding.employeeAge.selectedItemPosition + 18
        val role = binding.employeeRole.selectedItemPosition
        val photo=binding.employeeImage.tag as? String?:""
        val number=binding.phoneNumber.text.toString()
        val employee = Employee(0, role, name, age, gender, photo,number)
        employeeDetailViewModel.saveEmployeeDetail(employee)
        activity?.onBackPressed()

    }

    private fun setData(employee: Employee) {
        binding.employeeRole.setSelection(employee.role)
        binding.employeeAge.setSelection(employee.age - 18)
        binding.employeeName.setText(employee.name)
        when (employee.gender) {
            Gender.Female.ordinal -> {
                binding.genderFemale.isChecked = true
            }
            Gender.Male.ordinal -> {
                binding.genderMale.isChecked = true
            }
            Gender.Others.ordinal -> {
                binding.genderOther.isChecked = true
            }
        }

        if(employee.photo.isEmpty()){
            binding.employeeImage.setImageResource(R.drawable.blank_photo)
            binding.employeeImage.tag=""
        }else{
            binding.employeeImage.setImageURI(Uri.parse(employee.photo))
            binding.employeeImage.tag=employee.photo

        }

    }
    //Alternative OnActivityResult method is deprecated,  alternative Method
    //registerForActivityResult give you advantage to create separate on Activity result for each case
    //ie i can create capture request and upload from file
    private val cameraCaptureLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d(TAG, ": cameraCaptureLauncher " + result.resultCode)
            compressImage(selectedPhotoPath)
            val uri=Uri.fromFile(File(selectedPhotoPath))
            binding.employeeImage.setImageURI(uri)
            binding.employeeImage.tag=uri.toString()
        }
    }

    private val takePictureFromFile =registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //Here What we are doing is That the we are creating a file and after that
                //the Image which we have selected through the App we are using its uri
            result.data?.data.also { uri->
                val photo:File?=try {
                    createFiles(requireContext(), Environment.DIRECTORY_PICTURES, "jpg")
                }catch (ex: IOException){
                    Toast.makeText(activity, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    null
                }
                photo?.also {
                    try {
                        //creating resolver to resolve the uri
                        val resolver= activity?.applicationContext?.contentResolver
                        if (uri != null) {
                            //converting selected file to Stream
                            resolver?.openInputStream(uri).use { stream->
                                //copying selected file using stream

                                val output=FileOutputStream(photo)
                                stream!!.copyTo(output)
                            }
                            compressImage(photo.path)
                            val fileUri=Uri.fromFile(photo)
                            binding.employeeImage.setImageURI(fileUri)
                            binding.employeeImage.tag=fileUri.toString()
                        }
                    }catch (ex: FileNotFoundException){
                        ex.printStackTrace()
                    }catch (ex: IOException){
                        ex.printStackTrace()
                    }
                }
            }

        }
    }

    private fun bitmapToFile(bitmap: Bitmap, fileNameToSave: String): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 95, bos) // YOU can also save it in JPEG
            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            file // it will return null
        }
    }




}

