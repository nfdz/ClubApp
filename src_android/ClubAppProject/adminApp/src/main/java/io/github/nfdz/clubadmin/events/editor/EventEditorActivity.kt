package io.github.nfdz.clubadmin.events.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubadmin.common.Event
import io.github.nfdz.clubcommonlibrary.*
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_event_editor.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class EventEditorActivity : AppCompatActivity() {

    companion object {
        private const val EVENT_TO_EDIT_KEY = "event"
        fun startActivity(context: Context, event: Event? = null) {
            context.startActivity(Intent(context, EventEditorActivity::class.java).apply { putExtra(EVENT_TO_EDIT_KEY, event) })
        }
    }

    private var eventToEdit: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_editor)
        eventToEdit = intent.getParcelableExtra(EVENT_TO_EDIT_KEY)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = when(eventToEdit) {
            null -> getString(R.string.event_editor_add_mode_title)
            else -> getString(R.string.event_editor_edit_mode_title)
        }

        val categories = arrayOf(getString(EventCategory.ART.textRes),
            getString(EventCategory.VIDEO.textRes),
            getString(EventCategory.WELLNESS.textRes),
            getString(EventCategory.FOOD.textRes),
            getString(EventCategory.CHILDREN.textRes),
            getString(EventCategory.MUSIC.textRes),
            getString(EventCategory.SEASON.textRes))
        event_spinner_category.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,
            categories).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        eventToEdit?.let {
            event_et_title.setText(it.title)
            event_et_description.setText(it.description)
            event_spinner_category.setSelection(when(it.category) {
                EventCategory.ART.name -> 0
                EventCategory.VIDEO.name -> 1
                EventCategory.WELLNESS.name -> 2
                EventCategory.FOOD.name -> 3
                EventCategory.CHILDREN.name -> 4
                EventCategory.MUSIC.name -> 5
                else -> 6
            })
            event_et_date.setText(it.timestamp.toDate().printHourMinDayMonthYear())
            event_et_image_url.setText(it.imageUrl)
            event_et_facebook_url.setText(it.facebookUrl)
            event_et_instagram_url.setText(it.instagramUrl)
            event_tv_date_repeat_label.visibility = View.GONE
            event_et_date_repeat.visibility = View.GONE
        }

        event_btn_save.setOnClickListener { handleSaveEvent() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun handleSaveEvent() {
        // check title
        val title = event_et_title.text.toString()
        if (title.isEmpty()) {
            toast(R.string.event_editor_title_error)
            return
        }
        // check timestamp
        val timestamp = try {
            val simpleFormatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
            val timestampDate = simpleFormatter.parse(event_et_date.text.toString())
            timestampDate.time
        } catch (e: Exception) {
            Timber.e(e)
            toast(R.string.event_editor_date_format_error)
            return
        }
        val category = when(event_spinner_category.selectedItemPosition) {
            0 -> EventCategory.ART
            1 -> EventCategory.VIDEO
            2 -> EventCategory.WELLNESS
            3 -> EventCategory.FOOD
            4 -> EventCategory.CHILDREN
            5 -> EventCategory.MUSIC
            else -> EventCategory.SEASON
        }

        event_form_container.fadeOut()
        event_pb_loading.fadeIn()
        val eventToEditId = eventToEdit?.id
        if (eventToEditId != null) {
            FirebaseFirestore.getInstance().collection("events")
                .document(eventToEditId)
                .update(
                    "title", title,
                    "description", event_et_description.text.toString(),
                    "imageUrl", event_et_image_url.text.toString(),
                    "facebookUrl", event_et_facebook_url.text.toString(),
                    "instagramUrl", event_et_instagram_url.text.toString(),
                    "timestamp", timestamp,
                    "category", category.name
                )
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        navigateToFinish()
                    } else {
                        event_form_container.fadeIn()
                        event_pb_loading.fadeOut()
                        toast(R.string.event_editor_save_error)
                    }
                }
        } else {
            var repeat = event_et_date_repeat.text.toString().toIntOrNull() ?: 1
            if (repeat < 1) repeat = 1
            val event = mutableMapOf<String,Any>()
            event["title"] = title
            event["description"] = event_et_description.text.toString()
            event["imageUrl"] = event_et_image_url.text.toString()
            event["facebookUrl"] = event_et_facebook_url.text.toString()
            event["instagramUrl"] = event_et_instagram_url.text.toString()
            event["timestamp"] = timestamp
            event["category"] = category.name
            createEvent(event, timestamp, repeat)
        }

    }

    private fun createEvent(event: MutableMap<String,Any>, oldtimestamp: Long, oldrepeat: Int) {
        FirebaseFirestore.getInstance().collection("events").document()
            .set(event)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val repeat = oldrepeat - 1
                    if (repeat > 0) {
                        val timestamp = oldtimestamp.toCalendar().apply { add(Calendar.DAY_OF_MONTH, 7) }.timeInMillis
                        event["timestamp"] = timestamp
                        createEvent(event, timestamp, repeat)
                    } else {
                        navigateToFinish()
                    }
                } else {
                    event_form_container.fadeIn()
                    event_pb_loading.fadeOut()
                    toast(R.string.event_editor_save_error)
                }
            }
    }

    private fun navigateToFinish() {
        finish()
    }

}
