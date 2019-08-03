package io.github.nfdz.clubmember.common

import android.content.Context
import androidx.annotation.Size
import com.google.firebase.analytics.FirebaseAnalytics
import YOUR.MEMBER.APP.ID.HERE.BuildConfig
import timber.log.Timber

fun Context.logAnalytics(@Size(min = 1L,max = 40L) event: String) {
    if (BuildConfig.DEBUG) {
        Timber.i("AnalyticsDebug: $event")
    } else {
        FirebaseAnalytics.getInstance(this).logEvent(event, null)
    }
}

class AnalyticsEvent {
    companion object {
        const val FACEBOOK_EVENT = "FACEBOOK_EVENT"
        const val INSTAGRAM_EVENT = "INSTAGRAM_EVENT"
        const val SHARE_EVENT = "SHARE_EVENT"
        const val FACEBOOK_CLUB = "FACEBOOK_CLUB"
        const val INSTAGRAM_CLUB = "INSTAGRAM_CLUB"
        const val JOIN_EVENT = "JOIN_EVENT"
        const val CANCEL_EVENT = "CANCEL_EVENT"
    }
}