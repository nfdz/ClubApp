package io.github.nfdz.clubmember.common

import com.vicpin.krealmextensions.deleteAll
import com.vicpin.krealmextensions.executeTransaction
import com.vicpin.krealmextensions.queryFirst
import io.github.nfdz.clubcommonlibrary.toCalendar
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass
open class EventRealm(@PrimaryKey var id: String = "",
                      var title: String = "",
                      var description: String = "",
                      var imageUrl: String = "",
                      var facebookUrl: String = "",
                      var instagramUrl: String = "",
                      var category: String = "",
                      var timestamp: Long = 0) : RealmObject()


@RealmClass
open class BookedEventRealm(@PrimaryKey var id: String = "") : RealmObject()

@RealmClass
open class AttendedEventRealm(@PrimaryKey var id: String = "") : RealmObject()

@RealmClass
open class UserRealm(@PrimaryKey var email: String = "",
                     var alias: String = "",
                     var fullName: String = "",
                     var address: String = "",
                     var phoneNumber: String = "",
                     var birthday: String = "",
                     var signUpTimestamp: Long = 0,
                     var points: Int = 0,
                     var highlightChatFlag: Boolean = false,
                     var isDisabled: Boolean = false) : RealmObject()

@RealmClass
open class ChatMessageRealm(@PrimaryKey var timestamp: Long = 0,
                            var authorEmail: String = "",
                            var authorAlias: String = "",
                            var highlight: Boolean = false,
                            var text: String = "") : RealmObject()

const val LIMIT_CHAT_MESSAGES_STORAGE = 800
const val MARGIN_TO_CONFIRM_IN_HOURS = 6
const val AUTO_SYNC_ALL_MARGIN_IN_HOURS = 24L

fun isAvailableQR(timestamp: Long): Boolean {
    val from = timestamp.toCalendar().apply { add(Calendar.HOUR_OF_DAY, -6) }.timeInMillis
    val to = timestamp.toCalendar().apply { add(Calendar.HOUR_OF_DAY, 6) }.timeInMillis
    val now = System.currentTimeMillis()
    return now > from && now < to
}
fun isAttendanceModifiable(timestamp: Long): Boolean {
    val nowTomorrow = System.currentTimeMillis().toCalendar().apply {
        add(Calendar.DAY_OF_MONTH, 1)
    }
    return timestamp > nowTomorrow.timeInMillis
}

fun isEventConfirmed(eventId: String): Boolean {
    val entry = AttendedEventRealm().queryFirst { equalTo("id", eventId) }
    return entry != null && entry.isValid
}

fun isGoingToEvent(eventId: String): Boolean {
    val entry = BookedEventRealm().queryFirst { equalTo("id", eventId) }
    return entry != null && entry.isValid
}

fun clearDataModelPersistence() {
    executeTransaction {
        EventRealm().deleteAll()
        AttendedEventRealm().deleteAll()
        BookedEventRealm().deleteAll()
        UserRealm().deleteAll()
        ChatMessageRealm().deleteAll()
    }
}