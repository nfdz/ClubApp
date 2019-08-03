package io.github.nfdz.clubmember.event

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.github.nfdz.clubcommonlibrary.*
import io.github.nfdz.clubmember.common.AnalyticsEvent
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.common.logAnalytics
import io.github.nfdz.clubmember.confirm.ConfirmAttendanceActivity
import io.github.nfdz.clubmember.main.MainActivity
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_event.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*


class EventActivity : AppCompatActivity(), EventView {

    companion object {
        private const val EVENT_ID_EXTRA = "event_id"
        @JvmStatic
        fun startActivity(context: Context, eventId: String) {
            context.startActivity(Intent(context, EventActivity::class.java).apply { putExtra(EVENT_ID_EXTRA, eventId) })
        }
    }

    private val presenter: EventPresenter by lazy { EventPresenterImpl(this, EventInteractorImpl(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        val eventId = intent.getStringExtra(EVENT_ID_EXTRA) ?: ""
        if (eventId.isEmpty()) {
            finish()
        } else {
            presenter.onCreate(eventId)
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun setupView() {
        setContentView(R.layout.activity_event)
        setSupportActionBar(event_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        event_iv_share.setOnClickListener { presenter.onShareClick() }
        event_iv_instagram.setOnClickListener { presenter.onInstagramClick() }
        event_iv_facebook.setOnClickListener { presenter.onFacebookClick() }
        event_btn_attendance.setOnClickListener { presenter.onAttendanceClick() }
        event_tv_description.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.event, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                MainActivity.startActivity(this)
                finish()
                true
            }
            R.id.event_add_calendar -> { presenter.onAddEventToCalendarClick(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    override fun showEventInfo(info: EventInfo) {
        Glide.with(this)
            .setDefaultRequestOptions(RequestOptions().placeholder(R.drawable.image_empty_event).error(R.drawable.image_empty_event))
            .load(info.imageUrl)
            .into(event_iv_image)
        event_tv_date.text = info.timestamp.toDate().printHourMinDayMonthYear()
        event_tv_title.text = info.title
        event_tv_description.text = info.description
        event_iv_instagram.visibility = if (info.instagramUrl.isEmpty()) View.GONE else View.VISIBLE
        event_iv_facebook.visibility = if (info.facebookUrl.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun showButtonJoinMode() {
        event_btn_attendance.setText(R.string.event_attendance_go)
        event_btn_attendance.setBackgroundColor(ContextCompat.getColor(this, R.color.eventGoColor))
    }

    override fun showButtonCancelMode() {
        event_btn_attendance.setText(R.string.event_attendance_cancel)
        event_btn_attendance.setBackgroundColor(ContextCompat.getColor(this, R.color.eventCancelColor))
    }

    override fun showButtonUnavailableMode(@StringRes textRes: Int) {
        event_btn_attendance.setBackgroundColor(ContextCompat.getColor(this, R.color.eventUnavailableColor))
        event_btn_attendance.setText(textRes)
    }

    override fun showButtonQRMode() {
        event_btn_attendance.setBackgroundColor(ContextCompat.getColor(this, R.color.dark))
        event_btn_attendance.setText(R.string.event_attendance_qr)
    }

    override fun showLoading() {
        event_container_loading.fadeIn()
        event_btn_attendance.fadeOut()
    }

    override fun hideLoading() {
        event_container_loading.fadeOut()
        event_btn_attendance.fadeIn()
    }

    override fun askWantToGoConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.event_confirmation_title)
            setMessage(R.string.event_confirmation_want_to_go_text)
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            logAnalytics(AnalyticsEvent.JOIN_EVENT)
            presenter.onWantToGoConfirmation()
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    override fun askCancelConfirmation() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.event_confirmation_title)
            setMessage(R.string.event_confirmation_cancel_text)
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            logAnalytics(AnalyticsEvent.CANCEL_EVENT)
            presenter.onCancelConfirmation()
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    override fun showUnavailableActionMessage() {
        event_container.showSnackbar(getString(R.string.event_unavailable_change_attendance))
    }

    override fun showAlreadyConfirmedMessage() {
        event_container.showSnackbar(getString(R.string.event_already_confirmed))
    }

    override fun showSignUpErrorMessage() {
        event_container.showSnackbar(getString(R.string.event_sign_up_error))
    }

    override fun showCancelErrorMessage() {
        event_container.showSnackbar(getString(R.string.event_cancel_error))
    }

    override fun navigateToShare(text: String) {
        logAnalytics(AnalyticsEvent.SHARE_EVENT)
        try {
            val sendIntent: Intent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            startActivity(sendIntent)
        } catch (e: Exception) {
            reportException(e)
            event_container.showSnackbar(getString(R.string.event_share_error))
        }
    }

    override fun navigateToFacebook(facebookUrl: String) {
        logAnalytics(AnalyticsEvent.FACEBOOK_EVENT)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)))
        } catch (e: Exception) {
            reportException(e)
            event_container.showSnackbar(getString(R.string.event_facebook_error))
        }
    }

    override fun navigateToInstragram(instagramUrl: String) {
        logAnalytics(AnalyticsEvent.INSTAGRAM_EVENT)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(instagramUrl)))
        } catch (e: Exception) {
            reportException(e)
            event_container.showSnackbar(getString(R.string.event_instagram_error))
        }
    }

    override fun navigateToConfirmAttendance(eventId: String) {
        ConfirmAttendanceActivity.startActivity(this, eventId)
    }

    override fun navigateToAddToCalendar(info: EventInfo) {
        try {
            val beginTimeMillis = info.timestamp
            val endTimeMillis = beginTimeMillis.toCalendar().apply { add(Calendar.HOUR_OF_DAY, 1) }.timeInMillis
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = Events.CONTENT_URI
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTimeMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTimeMillis)
                putExtra(Events.TITLE, info.title)
                putExtra(Events.DESCRIPTION, info.description)
                putExtra(Events.EVENT_LOCATION, getString(R.string.club_location_uri_query))
                putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e)
            event_container.showSnackbar(getString(R.string.event_add_calendar_error))
        }
    }

    override fun navigateToInvalidEvent() {
        toast(R.string.event_invalid)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangeEvent(event: DataChangeEvent) {
        Timber.d("DataChangeEvent")
        presenter.onDataChangeEvent()
    }

}
