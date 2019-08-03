package io.github.nfdz.clubadmin.events.attendance

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.nfdz.clubadmin.common.Event
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_event_attendance.*
import timber.log.Timber

class EventAttendanceActivity : AppCompatActivity() {

    companion object {
        private const val EVENT_ID_KEY = "event_id"
        fun startActivity(context: Context, event: Event) {
            context.startActivity(Intent(context, EventAttendanceActivity::class.java).apply { putExtra(EVENT_ID_KEY, event.id) })
        }
    }

    private var eventId: String = ""
    private val adapter = UsersAttendanceAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_attendance)
        eventId = intent.getStringExtra(EVENT_ID_KEY) ?: ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        event_attendance_rv.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        event_attendance_refresh.setOnRefreshListener { refreshContent() }
        event_attendance_rv.adapter = adapter
        refreshContent()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun refreshContent() {
        event_attendance_refresh.isRefreshing = true

        FirebaseFirestore.getInstance().collection("user_event_bookings")
            .whereEqualTo("eventId", eventId)
            .get()
            .addOnCompleteListener(this) { taskBookings ->
                try {
                    if (taskBookings.isSuccessful) {
                        val bookingsEmail = taskBookings.result?.documents?.map { docBooking ->
                            docBooking.data!!["userEmail"] as String
                        }?.toSet() ?: emptySet()
                        FirebaseFirestore.getInstance().collection("user_event_confirmations")
                            .whereEqualTo("eventId", eventId)
                            .get()
                            .addOnCompleteListener(this) { taskConfirmations ->
                                try {
                                    if (taskConfirmations.isSuccessful) {
                                        val confirmationsEmail = taskConfirmations.result?.documents?.map { docConfirmation ->
                                            docConfirmation.data!!["userEmail"] as String
                                        }?.toSet() ?: emptySet()

                                        FirebaseFirestore.getInstance().collection("users")
                                            .orderBy("fullName", Query.Direction.ASCENDING)
                                            .get()
                                            .addOnCompleteListener(this) { taskUsers ->
                                                try {
                                                    if (taskUsers.isSuccessful) {
                                                        adapter.data = taskUsers.result?.documents
                                                        ?.filter { docUser ->
                                                            val email = docUser.data!!["email"] as String
                                                            bookingsEmail.contains(email)
                                                        }
                                                        ?.map { docUser ->
                                                            UsersAttendanceAdapter.UserEntry(docUser.data!!["fullName"] as String,
                                                                docUser.data!!["phoneNumber"] as String,
                                                                confirmationsEmail.contains(docUser.data!!["email"] as String))
                                                        } ?: emptyList()
                                                        event_attendance_refresh.isRefreshing = false
                                                    } else {
                                                        onRefreshError()
                                                    }
                                                } catch (e: Exception) {
                                                    Timber.e(e)
                                                    onRefreshError()
                                                }
                                            }
                                    } else {
                                        onRefreshError()
                                    }
                                } catch (e: Exception) {
                                    onRefreshError()
                                }
                            }
                    } else {
                        onRefreshError()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    onRefreshError()
                }
            }

    }

    private fun onRefreshError() {
        toast(R.string.event_attendance_refresh_error)
        event_attendance_refresh.isRefreshing = false
    }


}