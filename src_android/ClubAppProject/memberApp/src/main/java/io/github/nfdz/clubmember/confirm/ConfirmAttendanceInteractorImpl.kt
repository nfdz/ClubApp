package io.github.nfdz.clubmember.confirm

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import io.github.nfdz.clubmember.common.AttendedEventRealm
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.common.UserRealm
import io.github.nfdz.clubmember.reportException
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class ConfirmAttendanceInteractorImpl(val activity: Activity) : ConfirmAttendanceInteractor {

    private var eventId: String = ""

    override fun initialize(eventId: String) {
        this.eventId = eventId
    }

    override fun destroy() {
    }

    override fun confirmAttendance(
        values: List<String>,
        successCallback: () -> Unit,
        errorCallback: (invalidCode: Boolean) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback(false)
            return
        }

        FirebaseFirestore.getInstance().collection("qr_codes")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var anyMatch = false
                    task.result?.documents?.forEach { doc ->
                        val code = doc["code"] as String
                        if (values.contains(code)) {
                            anyMatch = true
                        }
                    }
                    if (anyMatch) {
                        val confirmation = mutableMapOf<String,Any>()
                        confirmation["eventId"] = eventId
                        confirmation["userEmail"] = email
                        FirebaseFirestore.getInstance().collection("user_event_confirmations").document()
                            .set(confirmation)
                            .addOnSuccessListener {
                                addPointsToUser(email) {
                                    AttendedEventRealm(eventId).save()
                                    successCallback()
                                    EventBus.getDefault().post(DataChangeEvent())
                                }
                            }
                            .addOnFailureListener {
                                Timber.e(it)
                                errorCallback(false)
                            }
                    } else {
                        errorCallback(true)
                    }
                } else {
                    errorCallback(false)
                }
            }
    }

    // TODO move this to a firebase function
    private fun addPointsToUser(email: String, callback: () -> Unit) {
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                val userDoc = task.result?.documents?.firstOrNull()
                if (task.isSuccessful && userDoc != null) {
                    val id = userDoc.id
                    val points = (userDoc.data!!["points"] as Number).toInt() + 10
                    FirebaseFirestore.getInstance().collection("users").document(id)
                        .update("points", points)
                        .addOnSuccessListener {
                            UserRealm().queryFirst()?.let { userRealm ->
                                userRealm.points = points.toInt()
                                userRealm.save()
                            }
                            callback()
                        }.addOnFailureListener {
                            Timber.e(it)
                            callback()
                        }
                } else {
                    reportException(IllegalStateException("Cannot retrieve user document"))
                    callback()
                }
            }
    }

}