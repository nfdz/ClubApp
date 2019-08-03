package io.github.nfdz.clubadmin.events.attendance

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.item_user_attendance.view.*
import kotlin.properties.Delegates

class UsersAttendanceAdapter : RecyclerView.Adapter<UsersAttendanceAdapter.UserViewHolder>() {

    data class UserEntry(val fullName: String, val phoneNumber: String, val confirmationFlag: Boolean)

    var data by Delegates.observable(emptyList<UserEntry>()) { _, oldList, newList ->
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }
            override fun getNewListSize(): Int {
                return newList.size
            }
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(parent.inflate(R.layout.item_user_attendance))
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bind(item: UserEntry) = with(itemView) {
            item_user_attendance_tv_fullname.text = item.fullName
            item_user_attendance_tv_phone.text = item.phoneNumber
            item_user_attendance_iv_confirmation.visibility = if (item.confirmationFlag) View.VISIBLE else View.INVISIBLE
            setOnClickListener { context.toast(item.fullName)}
        }

    }

}