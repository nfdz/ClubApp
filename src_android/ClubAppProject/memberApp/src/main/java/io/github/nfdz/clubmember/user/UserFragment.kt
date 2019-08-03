package io.github.nfdz.clubmember.user

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import io.github.nfdz.clubcommonlibrary.*
import io.github.nfdz.clubmember.bookings.BookingsActivity
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.points.PointsActivity
import io.github.nfdz.clubmember.splashscreen.SplashScreenActivity
import io.github.nfdz.clubmember.user.data.UserDataActivity
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.fragment_user.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class UserFragment : Fragment(), UserView {

    companion object {
        fun newInstance(): UserFragment {
            return UserFragment()
        }
    }

    private val presenter: UserPresenter by lazy { UserPresenterImpl(this, activity?.let { UserInteractorImpl(it) }) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        presenter.onCreate()
    }

    override fun onDestroyView() {
        presenter.onDestroy()
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }

    private fun setupView() {
        user_iv_edit_alias.setOnClickListener { presenter.onEditAliasClick() }
        user_tv_alias.setOnClickListener { presenter.onEditAliasClick() }
        user_btn_bookings.setOnClickListener { presenter.onMyBookingsClick() }
        user_btn_data.setOnClickListener { presenter.onMyDataClick() }
        user_btn_points.setOnClickListener { presenter.onMyPointsClick() }
        user_iv_logout.setOnClickListener { presenter.onLogoutClick() }
    }

    override fun showLoadingAlias() {
        user_pb_loading_alias.fadeIn()
        user_tv_alias.fadeOut()
        user_iv_edit_alias.fadeOut()
        user_tv_at.fadeOut()
    }

    override fun hideLoadingAlias() {
        user_pb_loading_alias.fadeOut()
        user_tv_alias.fadeIn()
        user_iv_edit_alias.fadeIn()
        user_tv_at.fadeIn()
    }

    override fun setContent(alias: String?, fullName: String, hasBookings: Boolean, signUpDate: Long) {
        user_tv_alias.text = alias ?: getString(R.string.user_alias_empty)
        user_tv_full_name.text = fullName
        user_btn_bookings.isEnabled = hasBookings
        user_tv_member_date.text = getString(R.string.user_member_date_text_format, signUpDate.toDate().printDayMonthYear())
    }

    override fun askEditAlias(currentAlias: String?) {
        context?.let { itContext ->
            var aliasEditText: EditText? = null
            aliasEditText = AlertDialog.Builder(itContext).apply {
                setView(R.layout.dialog_edit_user_alias)
            }.setPositiveButton(R.string.user_save_alias_ok) { dialog, _ ->
                presenter.onSaveAliasClick(aliasEditText?.text?.toString()?.trim())
                dialog.dismiss()
            }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show().findViewById<EditText>(R.id.alias_et)
            currentAlias?.let { aliasEditText?.append(it); aliasEditText }
        }
    }

    override fun askConfirmLogout() {
        context?.let { itContext ->
            AlertDialog.Builder(itContext).apply {
                setTitle(R.string.user_logout_title)
                setMessage(R.string.user_logout_message)
            }.setPositiveButton(android.R.string.yes) { dialog, _ ->
                presenter.onConfirmLogoutClick()
                dialog.dismiss()
            }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()
        }
    }

    override fun showInvalidAlias(alias: String?) {
        context?.let {
            AlertDialog.Builder(it).apply {
                setTitle(R.string.user_invalid_alias_title)
                setMessage(R.string.user_invalid_alias_msg)
            }.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                doMainThread { askEditAlias(alias) }
            }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()
        }
    }

    override fun showConflictAlias(alias: String?) {
        context?.let {
            AlertDialog.Builder(it).apply {
                setTitle(R.string.user_conflict_alias_title)
                setMessage(R.string.user_conflict_alias_msg)
            }.setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                doMainThread { askEditAlias(alias) }
            }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }.show()
        }
    }

    override fun showErrorSavingAliasMsg() {
        user_container.showSnackbar(getString(R.string.user_save_alias_error))
    }

    override fun navigateToBookings() {
        context?.let { BookingsActivity.startActivity(it) }
    }

    override fun navigateToPoints(userPoints: Int) {
        context?.let { PointsActivity.startActivity(it, userPoints) }
    }

    override fun navigateToData() {
        context?.let { UserDataActivity.startActivity(it) }
    }

    override fun navigateToLogIn() {
        activity?.let { SplashScreenActivity.startActivity(it) }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangeEvent(event: DataChangeEvent) {
        Timber.d("DataChangeEvent")
        presenter.onDataChangeEvent()
    }

}