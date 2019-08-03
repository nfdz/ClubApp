package io.github.nfdz.clubmember.common

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.printHourMinDayMonthYear
import io.github.nfdz.clubcommonlibrary.toDate
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.item_event.view.*
import kotlin.properties.Delegates

class EventsAdapter(data: List<EventEntry> = emptyList(), val smoothDataChanges: Boolean = true, val listener: Listener) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    data class EventEntry(val id: String, val imageUrl: String, val attendanceFlag: Boolean, val qrFlag: Boolean, val timestamp: Long) {
        companion object {
            fun buildFromEvent(event: EventRealm): EventsAdapter.EventEntry {
                val attendanceFlag = isGoingToEvent(event.id)
                return EventsAdapter.EventEntry(event.id,
                    event.imageUrl,
                    attendanceFlag,
                    attendanceFlag && isAvailableQR(event.timestamp),
                    event.timestamp)
            }
        }
    }

    interface Listener {
        fun onEventClick(event: EventEntry)
        fun onConfirmAttendanceClick(event: EventEntry)
    }

    var data by Delegates.observable(data) { _, oldList, newList ->
        if (smoothDataChanges) {
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
        } else {
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder(
            parent.inflate(R.layout.item_event),
            listener
        )
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class EventViewHolder(view: View, val listener: Listener) : RecyclerView.ViewHolder(view) {

        fun bind(item: EventEntry) = with(itemView) {
            list_item_tv_date.text = item.timestamp.toDate().printHourMinDayMonthYear()
            list_item_iv_event.setImageResource(R.drawable.image_empty_event)
            Glide.with(this)
                .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.image_empty_event).error(R.drawable.image_empty_event))
                .load(item.imageUrl)
                .into(list_item_iv_event)
            when {
                item.qrFlag -> {
                    list_item_iv_action.visibility = View.VISIBLE
                    list_item_iv_action.setImageResource(R.drawable.ic_qr_scan_light)
                }
                item.attendanceFlag -> {
                    list_item_iv_action.visibility = View.VISIBLE
                    list_item_iv_action.setImageResource(R.drawable.ic_person_checked_light)
                }
                else -> list_item_iv_action.visibility = View.GONE
            }
            list_item_iv_action.setOnClickListener { if (item.qrFlag) listener.onConfirmAttendanceClick(item) else listener.onEventClick(item) }
            setOnClickListener { listener.onEventClick(item) }
        }

    }

}