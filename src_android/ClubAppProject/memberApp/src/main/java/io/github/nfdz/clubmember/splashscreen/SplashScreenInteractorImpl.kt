package io.github.nfdz.clubmember.splashscreen

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubcommonlibrary.getLongFromPreferences
import io.github.nfdz.clubcommonlibrary.setLongInPreferences
import io.github.nfdz.clubmember.common.*
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SplashScreenInteractorImpl(val activity: Activity) : SplashScreenInteractor {

    override fun initialize() {
        if (!isLoggedIn()) {
            unsubscribeChatNotificationTopic({}, {})
        }
    }

    override fun destroy() {
    }

    override fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    override fun syncAllAndSubscribeContent(forceSync: Boolean, successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit) {
        val lastSyncTime = activity.getLongFromPreferences(R.string.last_sync_timestamp, 0)
        val now = System.currentTimeMillis()
        val needSync = (now - lastSyncTime) > TimeUnit.HOURS.toMillis(AUTO_SYNC_ALL_MARGIN_IN_HOURS)
        val callback = {
            if (forceSync || needSync) {
                Timber.d("Need sync")
                syncAllDataFromFirebase({
                    activity.setLongInPreferences(R.string.last_sync_timestamp, now)
                    successCallback()
                }, userErrorStateCallback)
            } else {
                Timber.d("Do not need sync")
                successCallback()
            }
        }
        subscribeChatNotificationTopic(callback, callback)
    }

    override fun logIn(
        email: String,
        password: String,
        successCallback: () -> Unit,
        errorCallback: (credentialsError: Boolean, disabledError: Boolean) -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            errorCallback(true, false)
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                FirebaseFirestore.getInstance().collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener { taskQuery ->
                        syncUser(taskQuery, {
                            syncAllDataFromFirebase({
                                    subscribeChatNotificationTopic(successCallback, {
                                        errorCallback(false, false)
                                    })
                                }, {
                                    errorCallback(false, it)
                                }, true)
                        }, { isDisabled ->
                            if (!isDisabled) {
                                reportException(IllegalStateException("email=$email"))
                            }
                            FirebaseAuth.getInstance().signOut()
                            errorCallback(false, isDisabled)
                        })
                    }
            } else {
                errorCallback(task.exception is FirebaseAuthInvalidCredentialsException, false)
            }
        }

    }

    override fun resetPassword(email: String, successCallback: () -> Unit, errorCallback: () -> Unit) {
        if (email.isEmpty()) {
            errorCallback()
            return
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                successCallback()
            } else {
                errorCallback()
            }
        }
    }

}