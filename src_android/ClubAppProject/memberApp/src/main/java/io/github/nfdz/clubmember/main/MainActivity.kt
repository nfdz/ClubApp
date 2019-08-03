package io.github.nfdz.clubmember.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import io.github.nfdz.clubcommonlibrary.showSnackbar
import io.github.nfdz.clubmember.calendar.CalendarFragment
import io.github.nfdz.clubmember.chat.ChatActivity
import io.github.nfdz.clubmember.common.BackClickHandler
import io.github.nfdz.clubmember.contact.ClubActivity
import io.github.nfdz.clubmember.home.HomeFragment
import io.github.nfdz.clubmember.reportException
import io.github.nfdz.clubmember.user.UserFragment
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) })
        }
    }

    private var pagerAdapter: MainPagerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
    }

    private fun setupView() {
        setContentView(R.layout.activity_main)
        setSupportActionBar(main_toolbar)
        supportActionBar?.title = ""
        pagerAdapter = MainPagerAdapter(this, supportFragmentManager)
        main_view_pager.adapter = pagerAdapter
        main_navigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId) {
                R.id.main_nav_home -> { main_view_pager.currentItem = 0; true }
                R.id.main_nav_calendar -> { main_view_pager.currentItem = 1; true }
                R.id.main_nav_user -> { main_view_pager.currentItem = 2; true }
                else -> false
            }
        }
        main_navigation.selectedItemId = R.id.main_nav_home
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.main_chat -> { navigateToChat(); true }
            R.id.main_club -> { navigateToClub(); true }
            R.id.main_playlist -> { navigateToPlaylist(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        val page = pagerAdapter?.currentPage
        if (page is BackClickHandler) {
            if (page.onBackClick()) return
        }
        if (main_navigation.selectedItemId != R.id.main_nav_home) {
            main_navigation.selectedItemId = R.id.main_nav_home
        } else {
            finish()
        }
    }

    private fun navigateToChat() {
        ChatActivity.startActivity(this)
    }

    private fun navigateToClub() {
        ClubActivity.startActivity(this)
    }

    private fun navigateToPlaylist() {
        val spotifyIntentUri = Uri.parse(getString(R.string.playlist_spotify_uri))
        val spotifyIntent = Intent(Intent.ACTION_VIEW, spotifyIntentUri)
        try {
            if (spotifyIntent.resolveActivity(packageManager) != null) {
                startActivity(spotifyIntent)
            } else {
                main_container.showSnackbar(getString(R.string.playlist_spotify_open_error))
            }
        } catch (e: Exception) {
            reportException(e)
            main_container.showSnackbar(getString(R.string.playlist_spotify_open_error))
        }
    }

    private class MainPagerAdapter(val context: Context, fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        var currentPage: Any = 0

        override fun getItem(position: Int) = when(position) {
            0 -> HomeFragment.newInstance()
            1 -> CalendarFragment.newInstance()
            2 -> UserFragment.newInstance()
            else -> throw IllegalArgumentException("Invalid tab position=$position")
        }

        override fun getCount() = 3

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            if (currentPage !== obj) {
                currentPage = obj
            }
            super.setPrimaryItem(container, position, obj)
        }

    }

}
