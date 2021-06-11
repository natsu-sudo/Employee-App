package com.coding.employeeapp.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.coding.employeeapp.R
import com.coding.employeeapp.database.Designation
import com.coding.employeeapp.database.Employee
import com.coding.employeeapp.database.Gender


class EmployeeAdapter(val listener: (Long) -> Unit): ListAdapter<Employee, EmployeeAdapter.ViewHolder>(
    DiffCallback()
) {
    inner class ViewHolder(private val view: View):RecyclerView.ViewHolder(view) {

        init {
            itemView.setOnClickListener {
                listener.invoke(getItem(adapterPosition).id)
            }
        }
        private val name:TextView=view.findViewById(R.id.employee_name)
        private val age:TextView=view.findViewById(R.id.employee_age)
        private val gender:TextView=view.findViewById(R.id.employee_gender)
        private val designation:TextView=view.findViewById(R.id.employee_designation)
        private val picture:ImageView=view.findViewById(R.id.employee_pic)
        private val number:TextView=view.findViewById(R.id.empl_number)

        fun onBind(item: Employee) {

            name.text=view.context.getString(R.string.name_emp, item.name)
            age.text=view.context.getString(R.string.age, item.age.toString())
            gender.text=view.context.getString(R.string.gender_, Gender.values()[item.gender])
            number.text=view.context.getString(R.string.phone_1_s,item.phone)
            designation.text=view.context.getString(
                R.string.designation,
                Designation.values()[item.role].name
            )
            if (item.photo.isEmpty()){
                picture.setImageResource(R.drawable.blank_photo)
            }else{
                picture.setImageURI(Uri.parse(item.photo))
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view:View=LayoutInflater.from(parent.context).inflate(
            R.layout.employee_list_adapter,
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(position))
    }

}

class DiffCallback:DiffUtil.ItemCallback<Employee>() {
    override fun areItemsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem.id==newItem.id
    }

    override fun areContentsTheSame(oldItem: Employee, newItem: Employee): Boolean {
        return oldItem==newItem
    }

}
