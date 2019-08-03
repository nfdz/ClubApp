package io.github.nfdz.clubmember.common

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import com.vicpin.krealmextensions.*
import io.github.nfdz.clubcommonlibrary.doAsync
import io.github.nfdz.clubcommonlibrary.doMainThread
import io.github.nfdz.clubcommonlibrary.toCalendar
import io.github.nfdz.clubmember.reportException
import io.realm.Realm
import io.realm.Sort
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


fun syncAllDataFromFirebase(successCallback: () -> Unit = {},
                            userErrorStateCallback: (isDisabled: Boolean) -> Unit = {},
                            skipUser: Boolean = false) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email
    if (email == null || email.isEmpty()) {
        reportException(IllegalStateException("Trying to sync data with no logged user"))
        clearDataModelPersistence()
        FirebaseAuth.getInstance().signOut()
        userErrorStateCallback(false)
        return
    }

    val dataCollectionsToSync = if (skipUser) 3 else 4
    val dataCollectionsToSyncCounter = AtomicInteger(0)
    val finishCallback = {
        successCallback()
        EventBus.getDefault().post(DataChangeEvent())
    }

    // 1) Sync user
    if (!skipUser) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                syncUser(task, {
                    if (dataCollectionsToSyncCounter.incrementAndGet() == dataCollectionsToSync) {
                        finishCallback()
                    }
                }, { isDisabled ->
                    if (isDisabled) {
                        FirebaseAuth.getInstance().signOut()
                        clearDataModelPersistence()
                        userErrorStateCallback(true)
                    } else {
                        if (dataCollectionsToSyncCounter.incrementAndGet() == dataCollectionsToSync) {
                            finishCallback()
                        }
                    }
                })
            }
    }

    // 2) Sync events
    val timestampFrom = System.currentTimeMillis().toCalendar().apply { add(Calendar.HOUR_OF_DAY, -MARGIN_TO_CONFIRM_IN_HOURS) }.timeInMillis
    FirebaseFirestore.getInstance().collection("events")
        .whereGreaterThan("timestamp", timestampFrom)
        .get()
        .addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    executeTransaction {
                        EventRealm().deleteAll()
                        task.result?.documents?.map { doc ->
                            EventRealm(doc.id,
                                doc.data!!["title"] as String,
                                doc.data!!["description"] as String,
                                doc.data!!["imageUrl"] as String,
                                doc.data!!["facebookUrl"] as String,
                                doc.data!!["instagramUrl"] as String,
                                doc.data!!["category"] as String,
                                (doc.data!!["timestamp"] as Number).toLong())
                        }?.saveAll()
                    }
                }
            } catch (e: Exception) {
                reportException(e)
            }
            if (dataCollectionsToSyncCounter.incrementAndGet() == dataCollectionsToSync) {
                finishCallback()
            }
        }

    // 3) Sync event bookings
    FirebaseFirestore.getInstance().collection("user_event_bookings")
        .whereEqualTo("userEmail", email)
        .get()
        .addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    executeTransaction {
                        BookedEventRealm().deleteAll()
                        task.result?.documents?.map { doc ->
                            BookedEventRealm(doc.data!!["eventId"] as String)
                        }?.saveAll()
                    }
                }
            } catch (e: Exception) {
                reportException(e)
            }
            if (dataCollectionsToSyncCounter.incrementAndGet() == dataCollectionsToSync) {
                finishCallback()
            }
        }

    // 4) Sync event confirmations
    FirebaseFirestore.getInstance().collection("user_event_confirmations")
        .whereEqualTo("userEmail", email)
        .get()
        .addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    executeTransaction {
                        AttendedEventRealm().deleteAll()
                        task.result?.documents?.map { doc ->
                            AttendedEventRealm(doc.data!!["eventId"] as String)
                        }?.saveAll()
                    }
                }
            } catch (e: Exception) {
                reportException(e)
            }
            if (dataCollectionsToSyncCounter.incrementAndGet() == dataCollectionsToSync) {
                finishCallback()
            }
        }
}

