package io.github.nfdz.clubadmin.events

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.github.nfdz.clubadmin.common.Event
import io.github.nfdz.clubadmin.events.attendance.EventAttendanceActivity
import io.github.nfdz.clubadmin.events.editor.EventEditorActivity
import io.github.nfdz.clubcommonlibrary.printHourMinDayMonthYear
import io.github.nfdz.clubcommonlibrary.toDate
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_events.*
import timber.log.Timber


class EventsActivity : AppCompatActivity(), EventsAdapter.Listener {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, EventsActivity::class.java))
        }
    }

    private val adapter = EventsAdapter(listener = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_events)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        events_rv.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        events_refresh.setOnRefreshListener { refreshContent() }
        events_fab_add.setOnClickListener { navigateToAddEvent() }
        events_rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun refreshContent() {
        events_refresh.isRefreshing = true
        FirebaseFirestore.getInstance().collection("events")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(this) { task ->
                try {
                    if (task.isSuccessful) {
                        adapter.data = task.result?.documents?.map { doc ->
                            Event(doc.id,
                                doc.data!!["title"] as String,
                                doc.data!!["description"] as String,
                                doc.data!!["imageUrl"] as String,
                                doc.data!!["facebookUrl"] as String,
                                doc.data!!["instagramUrl"] as String,
                                doc.data!!["category"] as String,
                                (doc.data!!["timestamp"] as Number).toLong())
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                events_refresh.isRefreshing = false
            }

    }

    override fun onCheckUsersClick(event: Event) {
        EventAttendanceActivity.startActivity(this, event)
    }

    override fun onEditEventClick(event: Event) {
        EventEditorActivity.startActivity(this, event)
    }

    override fun onDeleteEventClick(event: Event) {
        askDeleteConfirmation(event)
    }

    private fun askDeleteConfirmation(event: Event) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.events_confirm_delete_title)
            setMessage(getString(R.string.events_confirm_delete_message, event.title, event.timestamp.toDate().printHourMinDayMonthYear()))
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            deleteEvent(event)
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    private fun deleteEvent(event: Event) {
        toast(R.string.events_confirm_deleting)
        FirebaseFirestore.getInstance().collection("user_event_confirmations")
            .whereEqualTo("eventId", event.id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.documents?.forEach { doc ->
                        FirebaseFirestore.getInstance().collection("user_event_confirmations")
                            .document(doc.id)
                            .delete()
                    }
                }
            }
        FirebaseFirestore.getInstance().collection("user_event_bookings")
            .whereEqualTo("eventId", event.id)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result?.documents?.forEach { doc ->
                        FirebaseFirestore.getInstance().collection("user_event_bookings")
                            .document(doc.id)
                            .delete()
                    }
                }
            }
        FirebaseFirestore.getInstance().collection("events")
            .document(event.id)
            .delete()
            .addOnCompleteListener(this) {
                refreshContent()
            }
    }

    private fun navigateToAddEvent() {
        EventEditorActivity.startActivity(this)
    }

}
