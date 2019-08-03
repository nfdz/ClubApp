package io.github.nfdz.clubmember.event

import androidx.annotation.StringRes

interface EventView {
    fun showEventInfo(info: EventInfo)
    fun showButtonJoinMode()
    fun showButtonCancelMode()
    fun showButtonUnavailableMode(@StringRes textRes: Int)
    fun showButtonQRMode()
    fun showLoading()
    fun hideLoading()
    fun askWantToGoConfirmation()
    fun askCancelConfirmation()
    fun showUnavailableActionMessage()
    fun showAlreadyConfirmedMessage()
    fun showSignUpErrorMessage()
    fun showCancelErrorMessage()
    fun navigateToShare(text: String)
    fun navigateToFacebook(facebookUrl: String)
    fun navigateToInstragram(instagramUrl: String)
    fun navigateToConfirmAttendance(eventId: String)
    fun navigateToAddToCalendar(info: EventInfo)
    fun navigateToInvalidEvent()
}

interface EventPresenter {
    fun onCreate(eventId: String)
    fun onDestroy()
    fun onShareClick()
    fun onFacebookClick()
    fun onInstagramClick()
    fun onAttendanceClick()
    fun onWantToGoConfirmation()
    fun onCancelConfirmation()
    fun onAddEventToCalendarClick()
    fun onDataChangeEvent()
}

interface EventInteractor {
    fun initialize(eventId: String)
    fun destroy()
    fun isEventValid(): Boolean
    fun getEventInfo(): EventInfo
    fun getEventId(): String
    fun getAttendanceStatus(callback: (status: AttendanceActionStatus, eventId: String) -> Unit)
    fun joinToEvent(successCallback: () -> Unit, errorCallback: () -> Unit)
    fun cancelEvent(successCallback: () -> Unit, errorCallback: () -> Unit)
    fun getShareText(): String
    fun getFacebookUrl(): String
    fun getInstagramUrl(): String
}

data class EventInfo(val title: String,
                     val description: String,
                     val imageUrl: String,
                     val facebookUrl: String,
                     val instagramUrl: String,
                     val timestamp: Long)

enum class AttendanceActionStatus {
    AVAILABLE_JOIN,
    AVAILABLE_CANCEL,
    AVAILABLE_SCAN,
    JOINED_LOCKED,
    NOT_JOINED_LOCKED,
    CONFIRMED
}