package io.github.nfdz.clubadmin.common

import android.os.Parcel
import android.os.Parcelable

data class Event(val id: String,
                 val title: String,
                 val description: String,
                 val imageUrl: String,
                 val facebookUrl: String,
                 val instagramUrl: String,
                 val category: String,
                 val timestamp: Long) : Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeString(title)
        dest?.writeString(description)
        dest?.writeString(imageUrl)
        dest?.writeString(facebookUrl)
        dest?.writeString(instagramUrl)
        dest?.writeString(category)
        dest?.writeLong(timestamp)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Event> {
        override fun createFromParcel(parcel: Parcel): Event {
            return Event(parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readLong())
        }

        override fun newArray(size: Int): Array<Event?> {
            return arrayOfNulls(size)
        }
    }

}

data class UserBooking(val eventId: String,
                       val userEmail: String)

data class UserConfirmation(val eventId: String,
                            val userEmail: String)

data class QRCode(val id: String, val code: String)

data class User(val id: String,
                val email: String,
                val alias: String,
                val fullName: String,
                val address: String,
                val phoneNumber: String,
                val birthday: String,
                val signUpTimestamp: Long,
                val points: Int,
                val highlightChatFlag: Boolean,
                val isDisabled: Boolean) : Parcelable {

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeString(id)
        dest?.writeString(email)
        dest?.writeString(alias)
        dest?.writeString(fullName)
        dest?.writeString(address)
        dest?.writeString(phoneNumber)
        dest?.writeString(birthday)
        dest?.writeLong(signUpTimestamp)
        dest?.writeInt(points)
        dest?.writeInt(if (highlightChatFlag) 1 else 0)
        dest?.writeInt(if (isDisabled) 1 else 0)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readLong(),
                parcel.readInt(),
                parcel.readInt() == 1,
                parcel.readInt() == 1)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

const val ADMIN_EMAIL_KEY = "admin_email"
const val ADMIN_PASS_KEY = "admin_pass"