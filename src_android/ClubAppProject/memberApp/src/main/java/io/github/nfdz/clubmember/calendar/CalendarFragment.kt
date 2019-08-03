package io.github.nfdz.clubmember.calendar

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.applandeo.materialcalendarview.EventDay
import io.github.nfdz.clubcommonlibrary.printDayMonthYear
import io.github.nfdz.clubcommonlibrary.toCalendar
import io.github.nfdz.clubcommonlibrary.toDate
import io.github.nfdz.clubcommonlibrary.toast
import io.github.nfdz.clubmember.common.BackClickHandler
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.confirm.ConfirmAttendanceActivity
import io.github.nfdz.clubmember.event.EventActivity
import io.github.nfdz.clubmember.splashscreen.SplashScreenActivity
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.fragment_calendar.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber
import java.util.*


class CalendarFragment : Fragment(), CalendarView, EventsAdapter.Listener, BackClickHandler {

    companion object {
        private const val SELECTED_DAY_STATE_KEY = "selected_day"
        @JvmStatic
        fun newInstance(): CalendarFragment {
            return CalendarFragment()
        }
    }

    private val presenter: CalendarPresenter by lazy { CalendarPresenterImpl(this, activity?.let { CalendarInteractorImpl(it) }) }
    private val adapter = EventsAdapter(smoothDataChanges = false, listener = this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        presenter.onCreate(savedInstanceState?.getLong(SELECTED_DAY_STATE_KEY))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        presenter.getSelectedDayTime()?.let { outState.putLong(SELECTED_DAY_STATE_KEY, it) }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        presenter.onDestroy()
        super.onDestroyView()
    }

    private fun setupView() {
        calendar_rv_day.adapter = adapter
        calendar_srl_refresh.setOnRefreshListener { presenter.onRefreshClick() }
        calendar_iv_day_unselect.setOnClickListener { presenter.onUnselectDayClick() }
        calendar_cv.setOnDayClickListener { presenter.onSelectDayClick(it.calendar) }
        calendar_cv.showCurrentMonthPage()
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        presenter.onEventClick(event)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        presenter.onConfirmAttendanceClick(event)
    }

    override fun setCalendarContent(events: List<CalendarEvent>) {
        val firstDay = events.minBy { it.day }
        val lastDay = events.maxBy { it.day }
        val today = System.currentTimeMillis().toCalendar()
        calendar_cv.setMinimumDate(firstDay?.day ?: today)
        calendar_cv.setMaximumDate(lastDay?.day ?: today)
        calendar_cv.setEvents(events.map { EventDay(it.day, getCalendarEventDrawable(it.color)) })
    }

    private fun getCalendarEventDrawable(color: Int): Drawable? {
        val drawable = context?.let { ContextCompat.getDrawable(it, R.drawable.ic_dot) }
        drawable?.let { DrawableCompat.setTint(it, color) }
        return drawable
    }

    override fun showDayContent(day: Calendar, items: List<EventsAdapter.EventEntry>) {
        adapter.data = items
        calendar_tv_day.text = day.time.printDayMonthYear()
        calendar_tv_day.visibility = View.VISIBLE
        calendar_iv_day_unselect.visibility = View.VISIBLE
        calendar_srl_refresh.visibility = View.VISIBLE
        calendar_cv.visibility = View.GONE
    }

    override fun hideDayContent() {
        adapter.data = emptyList()
        calendar_tv_day.text = ""
        calendar_tv_day.visibility = View.GONE
        calendar_iv_day_unselect.visibility = View.GONE
        calendar_srl_refresh.visibility = View.GONE
        calendar_cv.visibility = View.VISIBLE
    }

    override fun hideRefresh() {
        calendar_srl_refresh.isRefreshing = false
    }

    override fun showDisabledUserMsg() {
        context?.toast(R.string.disabled_user_error)
    }

    override fun navigateToLogin() {
        activity?.let { SplashScreenActivity.startActivity(it) }
    }

    override fun navigateToEvent(eventId: String) {
        context?.let { EventActivity.startActivity(it, eventId) }
    }

    override fun navigateToConfirmAttendance(eventId: String) {
        context?.let { ConfirmAttendanceActivity.startActivity(it, eventId) }
    }

    override fun onBackClick(): Boolean {
        return if (isResumed && !isDetached) {
            presenter.onBackClick()
        } else {
            false
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangeEvent(event: DataChangeEvent) {
        Timber.d("DataChangeEvent")
        presenter.onDataChangeEvent()
    }

}