package io.github.nfdz.clubadmin.users

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubadmin.common.ADMIN_EMAIL_KEY
import io.github.nfdz.clubadmin.common.ADMIN_PASS_KEY
import io.github.nfdz.clubcommonlibrary.getStringFromPreferences
import java.text.SimpleDateFormat
import java.util.*


fun checkDateText(text: String, csvPattern: Boolean = false): Boolean = getDateFromText(text, csvPattern) != null

fun getDateFromText(text: String, csvPattern: Boolean = false): Long? {
    return try {
        val simpleFormatter = SimpleDateFormat(if (csvPattern) "dd/MM/yyyy" else "dd/MM/yy", Locale.getDefault())
        simpleFormatter.parse(text).time
    } catch (e: Exception) {
        null
    }
}

data class NewUser(val email: String,
                   val fullName: String,
                   val address: String,
                   val phoneNumber: String,
                   val birthday: String,
                   val highlightChatFlag: Boolean,
                   val signUpTimestamp: Long)

fun createUser(activity: Activity,
               userToCreate: NewUser,
               successCallback: () -> Unit,
               errorCallback:(alreadyExists: Boolean, createError: Boolean, saveError: Boolean) -> Unit) {
    FirebaseFirestore.getInstance().collection("users")
        .whereEqualTo("email", userToCreate.email)
        .get()
        .addOnCompleteListener(activity) { taskGet ->
            val userDoc = taskGet.result?.documents?.firstOrNull()
            if (taskGet.isSuccessful && userDoc == null) {
                val userPassword = userToCreate.birthday
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(userToCreate.email, userPassword)
                    .addOnCompleteListener { taskCreate ->
                        FirebaseAuth.getInstance().sendPasswordResetEmail(userToCreate.email)
                            .addOnCompleteListener {
                                val adminEmail = activity.getStringFromPreferences(ADMIN_EMAIL_KEY, "")
                                val adminPass = activity.getStringFromPreferences(ADMIN_PASS_KEY, "")
                                FirebaseAuth.getInstance().signInWithEmailAndPassword(adminEmail, adminPass).addOnCompleteListener(activity) { taskReLogin ->
                                    if (taskReLogin.isSuccessful) {
                                        if (taskCreate.isSuccessful) {
                                            val user = mutableMapOf<String,Any>()
                                            user["email"] = userToCreate.email
                                            user["fullName"] = userToCreate.fullName
                                            user["alias"] = ""
                                            user["address"] = userToCreate.address
                                            user["phoneNumber"] = userToCreate.phoneNumber
                                            user["birthday"] = userToCreate.birthday
                                            user["points"] = 0
                                            user["highlightChatFlag"] = userToCreate.highlightChatFlag
                                            user["signUpTimestamp"] = userToCreate.signUpTimestamp
                                            user["isDisabled"] = false
                                            FirebaseFirestore.getInstance().collection("users").document()
                                                .set(user)
                                                .addOnCompleteListener(activity) { task ->
                                                    if (task.isSuccessful) {
                                                        successCallback()
                                                    } else {
                                                        errorCallback(false, false, true)
                                                    }
                                                }
                                        } else {
                                            errorCallback(false, true, false)
                                        }
                                    } else {
                                        errorCallback(false, true, false)
                                    }
                                }
                            }
                    }
            } else {
                errorCallback(true, false, false)
            }
        }
}