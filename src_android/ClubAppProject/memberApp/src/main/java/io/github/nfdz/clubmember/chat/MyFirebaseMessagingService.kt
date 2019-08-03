package io.github.nfdz.clubmember.chat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.nfdz.clubmember.common.syncChatFromFirebase
import io.github.nfdz.clubmember.main.MainActivity
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import timber.log.Timber
import java.util.*


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: ""
        val body = remoteMessage.notification?.body ?: ""
        if (title.isEmpty() || body.isEmpty()) {
            reportException(IllegalArgumentException("Received notification with empty payload"))
            return
        }
        Timber.d("Message received: from=${remoteMessage.from}, title=$title, body=$body")
        if (remoteMessage.data["pushType"] == "chat") {
            syncChatFromFirebase({ anyNewMessages ->
                if (!CHAT_FOREGROUND_STATE && anyNewMessages) {
                    createNotificationChannel()
                    NotificationManagerCompat.from(this).notify(getRandomId(), createNotification(title, body, true))
                }
            }, {
                Timber.d("Skip push message")
            })
        } else {
            NotificationManagerCompat.from(this).notify(getRandomId(), createNotification(title, body, false))
        }
    }

    private fun getRandomId() = Random().nextInt(100000) + 1000

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.notification_channel_title)
            val description = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(getString(R.string.notification_channel_id), name, importance)
            channel.description = description
            channel.enableVibration(true)
            channel.enableLights(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, body: String, chatFlag: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
        builder.setContentTitle(title)
        builder.setContentText(body)
        builder.setSmallIcon(R.drawable.ic_notification)
        builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE)

        // Force heads up notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            builder.priority = NotificationManager.IMPORTANCE_HIGH
        } else {
            builder.priority = Notification.PRIORITY_HIGH
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setVibrate(longArrayOf(500, 1000))
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            builder.setSound(uri)
        }

        // Main activity intent
        val contentPendingIntent = if (chatFlag) {
            val chatActivityIntent = Intent(this, ChatActivity::class.java)
            PendingIntent.getActivity(this, 0, chatActivityIntent, 0)
        } else {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            PendingIntent.getActivity(this, 0, mainActivityIntent, 0)
        }
        builder.setContentIntent(contentPendingIntent)
        builder.setAutoCancel(true)

        return builder.build()
    }

}