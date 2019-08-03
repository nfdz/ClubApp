package io.github.nfdz.clubmember.bookings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import io.github.nfdz.clubcommonlibrary.toast
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.confirm.ConfirmAttendanceActivity
import io.github.nfdz.clubmember.event.EventActivity
import io.github.nfdz.clubmember.splashscreen.SplashScreenActivity
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_bookings.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

class BookingsActivity : AppCompatActivity(), BookingsView, EventsAdapter.Listener {

    companion object {
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, BookingsActivity::class.java))
        }
    }

    private val presenter: BookingsPresenter by lazy { BookingsPresenterImpl(this, BookingsInteractorImpl(this)) }
    private val adapter = EventsAdapter(listener = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        presenter.onCreate()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_bookings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bookings_srl_refresh.setOnRefreshListener { presenter.onRefreshClick() }
        bookings_rv.adapter = adapter
    }

    override fun setContent(items: List<EventsAdapter.EventEntry>) {
        adapter.data = items
    }

    override fun hideRefresh() {
        bookings_srl_refresh.isRefreshing = false
    }

    override fun showDisabledUserMsg() {
        toast(R.string.disabled_user_error)
    }

    override fun navigateToLogin() {
        SplashScreenActivity.startActivity(this)
    }

    override fun navigateToEvent(eventId: String) {
        EventActivity.startActivity(this, eventId)
    }

    override fun navigateToConfirmAttendance(eventId: String) {
        ConfirmAttendanceActivity.startActivity(this, eventId)
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        presenter.onEventClick(event)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        presenter.onConfirmAttendanceClick(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangeEvent(event: DataChangeEvent) {
        Timber.d("DataChangeEvent")
        presenter.onDataChangeEvent()
    }

}
