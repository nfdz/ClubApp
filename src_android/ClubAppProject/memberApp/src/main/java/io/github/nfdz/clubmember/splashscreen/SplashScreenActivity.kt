package io.github.nfdz.clubmember.splashscreen

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.showSnackbar
import io.github.nfdz.clubmember.chat.ChatActivity
import io.github.nfdz.clubmember.contact.ClubActivity
import io.github.nfdz.clubmember.event.EventActivity
import io.github.nfdz.clubmember.main.MainActivity
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_splash_screen.*


class SplashScreenActivity : AppCompatActivity(), SplashScreenView {

    companion object {
        @JvmStatic
        fun startActivity(activity: Activity) {
            val intent = Intent(activity, SplashScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
            activity.finish()
        }
    }

    private val presenter: SplashScreenPresenter by lazy { SplashScreenPresenterImpl(this, SplashScreenInteractorImpl(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        presenter.onCreate(getChatFlag(), getEventId())
    }

    private fun getChatFlag(): Boolean = "chat" == intent?.extras?.getString("pushType")

    private fun getEventId(): String {
        if (Intent.ACTION_VIEW == intent?.action) {
            val segments = intent?.data?.pathSegments
            if (segments != null && segments.size > 1) {
                return segments[1]
            }
        }
        return ""
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    private fun setupView() {
        setContentView(R.layout.activity_splash_screen)
        splash_screen_tv_contact.setOnClickListener { presenter.onContactClick() }
        splash_screen_btn_login.setOnClickListener {
            presenter.onLoginClick(splash_screen_tie_email.text.toString(), splash_screen_tie_pass.text.toString())
        }
        splash_screen_tv_reset_password.setOnClickListener { presenter.onResetPassword(splash_screen_tie_email.text.toString()) }
        Glide.with(this)
            .asGif()
            .load(R.drawable.loading)
            .into(splash_screen_iv_loading)
    }

    override fun hideLoading() {
        splash_screen_iv_loading.fadeOut()
    }

    override fun showLoading() {
        splash_screen_iv_loading.fadeIn()
    }

    override fun showLoginForm() {
        splash_screen_container_form.fadeIn()
    }

    override fun hideLoginForm() {
        splash_screen_container_form.fadeOut()
    }

    override fun showLoginCredentialsError() {
        splash_screen_root.showSnackbar(getString(R.string.splash_screen_credentials_error))
    }

    override fun showLoginConnectionError() {
        splash_screen_root.showSnackbar(getString(R.string.splash_screen_connection_error))
    }

    override fun showResetPasswordError() {
        splash_screen_root.showSnackbar(getString(R.string.splash_screen_reset_password_error))
    }

    override fun showCheckEmailMsg() {
        splash_screen_root.showSnackbar(getString(R.string.splash_screen_reset_password_check_email))
    }

    override fun showDisabledUserMsg() {
        splash_screen_root.showSnackbar(getString(R.string.disabled_user_error))
    }

    override fun navigateToMain() {
        MainActivity.startActivity(this)
        finish()
    }

    override fun navigateToChat() {
        ChatActivity.startActivity(this)
        finish()
    }

    override fun navigateToEvent(eventId: String) {
        EventActivity.startActivity(this, eventId)
        finish()
    }

    override fun navigateToContact() {
        ClubActivity.startActivity(this)
    }

}
