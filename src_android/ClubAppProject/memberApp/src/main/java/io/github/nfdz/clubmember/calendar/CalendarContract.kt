package io.github.nfdz.clubmember.calendar

import io.github.nfdz.clubmember.common.EventsAdapter
import java.util.*

interface CalendarView {
    fun setCalendarContent(events: List<CalendarEvent>)
    fun showDayContent(day: Calendar, items: List<EventsAdapter.EventEntry>)
    fun hideDayContent()
    fun hideRefresh()
    fun showDisabledUserMsg()
    fun navigateToLogin()
    fun navigateToEvent(eventId: String)
    fun navigateToConfirmAttendance(eventId: String)
}

interface CalendarPresenter {
    fun onCreate(selectedDayTime: Long? = null)
    fun onDestroy()
    fun getSelectedDayTime(): Long?
    fun onDataChangeEvent()
    fun onRefreshClick()
    fun onUnselectDayClick()
    fun onSelectDayClick(day: Calendar)
    fun onEventClick(event: EventsAdapter.EventEntry)
    fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry)
    fun onBackClick(): Boolean
}

interface CalendarInteractor {
    fun initialize()
    fun destroy()
    fun loadDayData(day: Calendar, callback: (List<EventsAdapter.EventEntry>) -> Unit)
    fun loadCalendarData(callback: (List<CalendarEvent>) -> Unit)
    fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit)
}

data class CalendarEvent(val day: Calendar, val color: Int)
