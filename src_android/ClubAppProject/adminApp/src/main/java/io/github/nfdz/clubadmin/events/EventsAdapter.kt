package io.github.nfdz.clubadmin.events

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import io.github.nfdz.clubadmin.common.Event
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.printHourMinDayMonthYear
import io.github.nfdz.clubcommonlibrary.toDate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.item_event.view.*
import kotlin.properties.Delegates

class EventsAdapter(data: List<Event> = emptyList(), val listener: Listener) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    interface Listener {
        fun onCheckUsersClick(event: Event)
        fun onEditEventClick(event: Event)
        fun onDeleteEventClick(event: Event)
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

        fun bind(item: Event) = with(itemView) {
            item_event_tv_title.text = item.title
            item_event_tv_date.text = item.timestamp.toDate().printHourMinDayMonthYear()
            setOnClickListener { context.toast(item.title) }
            item_event_iv_bookings.setOnClickListener { listener.onCheckUsersClick(item) }
            item_event_iv_edit.setOnClickListener { listener.onEditEventClick(item) }
            item_event_iv_delete.setOnClickListener { listener.onDeleteEventClick(item) }
        }

    }

}