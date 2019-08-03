package io.github.nfdz.clubadmin.users

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.github.nfdz.clubadmin.common.User
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.item_user.view.*
import kotlin.properties.Delegates

class UsersAdapter(val listener: Listener) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    interface Listener {
        fun onToggleUserEnableStateClick(user: User)
        fun onEditUserClick(user: User)
    }

    var data by Delegates.observable(emptyList<User>()) { _, _, _ ->
        filterData()
    }

    var filter by Delegates.observable("") { _, _, _ ->
        filterData()
    }

    private fun filterData() {
        filteredData = data.filter { it.fullName.contains(filter, true) }
    }

    private var filteredData by Delegates.observable(emptyList<User>()) { _, oldList, newList ->
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return oldList.size
            }
            override fun getNewListSize(): Int {
                return newList.size
            }
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition].id == newList[newItemPosition].id
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(
            parent.inflate(R.layout.item_user),
            listener
        )
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(filteredData[position])
    }

    override fun getItemCount(): Int = filteredData.size

    class UserViewHolder(view: View, val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bind(item: User) = with(itemView) {
            item_user_tv_fullname.text = item.fullName
            item_user_tv_email.text = item.email
            item_user_iv_toggle_state.setImageResource(if (item.isDisabled) R.drawable.ic_slider_dark else R.drawable.ic_slider_fill_accent)
            setOnClickListener { context.toast(item.fullName) }
            item_user_iv_edit.setOnClickListener { listener.onEditUserClick(item) }
            item_user_iv_toggle_state.setOnClickListener { listener.onToggleUserEnableStateClick(item) }
        }

    }

}