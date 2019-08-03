package io.github.nfdz.clubmember.event

import YOUR.MEMBER.APP.ID.HERE.R

class EventPresenterImpl(var view: EventView?, var interactor: EventInteractor?) : EventPresenter {

    override fun onCreate(eventId: String) {
        interactor?.initialize(eventId)
        if (true == interactor?.isEventValid()) {
            interactor?.getEventInfo()?.let { view?.showEventInfo(it) }
            refreshAttendanceView()
        } else {
            view?.navigateToInvalidEvent()
        }
    }

    private fun refreshAttendanceView() {
        interactor?.getAttendanceStatus { status, _ ->
            when (status) {
                AttendanceActionStatus.AVAILABLE_JOIN -> view?.showButtonJoinMode()
                AttendanceActionStatus.AVAILABLE_CANCEL -> view?.showButtonCancelMode()
                AttendanceActionStatus.AVAILABLE_SCAN -> view?.showButtonQRMode()
                AttendanceActionStatus.JOINED_LOCKED -> view?.showButtonUnavailableMode(R.string.event_attendance_unavailable_change_in)
                AttendanceActionStatus.NOT_JOINED_LOCKED -> view?.showButtonUnavailableMode(R.string.event_attendance_unavailable_change)
                AttendanceActionStatus.CONFIRMED -> view?.showButtonUnavailableMode(R.string.event_attendance_unavailable_confirmed)
            }
        }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onShareClick() {
        interactor?.getShareText()?.let { view?.navigateToShare(it) }
    }

    override fun onFacebookClick() {
        interactor?.getFacebookUrl()?.let { view?.navigateToFacebook(it) }
    }

    override fun onInstagramClick() {
        interactor?.getInstagramUrl()?.let { view?.navigateToInstragram(it) }
    }

    override fun onAttendanceClick() {
        interactor?.getAttendanceStatus { status, eventId ->
            when (status) {
                AttendanceActionStatus.AVAILABLE_JOIN -> view?.askWantToGoConfirmation()
                AttendanceActionStatus.AVAILABLE_CANCEL -> view?.askCancelConfirmation()
                AttendanceActionStatus.AVAILABLE_SCAN -> view?.navigateToConfirmAttendance(eventId)
                AttendanceActionStatus.JOINED_LOCKED -> view?.showUnavailableActionMessage()
                AttendanceActionStatus.NOT_JOINED_LOCKED -> view?.showUnavailableActionMessage()
                AttendanceActionStatus.CONFIRMED -> view?.showAlreadyConfirmedMessage()
            }
        }
    }

    override fun onWantToGoConfirmation() {
        view?.showLoading()
        interactor?.joinToEvent({
            view?.hideLoading()
            refreshAttendanceView()
        }, {
            view?.hideLoading()
            view?.showSignUpErrorMessage()
        })
    }

    override fun onCancelConfirmation() {
        view?.showLoading()
        interactor?.cancelEvent({
            view?.hideLoading()
            refreshAttendanceView()
        }, {
            view?.hideLoading()
            view?.showCancelErrorMessage()
        })
    }

    override fun onAddEventToCalendarClick() {
        interactor?.getEventInfo()?.let { view?.navigateToAddToCalendar(it) }
    }

    override fun onDataChangeEvent() {
        refreshAttendanceView()
    }

}