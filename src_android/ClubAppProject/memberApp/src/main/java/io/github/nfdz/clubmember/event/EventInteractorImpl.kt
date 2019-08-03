package io.github.nfdz.clubmember.event

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import io.github.nfdz.clubmember.common.*
import YOUR.MEMBER.APP.ID.HERE.R
import org.greenrobot.eventbus.EventBus
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger


class EventInteractorImpl(val activity: Activity) : EventInteractor {

    private var eventInfo: EventInfo = EventInfo("", "", "", "", "",0)
    private var eventId: String = ""
    private var validEvent: Boolean = false

    override fun initialize(eventId: String) {
        this.eventId = eventId
        EventRealm().queryFirst { equalTo("id", eventId) }?.let {
            validEvent = true
            eventInfo = EventInfo(it.title, it.description, it.imageUrl, it.facebookUrl, it.instagramUrl, it.timestamp)
        }
    }

    override fun destroy() {

    }

    override fun isEventValid(): Boolean {
        return validEvent
    }

    override fun getEventInfo() = eventInfo

    override fun getEventId() = eventId

    override fun getAttendanceStatus(callback: (status: AttendanceActionStatus, eventId: String) -> Unit) {
        callback(when {
                isEventConfirmed(eventId) -> AttendanceActionStatus.CONFIRMED
                isGoingToEvent(eventId) -> when {
                    isAvailableQR(eventInfo.timestamp) -> AttendanceActionStatus.AVAILABLE_SCAN
                    isAttendanceModifiable(eventInfo.timestamp) -> AttendanceActionStatus.AVAILABLE_CANCEL
                    else -> AttendanceActionStatus.JOINED_LOCKED
                }
                else -> when {
                    isAttendanceModifiable(eventInfo.timestamp) -> AttendanceActionStatus.AVAILABLE_JOIN
                    else -> AttendanceActionStatus.NOT_JOINED_LOCKED
                }
            }, eventId)
    }

    override fun joinToEvent(successCallback: () -> Unit, errorCallback: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback()
            return
        }

        val booking = mutableMapOf<String,Any>()
        booking["eventId"] = eventId
        booking["userEmail"] = email

        FirebaseFirestore.getInstance().collection("user_event_bookings").document()
            .set(booking)
            .addOnSuccessListener {
                BookedEventRealm(eventId).save()
                successCallback()
                EventBus.getDefault().post(DataChangeEvent())
            }
            .addOnFailureListener {
                Timber.e(it)
                errorCallback()
            }
    }

    override fun cancelEvent(successCallback: () -> Unit, errorCallback: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback()
            return
        }

        FirebaseFirestore.getInstance().collection("user_event_bookings")
            .whereEqualTo("userEmail", email)
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val docsToDelete: Int = task.result?.documents?.size ?: 0
                    val docsDeletedCounter = AtomicInteger(0)
                    val anyError = AtomicBoolean(false)
                    val finishDeletion = {
                        if (!anyError.get()) {
                            BookedEventRealm().delete { equalTo("id", eventId) }
                            successCallback()
                            EventBus.getDefault().post(DataChangeEvent())
                        } else {
                            errorCallback()
                        }
                    }
                    if (docsToDelete == 0) {
                        finishDeletion()
                    } else {
                        task.result?.documents?.forEach { doc ->
                            FirebaseFirestore.getInstance().collection("user_event_bookings")
                                .document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    if (docsDeletedCounter.incrementAndGet() == docsToDelete) {
                                        finishDeletion()
                                    }
                                }
                                .addOnFailureListener {
                                    Timber.e(it)
                                    anyError.set(true)
                                    if (docsDeletedCounter.incrementAndGet() == docsToDelete) {
                                        finishDeletion()
                                    }
                                }
                        }
                    }
                } else {
                    errorCallback()
                }
            }
    }

    override fun getShareText(): String {
        val eventUrl = "https://${activity.getString(R.string.event_url_host)}/event/$eventId"
        return activity.getString(R.string.event_share_text, eventUrl)
    }

    override fun getFacebookUrl() = eventInfo.facebookUrl

    override fun getInstagramUrl() = eventInfo.instagramUrl

}