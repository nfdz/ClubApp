package io.github.nfdz.clubmember.calendar

import io.github.nfdz.clubcommonlibrary.toCalendar
import io.github.nfdz.clubmember.common.EventsAdapter
import java.util.*

class CalendarPresenterImpl(var view: CalendarView?, var interactor: CalendarInteractor?) : CalendarPresenter {

    private var selectedDay: Calendar? = null

    override fun onCreate(selectedDayTime: Long?) {
        this.selectedDay = selectedDayTime?.toCalendar()
        interactor?.initialize()
        updateCalendarData()
        updateDayData(selectedDay)
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun getSelectedDayTime(): Long? {
        return selectedDay?.timeInMillis
    }

    private fun updateCalendarData() {
        interactor?.loadCalendarData {
            view?.setCalendarContent(it)
        }
    }

    private fun updateDayData(day: Calendar?) {
        if (day != null) {
            interactor?.loadDayData(day) {
                if (it.isEmpty()) {
                    view?.hideDayContent()
                } else {
                    view?.showDayContent(day, it)
                }
            }
        } else {
            view?.hideDayContent()
        }
    }

    override fun onDataChangeEvent() {
        updateCalendarData()
        updateDayData(selectedDay)
    }

    override fun onRefreshClick() {
        interactor?.syncAllData({
            view?.hideRefresh()
        }, { isDisabled ->
            if (isDisabled) view?.showDisabledUserMsg()
            view?.navigateToLogin()
        })
    }

    override fun onUnselectDayClick() {
        selectedDay = null
        updateDayData(selectedDay)
    }

    override fun onSelectDayClick(day: Calendar) {
        selectedDay = day
        updateDayData(selectedDay)
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        view?.navigateToEvent(event.id)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        view?.navigateToConfirmAttendance(event.id)
    }

    override fun onBackClick(): Boolean {
        return if (selectedDay != null) {
            selectedDay = null
            updateDayData(selectedDay)
            true
        } else {
            false
        }
    }

}