package io.github.nfdz.clubmember.bookings

import android.app.Activity
import com.vicpin.krealmextensions.querySorted
import io.github.nfdz.clubmember.common.EventRealm
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.common.isEventConfirmed
import io.github.nfdz.clubmember.common.syncAllDataFromFirebase
import io.realm.Sort

class BookingsInteractorImpl(val activity: Activity) : BookingsInteractor {

    override fun initialize() {
    }

    override fun destroy() {
    }

    override fun loadData(callback: (List<EventsAdapter.EventEntry>) -> Unit) {
        callback(EventRealm().querySorted("timestamp", Sort.ASCENDING).asSequence()
            .filter { it.isLoaded && it.isValid && !isEventConfirmed(it.id) }
            .map { EventsAdapter.EventEntry.buildFromEvent(it) }
            .filter { it.attendanceFlag }
            .toList())
    }

    override fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit) {
        syncAllDataFromFirebase(successCallback, userErrorStateCallback)
    }

}