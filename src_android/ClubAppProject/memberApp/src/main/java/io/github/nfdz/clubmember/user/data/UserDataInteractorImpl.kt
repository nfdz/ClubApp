package io.github.nfdz.clubmember.user.data

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import io.github.nfdz.clubmember.common.UserRealm
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class UserDataInteractorImpl(val activity: Activity) : UserDataInteractor {

    override fun initialize() {
    }

    override fun destroy() {
    }

    override fun loadData(callback: (UserDataFields) -> Unit) {
        UserRealm().queryFirst()?.let {
            callback(UserDataFields(it.fullName, it.email, it.birthday, it.address, it.phoneNumber))
        }
    }

    override fun saveData(
        birthday: String,
        address: String,
        phoneNumber: String,
        successCallback: () -> Unit,
        errorCallback: (invalidBirthday: Boolean) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
            errorCallback(false)
            return
        }

        try {
            val simpleFormatter = SimpleDateFormat(activity.getString(R.string.user_data_birthday_pattern), Locale.getDefault())
            val birthdayDate = simpleFormatter.parse(birthday)
            if (birthdayDate.time > System.currentTimeMillis()) throw IllegalArgumentException("Invalid birthday")
        } catch (e: Exception) {
            Timber.e(e)
            errorCallback(true)
            return
        }

        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnCompleteListener { task ->
                val userDoc = task.result?.documents?.firstOrNull()
                if (task.isSuccessful && userDoc != null) {
                    FirebaseFirestore.getInstance().collection("users").document(userDoc.id)
                        .update(
                            "birthday", birthday,
                            "address", address,
                            "phoneNumber", phoneNumber
                        ).addOnSuccessListener {
                            UserRealm().queryFirst()?.let { userRealm ->
                                userRealm.birthday = birthday
                                userRealm.address = address
                                userRealm.phoneNumber = phoneNumber
                                userRealm.save()
                            }
                            successCallback()
                        }.addOnFailureListener {
                            errorCallback(false)
                        }
                } else {
                    reportException(IllegalStateException("Cannot retrieve user document"))
                    errorCallback(false)
                }
            }
    }

    override fun changePassword(successCallback: () -> Unit, errorCallback: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val email = user?.email
        if (email == null || email.isEmpty()) {
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