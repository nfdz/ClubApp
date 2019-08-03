package io.github.nfdz.clubmember.home

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import io.github.nfdz.clubcommonlibrary.EventCategory
import io.github.nfdz.clubmember.common.EventsAdapter

interface HomeView {
    fun showContent(items: List<EventsAdapter.EventEntry>)
    fun showNoContent()
    fun showFilter(filter: HomeFilter)
    fun showNoFilter()
    fun hideRefresh()
    fun showDisabledUserMsg()
    fun navigateToLogin()
    fun navigateToChooseFilter()
    fun navigateToEvent(eventId: String)
    fun navigateToConfirmAttendance(eventId: String)
}

interface HomePresenter {
    fun onCreate()
    fun onDestroy()
    fun onRefreshClick()
    fun onChangeFilterClick(category: EventCategory)
    fun onFilterClick()
    fun onRemoveFilterClick()
    fun onEventClick(event: EventsAdapter.EventEntry)
    fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry)
    fun onDataChangeEvent()
    fun onBackClick(): Boolean
}

interface HomeInteractor {
    fun initialize()
    fun destroy()
    fun loadContent(callback: (List<EventsAdapter.EventEntry>) -> Unit)
    fun getSelectedFilter(): HomeFilter?
    fun clearSelectedFilter(callback: () -> Unit)
    fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit)
    fun changeFilter(category: EventCategory, callback: () -> Unit)
}

data class HomeFilter(@StringRes val filterTextRes: Int, @ColorRes val filterColor: Int)
