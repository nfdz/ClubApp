package io.github.nfdz.clubmember.calendar

import android.app.Activity
import androidx.core.content.ContextCompat
import com.vicpin.krealmextensions.querySorted
import io.github.nfdz.clubcommonlibrary.EventCategory
import io.github.nfdz.clubcommonlibrary.setFirstHourOfDay
import io.github.nfdz.clubcommonlibrary.setLastHourOfDay
import io.github.nfdz.clubcommonlibrary.toCalendar
import io.github.nfdz.clubmember.common.EventRealm
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.common.isEventConfirmed
import io.github.nfdz.clubmember.common.syncAllDataFromFirebase
import io.realm.Sort
import java.util.*

class CalendarInteractorImpl(val activity: Activity) : CalendarInteractor {

    override fun initialize() {
    }

    override fun destroy() {
    }

    override fun loadDayData(day: Calendar, callback: (List<EventsAdapter.EventEntry>) -> Unit) {
        callback(EventRealm().querySorted("timestamp", Sort.ASCENDING) {
            between("timestamp", day.setFirstHourOfDay().timeInMillis, day.setLastHourOfDay().timeInMillis)
        } .asSequence()
            .filter { it.isLoaded && it.isValid && !isEventConfirmed(it.id) }
            .map { EventsAdapter.EventEntry.buildFromEvent(it) }
            .toList())
    }

    override fun loadCalendarData(callback: (List<CalendarEvent>) -> Unit) {
        callback(EventRealm().querySorted("timestamp", Sort.ASCENDING).asSequence()
            .filter { it.isLoaded && it.isValid && !isEventConfirmed(it.id) }
            .map { mapEvent(it) }
            .toList())
    }

    private fun mapEvent(event: EventRealm) = CalendarEvent(event.timestamp.toCalendar(),
        when(EventCategory.valueOf(event.category)) {
            EventCategory.ART -> ContextCompat.getColor(activity, EventCategory.ART.colorRes)
            EventCategory.VIDEO -> ContextCompat.getColor(activity, EventCategory.VIDEO.colorRes)
            EventCategory.WELLNESS -> ContextCompat.getColor(activity, EventCategory.WELLNESS.colorRes)
            EventCategory.FOOD -> ContextCompat.getColor(activity, EventCategory.FOOD.colorRes)
            EventCategory.CHILDREN -> ContextCompat.getColor(activity, EventCategory.CHILDREN.colorRes)
            EventCategory.MUSIC -> ContextCompat.getColor(activity, EventCategory.MUSIC.colorRes)
            EventCategory.SEASON -> ContextCompat.getColor(activity, EventCategory.SEASON.colorRes)
        })

    override fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit) {
        syncAllDataFromFirebase(successCallback, userErrorStateCallback)
    }

}