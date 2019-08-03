package io.github.nfdz.clubadmin.alerts

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_alerts.*

class AlertsActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, AlertsActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        alert_btn_send.setOnClickListener { sendNotification() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun sendNotification() {
        val title = alert_et_title.text.toString()
        val message = alert_et_message.text.toString()
        if (title.isEmpty() || message.isEmpty()) {
            toast(R.string.alerts_content_empty_error)
            return
        }

        alert_container.fadeOut()
        alert_pb_loading.fadeIn()
        val event = mutableMapOf<String,Any>()
        event["title"] = title
        event["message"] = message
        FirebaseFirestore.getInstance().collection("alerts").document()
            .set(event)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToFinish()
                } else {
                    alert_container.fadeIn()
                    alert_pb_loading.fadeOut()
                    toast(R.string.alerts_send_error)
                }
            }
    }

    private fun navigateToFinish() {
        finish()
    }

}
