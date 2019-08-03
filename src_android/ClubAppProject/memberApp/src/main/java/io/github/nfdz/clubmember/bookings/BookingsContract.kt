package io.github.nfdz.clubmember.bookings

import io.github.nfdz.clubmember.common.EventsAdapter

interface BookingsView {
    fun setContent(items: List<EventsAdapter.EventEntry>)
    fun hideRefresh()
    fun showDisabledUserMsg()
    fun navigateToLogin()
    fun navigateToEvent(eventId: String)
    fun navigateToConfirmAttendance(eventId: String)
}

interface BookingsPresenter {
    fun onCreate()
    fun onDestroy()
    fun onDataChangeEvent()
    fun onRefreshClick()
    fun onEventClick(event: EventsAdapter.EventEntry)
    fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry)
}

interface BookingsInteractor {
    fun initialize()
    fun destroy()
    fun loadData(callback: (List<EventsAdapter.EventEntry>) -> Unit)
    fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit)
}