fun syncUser(task: Task<QuerySnapshot>,
             successCallback: () -> Unit,
             errorCallback: (isDisabled: Boolean) -> Unit) {
    try {
        var success = false
        var isDisabled = false
        val userDoc = task.result?.documents?.firstOrNull()
        if (task.isSuccessful && userDoc != null) {
            try {
                executeTransaction {
                    UserRealm().deleteAll()
                    isDisabled = userDoc.data!!["isDisabled"] as Boolean
                    if (!isDisabled) {
                        UserRealm(
                            userDoc.data!!["email"] as String,
                            userDoc.data!!["alias"] as String,
                            userDoc.data!!["fullName"] as String,
                            userDoc.data!!["address"] as String,
                            userDoc.data!!["phoneNumber"] as String,
                            userDoc.data!!["birthday"] as String,
                            (userDoc.data!!["signUpTimestamp"] as Number).toLong(),
                            (userDoc.data!!["points"] as Number).toInt(),
                            userDoc.data!!["highlightChatFlag"] as Boolean,
                            isDisabled).save()
                    }
                }
                if (isDisabled) {
                    errorCallback(true)
                } else {
                    success = true
                    successCallback()
                }
            } catch (e: Exception) {
                reportException(e)
            }
        }
        if (!success && !isDisabled){
            errorCallback(false)
        }
    } catch (e: java.lang.Exception) {
        reportException(e)
        errorCallback(false)
    }
}


fun syncChatFromFirebase(successCallback: (anyNewMessages: Boolean) -> Unit = {},
                         errorCallback: () -> Unit = {}) {
    val user = FirebaseAuth.getInstance().currentUser
    val email = user?.email
    if (email == null || email.isEmpty()) {
        reportException(IllegalStateException("Trying to sync chat with no logged user"))
        errorCallback()
        return
    }

    var lastMessageTimestamp: Long = -1L
    var realm: Realm? = null
    try {
        realm = Realm.getDefaultInstance()
        val result = realm.where(ChatMessageRealm::class.java).sort("timestamp", Sort.DESCENDING).findFirst()
        result?.let { lastMessageTimestamp = it.timestamp }
    } catch (e: Exception) {
        Timber.e(e)
    } finally {
        realm?.close()
    }

    FirebaseFirestore.getInstance().collection("chat_messages")
        .whereGreaterThan("timestamp", lastMessageTimestamp)
        .orderBy("timestamp", Query.Direction.DESCENDING)
        .limit(LIMIT_CHAT_MESSAGES_STORAGE.toLong())
        .get()
        .addOnCompleteListener { task ->
            var success = false
            try {
                if (task.isSuccessful) {
                    doAsync {
                        var anyNewMessages = false
                        executeTransaction {
                            task.result?.documents?.map { doc ->
                                anyNewMessages = true
                                ChatMessageRealm((doc.data!!["timestamp"] as Number).toLong(),
                                    doc.data!!["authorEmail"] as String,
                                    doc.data!!["authorAlias"] as String,
                                    doc.data!!["highlight"] as Boolean,
                                    doc.data!!["text"] as String)
                            }?.saveAll()
                            ChatMessageRealm().querySorted("timestamp", Sort.DESCENDING).forEachIndexed { index, chatMessageRealm ->
                                if (index > LIMIT_CHAT_MESSAGES_STORAGE) {
                                    ChatMessageRealm().delete { equalTo("timestamp", chatMessageRealm.timestamp) }
                                }
                            }
                        }
                        doMainThread {
                            success = true
                            successCallback(anyNewMessages)
                            if (anyNewMessages) {
                                EventBus.getDefault().post(ChatEvent())
                            }
                        }
                    }

                }
            } catch (e: Exception) {
                reportException(e)
            }
            if (!success) {
                errorCallback()
            }
        }

}

fun subscribeChatNotificationTopic(successCallback: () -> Unit, errorCallback: () -> Unit) {
    FirebaseMessaging.getInstance().subscribeToTopic("chat")
        .addOnSuccessListener { successCallback() }
        .addOnFailureListener {
            Timber.e(it)
            errorCallback()
        }
}

fun unsubscribeChatNotificationTopic(successCallback: () -> Unit, errorCallback: () -> Unit) {
    FirebaseMessaging.getInstance().unsubscribeFromTopic("chat")
        .addOnSuccessListener { successCallback() }
        .addOnFailureListener {
            Timber.e(it)
            errorCallback()
        }
}
