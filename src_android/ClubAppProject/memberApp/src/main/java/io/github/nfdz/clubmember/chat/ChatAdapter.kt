package io.github.nfdz.clubmember.chat

import android.graphics.Typeface
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.nfdz.clubcommonlibrary.inflate
import io.github.nfdz.clubcommonlibrary.printDayMonthYear
import io.github.nfdz.clubcommonlibrary.printHourMin
import io.github.nfdz.clubcommonlibrary.toDate
import YOUR.MEMBER.APP.ID.HERE.R
import kotlin.properties.Delegates

class ChatAdapter(data: List<ChatMessage> = emptyList(), val listener: Listener) : RecyclerView.Adapter<ChatAdapter.ChatMessageViewHolder>() {

    companion object {
        private const val OWN_MESSAGE_TYPE = 100
        private const val OTHER_MESSAGE_TYPE = 200
    }

    interface Listener {
        fun onChatMessageLongClick(message: ChatMessage)
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
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldList[oldItemPosition] == newList[newItemPosition]
            }
        })
        result.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        return ChatMessageViewHolder(
            parent.inflate(if (OWN_MESSAGE_TYPE == viewType) R.layout.item_chat_own_message else R.layout.item_chat_other_message),
            listener
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (data[position].isOwn) OWN_MESSAGE_TYPE else OTHER_MESSAGE_TYPE
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    class ChatMessageViewHolder(view: View, val listener: Listener) : RecyclerView.ViewHolder(view) {

        val aliasView: TextView by lazy { itemView.findViewById<TextView>(R.id.chat_message_tv_alias) }
        val dateView: TextView by lazy { itemView.findViewById<TextView>(R.id.chat_message_tv_date) }
        val textView: TextView by lazy {
            itemView.findViewById<TextView>(R.id.chat_message_tv_text).apply {
                movementMethod = LinkMovementMethod.getInstance()
            }
        }

        fun bind(item: ChatMessage) {
            aliasView.text = item.author
            if (item.highlight) {
                aliasView.setTypeface(null, Typeface.BOLD)
            } else {
                aliasView.setTypeface(null, Typeface.NORMAL)
            }
            textView.text = item.text
            val msgDate = item.timestamp.toDate()
            val dateText = "${msgDate.printHourMin()}\n${msgDate.printDayMonthYear()}"
            dateView.text = dateText
            itemView.setOnLongClickListener { listener.onChatMessageLongClick(item); true }
        }

    }

}