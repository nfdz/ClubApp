package io.github.nfdz.clubmember.chat

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.querySorted
import com.vicpin.krealmextensions.save
import io.github.nfdz.clubcommonlibrary.doAsync
import io.github.nfdz.clubcommonlibrary.doMainThread
import io.github.nfdz.clubcommonlibrary.getBooleanFromPreferences
import io.github.nfdz.clubcommonlibrary.setBooleanInPreferences
import io.github.nfdz.clubmember.common.ChatEvent
import io.github.nfdz.clubmember.common.ChatMessageRealm
import io.github.nfdz.clubmember.common.UserRealm
import io.github.nfdz.clubmember.common.syncChatFromFirebase
import YOUR.MEMBER.APP.ID.HERE.R
import io.realm.Sort
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class ChatInteractorImpl(val activity: Activity) : ChatInteractor {

    private val clipboardLabel = activity.getString(R.string.chat_clipboard_label)
    private var userAlias: String = ""
    private var userEmail: String = ""
    private var userHighlightChatFlag: Boolean = false

    private var cache: List<ChatMessage> = emptyList()
    private var lastMessageTimestamp = -1L

    override fun initialize() {
        UserRealm().queryFirst()?.let {
            userAlias = if (it.alias.isEmpty()) it.fullName else it.alias
            userEmail = it.email
            userHighlightChatFlag = it.highlightChatFlag
        }
    }

    override fun destroy() {
    }

    override fun syncChat() {
        syncChatFromFirebase({}, {})
    }

    override fun cancelNotifications() {
        NotificationManagerCompat.from(activity).cancelAll()
    }

    override fun getAndSetFirstTimeInChat(): Boolean {
        val isFirstTime = activity.getBooleanFromPreferences(R.string.chat_first_time_preference_key, R.string.chat_first_time_preference_default)
        if (isFirstTime) {
            doAsync {
                activity.setBooleanInPreferences(R.string.chat_first_time_preference_key, false)
            }
        }
        return isFirstTime
    }

    override fun copyMessageToTheClipboard(message: ChatMessage) {
        if (!message.text.isEmpty()) {
            val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(clipboardLabel, message.text)
            clipboard?.primaryClip = clip
        }
    }

    override fun loadChatMessages(callback: (items: List<ChatMessage>, newMessages: Boolean) -> Unit) {
        val beforeLastMessageTimestamp = lastMessageTimestamp
        doAsync {
            val newData = ChatMessageRealm().querySorted("timestamp", Sort.DESCENDING) {
                greaterThan("timestamp", lastMessageTimestamp)
            }.map {
                ChatMessage(it.authorAlias,
                    it.timestamp,
                    it.text,
                    it.highlight,
                    it.authorEmail == userEmail)
            }
            lastMessageTimestamp = newData.firstOrNull()?.timestamp ?: lastMessageTimestamp
            cache = newData + cache
            doMainThread {
                val afterLastMessageTimestamp = lastMessageTimestamp
                val newMessages = when (beforeLastMessageTimestamp) {
                    -1L -> false
                    afterLastMessageTimestamp -> false
                    else -> true
                }
                callback(cache, newMessages)
            }
        }
    }

    override fun sendMessage(text: String, successCallback: () -> Unit, errorCallback: () -> Unit) {
        if (text.trim().isEmpty()) {
            successCallback()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback()
            return
        }

        val timestamp = System.currentTimeMillis()
        val message = mutableMapOf<String,Any>()
        message["authorEmail"] = email
        message["authorAlias"] = userAlias
        message["highlight"] = userHighlightChatFlag
        message["text"] = text
        message["timestamp"] = timestamp

        FirebaseFirestore.getInstance().collection("chat_messages").document()
            .set(message)
            .addOnSuccessListener {
                ChatMessageRealm(timestamp,
                    email,
                    userAlias,
                    userHighlightChatFlag,
                    text).save()
                successCallback()
                EventBus.getDefault().post(ChatEvent())
            }
            .addOnFailureListener {
                Timber.e(it)
                errorCallback()
            }
    }

}