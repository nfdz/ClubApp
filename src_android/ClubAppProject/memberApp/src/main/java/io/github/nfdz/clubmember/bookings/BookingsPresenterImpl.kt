package io.github.nfdz.clubmember.bookings

import io.github.nfdz.clubmember.common.EventsAdapter

class BookingsPresenterImpl(var view: BookingsView?, var interactor: BookingsInteractor?) : BookingsPresenter {

    override fun onCreate() {
        interactor?.initialize()
        interactor?.loadData { view?.setContent(it) }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onDataChangeEvent() {
        interactor?.loadData { view?.setContent(it) }
    }

    override fun onRefreshClick() {
        interactor?.syncAllData({
            view?.hideRefresh()
        }, { isDisabled ->
            if (isDisabled) view?.showDisabledUserMsg()
            view?.navigateToLogin()
        })
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        view?.navigateToEvent(event.id)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        view?.navigateToConfirmAttendance(event.id)
    }

}