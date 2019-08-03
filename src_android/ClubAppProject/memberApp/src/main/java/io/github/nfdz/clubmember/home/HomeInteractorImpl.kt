package io.github.nfdz.clubmember.home

import android.app.Activity
import com.vicpin.krealmextensions.querySorted
import io.github.nfdz.clubcommonlibrary.*
import io.github.nfdz.clubmember.common.EventRealm
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.common.isEventConfirmed
import io.github.nfdz.clubmember.common.syncAllDataFromFirebase
import YOUR.MEMBER.APP.ID.HERE.R
import io.realm.Sort

class HomeInteractorImpl(val activity: Activity) : HomeInteractor {

    override fun initialize() {
    }

    override fun destroy() {
    }

    override fun loadContent(callback: (List<EventsAdapter.EventEntry>) -> Unit) {
        val filterByCategory = try {
            EventCategory.valueOf(activity.getStringFromPreferences(R.string.category_preference_key, R.string.category_preference_default))
        } catch (e: Exception) {
            null
        }
        callback(
            if (filterByCategory == null) {
                EventRealm().querySorted("timestamp", Sort.ASCENDING).asSequence()
                    .filter { it.isLoaded && it.isValid && !isEventConfirmed(it.id) }
                    .map { EventsAdapter.EventEntry.buildFromEvent(it) }
                    .toList()
            } else {
                EventRealm().querySorted("timestamp", Sort.ASCENDING).asSequence()
                    .filter { it.isLoaded && it.isValid && it.category == filterByCategory.name && !isEventConfirmed(it.id) }
                    .map { EventsAdapter.EventEntry.buildFromEvent(it) }
                    .toList()
            }
        )
    }

    override fun getSelectedFilter(): HomeFilter? {
        return try {
            val category = EventCategory.valueOf(activity.getStringFromPreferences(R.string.category_preference_key, R.string.category_preference_default))
            HomeFilter(category.textRes, category.colorRes)
        } catch (e: Exception) {
            null
        }
    }

    override fun clearSelectedFilter(callback: () -> Unit) {
        doAsync {
            activity.setStringInPreferences(R.string.category_preference_key, "")
            doMainThread {
                callback()
            }
        }
    }

    override fun syncAllData(successCallback: () -> Unit, userErrorStateCallback: (isDisabled: Boolean) -> Unit) {
        syncAllDataFromFirebase(successCallback, userErrorStateCallback)
    }

    override fun changeFilter(category: EventCategory, callback: () -> Unit) {
        doAsync {
            activity.setStringInPreferences(R.string.category_preference_key, category.name)
            doMainThread {
                callback()
            }
        }
    }

}