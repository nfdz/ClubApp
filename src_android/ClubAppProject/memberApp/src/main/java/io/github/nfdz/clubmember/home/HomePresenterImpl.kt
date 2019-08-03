package io.github.nfdz.clubmember.home

import io.github.nfdz.clubcommonlibrary.EventCategory
import io.github.nfdz.clubmember.common.EventsAdapter

class HomePresenterImpl(var view: HomeView?, var interactor: HomeInteractor?) : HomePresenter {

    override fun onCreate() {
        interactor?.initialize()
        loadContent()
        updateFilter(interactor?.getSelectedFilter())
    }

    private fun loadContent() {
        interactor?.loadContent {
            if (it.isEmpty()) {
                view?.showNoContent()
            } else {
                view?.showContent(it)
            }
        }
    }

    private fun updateFilter(filter: HomeFilter?) {
        if (filter == null) {
            view?.showNoFilter()
        } else {
            view?.showFilter(filter)
        }
    }

    override fun onDestroy() {
        view = null
        interactor?.destroy()
        interactor = null
    }

    override fun onRefreshClick() {
        interactor?.syncAllData({
            view?.hideRefresh()
        }, { isDisabled ->
            if (isDisabled) view?.showDisabledUserMsg()
            view?.navigateToLogin()
        })
    }

    override fun onChangeFilterClick(category: EventCategory) {
        interactor?.changeFilter(category) {
            loadContent()
            updateFilter(interactor?.getSelectedFilter())
        }
    }

    override fun onFilterClick() {
        view?.navigateToChooseFilter()
    }

    override fun onRemoveFilterClick() {
        interactor?.clearSelectedFilter {
            loadContent()
            updateFilter(interactor?.getSelectedFilter())
        }
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        view?.navigateToEvent(event.id)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        view?.navigateToConfirmAttendance(event.id)
    }

    override fun onDataChangeEvent() {
        loadContent()
    }

    override fun onBackClick(): Boolean {
        val filterEnabled = interactor?.getSelectedFilter() != null
        return if (filterEnabled) {
            onRemoveFilterClick()
            true
        } else {
            false
        }
    }

}