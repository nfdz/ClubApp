package io.github.nfdz.clubadmin.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubadmin.alerts.AlertsActivity
import io.github.nfdz.clubadmin.chat.ChatActivity
import io.github.nfdz.clubadmin.common.ADMIN_EMAIL_KEY
import io.github.nfdz.clubadmin.common.ADMIN_PASS_KEY
import io.github.nfdz.clubadmin.events.EventsActivity
import io.github.nfdz.clubadmin.qrs.QRsActivity
import io.github.nfdz.clubadmin.users.UsersActivity
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.setStringInPreferences
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_btn_events.setOnClickListener { navigateToEvents() }
        main_btn_users.setOnClickListener { navigateToUsers() }
        main_btn_alerts.setOnClickListener { navigateToAlerts() }
        main_btn_qrs.setOnClickListener { navigateToQRs() }
        main_btn_chat.setOnClickListener { navigateToChat() }
        main_btn_login.setOnClickListener { logIn(main_et_email.text.toString(), main_et_password.text.toString()) }

        if (isLoggedIn()) {
            main_admin_container.fadeIn()
        } else {
            main_guest_container.fadeIn()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_exit -> { navigateToExit(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToExit() {
        setStringInPreferences(ADMIN_EMAIL_KEY, "")
        setStringInPreferences(ADMIN_PASS_KEY, "")
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun navigateToEvents() {
        EventsActivity.startActivity(this)
    }

    private fun navigateToUsers() {
        UsersActivity.startActivity(this)
    }

    private fun navigateToAlerts() {
        AlertsActivity.startActivity(this)
    }

    private fun navigateToQRs() {
        QRsActivity.startActivity(this)
    }

    private fun navigateToChat() {
        ChatActivity.startActivity(this)
    }

    private fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    private fun logIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            toast(R.string.main_login_fail)
            return
        }

        main_guest_container.fadeOut()
        main_pb_loading.fadeIn()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                FirebaseFirestore.getInstance().collection("admins")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnCompleteListener(this) { taskQuery ->
                        val adminDoc = taskQuery.result?.documents?.firstOrNull()
                        if (task.isSuccessful && adminDoc != null) {
                            main_pb_loading.fadeOut()
                            main_admin_container.fadeIn()
                            setStringInPreferences(ADMIN_EMAIL_KEY, email)
                            setStringInPreferences(ADMIN_PASS_KEY, password)
                        } else {
                            handleLogInFail()
                        }
                    }
            } else {
                handleLogInFail()
            }
        }
    }

    private fun handleLogInFail() {
        main_pb_loading.fadeOut()
        FirebaseAuth.getInstance().signOut()
        toast(R.string.main_login_fail)
        main_guest_container.fadeIn()
    }

}
