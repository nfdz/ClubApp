package io.github.nfdz.clubmember.contact

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import io.github.nfdz.clubcommonlibrary.showSnackbar
import io.github.nfdz.clubmember.common.AnalyticsEvent
import io.github.nfdz.clubmember.common.logAnalytics
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_club.*
import kotlinx.android.synthetic.main.activity_event.*

class ClubActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ClubActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_club)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        club_iv_where_maps.setOnClickListener { navigateToMap() }
        club_tv_contact_content.movementMethod = LinkMovementMethod.getInstance()
        club_iv_instagram.setOnClickListener { navigateToInstragram() }
        club_iv_facebook.setOnClickListener { navigateToFacebook() }
    }

    private fun navigateToMap() {
        val uriLocation = getString(R.string.club_location_uri)
        val uriQuery = Uri.encode(getString(R.string.club_location_uri_query))
        val mapIntentUri = Uri.parse("$uriLocation?q=$uriQuery")
        val mapIntent = Intent(Intent.ACTION_VIEW, mapIntentUri)
        try {
            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent)
            } else {
                club_container.showSnackbar(getString(R.string.club_open_map_error))
            }
        } catch (e: Exception) {
            reportException(e)
            club_container.showSnackbar(getString(R.string.club_open_map_error))
        }
    }

    private fun navigateToFacebook() {
        logAnalytics(AnalyticsEvent.FACEBOOK_CLUB)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.club_facebook_url))))
        } catch (e: Exception) {
            reportException(e)
            event_container.showSnackbar(getString(R.string.event_facebook_error))
        }
    }

    private fun navigateToInstragram() {
        logAnalytics(AnalyticsEvent.INSTAGRAM_CLUB)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.club_instagram_url))))
        } catch (e: Exception) {
            reportException(e)
            event_container.showSnackbar(getString(R.string.event_instagram_error))
        }
    }

}
