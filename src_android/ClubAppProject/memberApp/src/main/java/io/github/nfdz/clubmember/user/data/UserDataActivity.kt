package io.github.nfdz.clubmember.user.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.hideKeyboard
import io.github.nfdz.clubcommonlibrary.showSnackbar
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_user_data.*

class UserDataActivity : AppCompatActivity(), UserDataView {

    companion object {
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, UserDataActivity::class.java))
        }
    }

    private val presenter: UserDataPresenter by lazy { UserDataPresenterImpl(this, UserDataInteractorImpl(this)) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        presenter.onCreate()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_user_data)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        user_data_tv_change_password.setOnClickListener { presenter.onChangePasswordClick() }
        user_data_btn_save.setOnClickListener {
            presenter.onSaveClick(user_data_tie_birthday.text.toString(),
                user_data_tie_address.text.toString(),
                user_data_tie_phone.text.toString())
        }
    }

    override fun setContent(fields: UserDataFields) {
        user_data_tie_full_name.setText(fields.fullName)
        user_data_tie_email.setText(fields.email)
        user_data_tie_birthday.setText(fields.birthday)
        user_data_tie_address.setText(fields.address)
        user_data_tie_phone.setText(fields.phoneNumber)
    }

    override fun showLoading() {
        user_data_pb_loading.fadeIn()
        user_data_container_form.fadeOut()
        user_data_container.hideKeyboard()
    }

    override fun hideLoading() {
        user_data_pb_loading.fadeOut()
        user_data_container_form.fadeIn()
    }

    override fun showInvalidBirthdayMsg() {
        user_data_container.showSnackbar(getString(R.string.user_data_invalid_birthday))
    }

    override fun showSaveErrorMsg() {
        user_data_container.showSnackbar(getString(R.string.user_data_save_data_error))
    }

    override fun showCheckEmailMsg() {
        user_data_container.showSnackbar(getString(R.string.user_data_password_check_email))
    }

    override fun showChangePasswordError() {
        user_data_container.showSnackbar(getString(R.string.user_data_password_change_error))
    }

    override fun navigateToFinish() {
        finish()
    }

}
