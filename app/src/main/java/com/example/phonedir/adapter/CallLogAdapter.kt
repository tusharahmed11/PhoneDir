package com.example.phonedir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView

import androidx.recyclerview.widget.RecyclerView
import com.example.phonedir.data.model.CallLogModel
import com.example.phonedir.R

class CallLogAdapter(private val mList: List<CallLogModel>) : RecyclerView.Adapter<CallLogAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.call_list_item_layout, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        holder.phoneNumberTv.text = itemsViewModel.phoneNumber
        holder.contactNameTv.text = itemsViewModel.contactName
        holder.callTypeTv.text = itemsViewModel.callType
        holder.callDateTv.text = itemsViewModel.callDate
        holder.callTimeTv.text = itemsViewModel.callTime
        holder.callDurationTv.text = itemsViewModel.callDuration
    }


    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val phoneNumberTv : AppCompatTextView = itemView.findViewById(R.id.phone_number_tv)
        val contactNameTv : AppCompatTextView = itemView.findViewById(R.id.contact_name_tv)
        val callTypeTv : AppCompatTextView = itemView.findViewById(R.id.call_type_tv)
        val callDateTv : AppCompatTextView = itemView.findViewById(R.id.call_date_tv)
        val callTimeTv : AppCompatTextView = itemView.findViewById(R.id.call_time_tv)
        val callDurationTv : AppCompatTextView = itemView.findViewById(R.id.call_duration_tv)
    }
}