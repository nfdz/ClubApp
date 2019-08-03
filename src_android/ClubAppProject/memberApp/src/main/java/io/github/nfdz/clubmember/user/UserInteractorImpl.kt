package io.github.nfdz.clubmember.user

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import io.github.nfdz.clubcommonlibrary.doAsync
import io.github.nfdz.clubcommonlibrary.doMainThread
import io.github.nfdz.clubmember.common.AttendedEventRealm
import io.github.nfdz.clubmember.common.BookedEventRealm
import io.github.nfdz.clubmember.common.UserRealm
import io.github.nfdz.clubmember.common.clearDataModelPersistence
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R

class UserInteractorImpl(val activity: Activity) : UserInteractor {

    private val minAliasLength = activity.resources.getInteger(R.integer.alias_min_length)
    private val maxAliasLength = activity.resources.getInteger(R.integer.alias_max_length)

    override fun initialize() {
    }

    override fun destroy() {
    }

    override fun loadUserData(callback: (alias: String?, fullName: String, hasBookings: Boolean, signUpDate: Long, points: Int) -> Unit) {
        UserRealm().queryFirst()?.let { user ->
            val bookings = BookedEventRealm().queryAll().map { it.id }.toSet()
            val confirmations = AttendedEventRealm().queryAll().map { it.id }.toSet()
            val hasPendingBookings = !bookings.subtract(confirmations).isEmpty()
            callback(if (user.alias.isEmpty()) null else user.alias, user.fullName, hasPendingBookings, user.signUpTimestamp, user.points)
        }
    }

    override fun changeAlias(
        alias: String?,
        successCallback: () -> Unit,
        errorCallback: (invalid: Boolean, conflict: Boolean) -> Unit
    ) {

        if (alias == null || !isAliasValid(alias)) {
            errorCallback(true, false)
            return
        }

        var sameAlias = false
        UserRealm().queryFirst()?.let { sameAlias = alias == it.alias }
        if (sameAlias) {
            successCallback()
            return
        }

        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback(false, false)
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("alias", alias)
            .get()
            .addOnCompleteListener { taskQuery ->
                if (taskQuery.isSuccessful && taskQuery.result?.documents?.isEmpty() == true) {
                    FirebaseFirestore.getInstance().collection("users")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnCompleteListener { task ->
                            val userDoc = task.result?.documents?.firstOrNull()
                            if (task.isSuccessful && userDoc != null) {
                                FirebaseFirestore.getInstance().collection("users").document(userDoc.id)
                                    .update("alias", alias).addOnSuccessListener {
                                        UserRealm().queryFirst()?.let { userRealm ->
                                            userRealm.alias = alias
                                            userRealm.save()
                                        }
                                        successCallback()
                                    }.addOnFailureListener {
                                        errorCallback(false, false)
                                    }
                            } else {
                                reportException(IllegalStateException("Cannot retrieve user document"))
                                errorCallback(false, false)
                            }
                        }
                } else {
                    errorCallback(false, true)
                }
            }
    }

    private fun isAliasValid(alias: String): Boolean {
        if (alias.length < minAliasLength || alias.length > maxAliasLength) {
            return false
        }
        for (c in alias) {
            if ((c != '_') && (c != '-') && (c != ' ') && (c !in 'a'..'z') && (c !in 'A'..'Z') && (c !in '0'..'9')) {
                return false
            }
        }
        return true
    }

    override fun logout(callback: () -> Unit) {
        doAsync {
            try {
                FirebaseAuth.getInstance().signOut()
                clearDataModelPersistence()
            } catch (e: Exception) {
                reportException(e)
            } finally {
                doMainThread {
                    callback()
                }
            }
        }

    }

}