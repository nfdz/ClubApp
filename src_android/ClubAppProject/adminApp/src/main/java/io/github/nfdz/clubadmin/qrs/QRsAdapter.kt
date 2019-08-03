package io.github.nfdz.clubadmin.qrs

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.github.nfdz.clubadmin.common.QRCode
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.item_qr.view.*
import kotlin.properties.Delegates

class QRsAdapter(data: List<QRCode> = emptyList(), val listener: Listener) : RecyclerView.Adapter<QRsAdapter.QRViewHolder>() {

    interface Listener {
        fun onDeleteQRClick(qr: QRCode)
    }

    var data by Delegates.observable(data) { _, oldList, newList ->
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRViewHolder {
        return QRViewHolder(
            parent.inflate(R.layout.item_qr),
            listener
        )
    }

    override fun onBindViewHolder(holder: QRViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class QRViewHolder(view: View, val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bind(item: QRCode) = with(itemView) {
            item_qr_tv_code.text = item.code
            setOnClickListener { context.toast(item.code) }
            item_qr_iv_delete.setOnClickListener { listener.onDeleteQRClick(item) }
        }

    }

}