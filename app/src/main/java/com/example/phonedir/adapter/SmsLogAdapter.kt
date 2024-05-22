package com.example.phonedir.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.phonedir.R
import com.example.phonedir.data.model.MessageLogModel

class SmsLogAdapter(private val mList: List<MessageLogModel>) : RecyclerView.Adapter<SmsLogAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.sms_list_item_layout, parent, false)

        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return  mList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val itemsViewModel = mList[position]
        holder.phoneNumberTv.text = itemsViewModel.phoneNumber
        holder.contactNameTv.text = itemsViewModel.contactName
        holder.callDateTv.text = itemsViewModel.messageDate
        holder.messageTv.text = itemsViewModel.message
    }


    inner class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val phoneNumberTv : AppCompatTextView = itemView.findViewById(R.id.phone_number_tv)
        val contactNameTv : AppCompatTextView = itemView.findViewById(R.id.contact_name_tv)
        val callDateTv : AppCompatTextView = itemView.findViewById(R.id.call_date_tv)
        val messageTv : AppCompatTextView = itemView.findViewById(R.id.message_tv)

    }
}