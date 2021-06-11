package com.coding.employeeapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coding.employeeapp.BuildConfig
import com.coding.employeeapp.R
import com.coding.employeeapp.createFiles
import com.coding.employeeapp.database.Designation
import com.coding.employeeapp.database.Employee
import com.coding.employeeapp.database.Gender
import com.coding.employeeapp.databinding.FragmentEmployeeListBinding
import com.coding.employeeapp.viewmodel.EmployeeListViewModel
import com.coding.employeeapp.viewmodel.ViewModelFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*

private const val TAG = "EmployeeListFragment"
class EmployeeListFragment : Fragment() {

    private var _binding:FragmentEmployeeListBinding?=null
    private val binding get() = _binding!!
    lateinit var listViewModel: EmployeeListViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listViewModel=activity?.run {
            ViewModelProvider(viewModelStore,ViewModelFactory(this))[EmployeeListViewModel::class.java]
        }!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding= FragmentEmployeeListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerViewAdapter=EmployeeAdapter{
            findNavController().navigate(EmployeeListFragmentDirections.actionEmployeeListFragmentToFragmentDetailFragment(it))
        }
        binding.employeeListRecyclerview.apply {
            layoutManager=LinearLayoutManager(activity)
            adapter=recyclerViewAdapter
        }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(EmployeeListFragmentDirections.actionEmployeeListFragmentToFragmentDetailFragment(0L))
        }
        listViewModel.getList.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                Log.d("TAG", "onViewCreated: $it")
                recyclerViewAdapter.submitList(it)
            }
        })
        binding.topAppBar.setOnMenuItemClickListener {menuItem ->
                when(menuItem.itemId){
                    R.id.import_file -> {
                        importFile()
                        true
                    }
                    R.id.export_file->{
//                        GlobalScope.launch {
//                            exportEmployee()
//                        }
                        exportEmployees()
                        true
                    }

                    else->false
                }
        }
    }

    private fun exportEmployees() {
        val intent=Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type="text/csv"
            putExtra(Intent.EXTRA_TIME,"employee_list_.csv")
        }
        createFileRequest.launch(intent)
    }

    //for less than Android SDK 19
    private suspend fun exportEmployee() {
        var csvFile:File?=null
        //Changing to IO its Best Practice to Use IO for Network or IO relate Work
        withContext(Dispatchers.IO){
            csvFile =try {
                //Creating a File in Documents Folder type CSV
                createFiles(requireActivity(),"Documents","csv")
            }catch (ex:IOException){
             ex.printStackTrace()
             null
            }
            //Here we are Writing in CSV File
            csvFile?.printWriter()?.use { out->
                val employees=listViewModel.getList.value
                Log.d(TAG, "exportEmployee: Size ${employees?.size}")
                if (employees != null) {
                    if (employees.isNotEmpty()){
                        employees.forEach {
                            //printing in a Single Line Comma Separated Value
                            Log.d(TAG, "exportEmployee: ${it.name+","+Designation.values()[it.role]+","+Gender.values()[it.gender]+","+it.age+","+it.phone}")
                            out.println(it.name+","+Designation.values()[it.role]+","+Gender.values()[it.gender]+","+it.age+","+it.phone)
                        }
                    }
                }
            }
            //Switching to Dispatcher.Main Because we requires Functionality android FrameWork
            withContext(Dispatchers.Main){
                csvFile?.let {
                    val uri =FileProvider.getUriForFile(requireActivity(),BuildConfig.APPLICATION_ID+".fileprovider",
                    it)
                    launchFile(uri,"csv")
                }
            }
        }
    }

    private fun launchFile(uri: Uri, ext: String) {
            val mimeType=MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext)
        val intent=Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        intent.setDataAndType(uri,mimeType)
        if (intent.resolveActivity(requireActivity().packageManager)!=null){
            startActivity(intent)
        }else{
            Toast.makeText(requireActivity(),getString(R.string.no_app),Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFile() {
        val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//            readFile->
//            readFile.addCategory(Intent.CATEGORY_OPENABLE)
//            readFile.type="text/*"
//            readFile.resolveActivity(requireActivity().packageManager).also {
//
//            }
            addCategory(Intent.CATEGORY_OPENABLE)
            type="text/csv"

        }
        intent.resolveActivity(requireActivity().packageManager)?.also {
            readFromURL.launch(intent)
        }

    }

    private val readFromURL=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if (result.resultCode==Activity.RESULT_OK){
            GlobalScope.launch {
                result.data?.data.also {
                    readFromFile(it!!)
                }
            }
        }
    }

    private suspend fun readFromFile(uri:Uri){
        try {
            requireActivity().applicationContext.contentResolver.openFileDescriptor(uri,"r").use {
               withContext(Dispatchers.IO){
                   FileInputStream(it?.fileDescriptor).use {
                       parseCSV(it)
                   }
               }
            }
        }catch (e:FileNotFoundException){
            e.printStackTrace()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    private fun parseCSV(stream: FileInputStream) {
        val employee= mutableListOf<Employee>()
        BufferedReader(InputStreamReader(stream)).readLine().forEach {
            val tokens=it.toString().split(",")
            employee.add(Employee(id=0,name = tokens[0],role = tokens[1].toInt(),gender = tokens[2].toInt(),
                age = tokens[3].toInt(),photo =tokens[4],phone = tokens[5] ))
        }
        if (employee.isNotEmpty()){
            listViewModel.insertEmployeeList(employee)
        }
    }

    private val createFileRequest=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result->
        if (result.resultCode==Activity.RESULT_OK){
            result.data?.data.also { uri->
                GlobalScope.launch {
                    if(writeToFile(uri!!)){
                        withContext(Dispatchers.Main){
                            Toast.makeText(context,getString(R.string.success_msg),Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun writeToFile(uri: Uri): Boolean {
        try {
            requireActivity().applicationContext.contentResolver.openFileDescriptor(uri,"w")?.use {
                FileOutputStream(it.fileDescriptor).use {fileOutputStream ->
                val employeesList=listViewModel.getList.value
                    if (employeesList!=null && employeesList.isNotEmpty()){
                        employeesList.forEach { employee ->
                            Log.d(TAG, "writeToFile: "+(employee.name+","+Designation.values()[employee.role]+","+Gender.values()[employee.gender]+","+employee.age+","+employee.phone))
                            fileOutputStream.write((employee.name+","+Designation.values()[employee.role]+","+Gender.values()[employee.gender]+","+employee.age+","+employee.phone+"\n").toByteArray())
                        }
                    }
                }
            }
        }catch (ex:FileNotFoundException){
            ex.printStackTrace()
            return false
        }catch (ex:IOException){
            ex.printStackTrace()
            return false
        }
        return true
    }


}