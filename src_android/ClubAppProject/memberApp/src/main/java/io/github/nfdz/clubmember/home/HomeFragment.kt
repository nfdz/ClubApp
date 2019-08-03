package io.github.nfdz.clubmember.home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.github.nfdz.clubcommonlibrary.toast
import io.github.nfdz.clubmember.common.BackClickHandler
import io.github.nfdz.clubmember.common.DataChangeEvent
import io.github.nfdz.clubmember.common.EventsAdapter
import io.github.nfdz.clubmember.confirm.ConfirmAttendanceActivity
import io.github.nfdz.clubmember.event.EventActivity
import io.github.nfdz.clubmember.home.category.ChooseCategoryActivity
import io.github.nfdz.clubmember.splashscreen.SplashScreenActivity
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.fragment_home.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class HomeFragment : Fragment(), HomeView, EventsAdapter.Listener, BackClickHandler {

    companion object {

        private const val CHOOSE_CATEGORY_REQUEST = 42386

        @JvmStatic
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private val presenter: HomePresenter by lazy { HomePresenterImpl(this, activity?.let { HomeInteractorImpl(it) }) }
    private val adapter = EventsAdapter(listener = this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        presenter.onCreate()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (CHOOSE_CATEGORY_REQUEST == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                ChooseCategoryActivity.getCategoryFromResult(data)?.let { presenter.onChangeFilterClick(it) }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroyView() {
        EventBus.getDefault().unregister(this)
        presenter.onDestroy()
        super.onDestroyView()
    }

    override fun onBackClick(): Boolean {
        return if (isResumed && !isDetached) {
            presenter.onBackClick()
        } else {
            false
        }
    }

    private fun setupView() {
        home_rv.adapter = adapter
        home_tv_filter.setOnClickListener { presenter.onFilterClick() }
        home_srl_refresh.setOnRefreshListener { presenter.onRefreshClick() }
        home_iv_cancel_filter.setOnClickListener { presenter.onRemoveFilterClick() }
    }

    override fun showContent(items: List<EventsAdapter.EventEntry>) {
        adapter.data = items
        home_tv_no_content.visibility = View.GONE
    }

    override fun showNoContent() {
        adapter.data = emptyList()
        home_tv_no_content.visibility = View.VISIBLE
    }

    override fun showFilter(filter: HomeFilter) {
        home_tv_filter.setText(filter.filterTextRes)
        context?.let { home_tv_filter.setBackgroundColor(ContextCompat.getColor(it, filter.filterColor)) }
        home_iv_no_filter.visibility = View.GONE
        home_iv_cancel_filter.visibility = View.VISIBLE
    }

    override fun showNoFilter() {
        home_tv_filter.setText(R.string.home_no_filter)
        context?.let { home_tv_filter.setBackgroundColor(ContextCompat.getColor(it, R.color.gray)) }
        home_iv_no_filter.visibility = View.VISIBLE
        home_iv_cancel_filter.visibility = View.GONE
    }

    override fun hideRefresh() {
        home_srl_refresh.isRefreshing = false
    }

    override fun showDisabledUserMsg() {
        context?.toast(R.string.disabled_user_error)
    }

    override fun navigateToLogin() {
        activity?.let { SplashScreenActivity.startActivity(it) }
    }

    override fun navigateToChooseFilter() {
        ChooseCategoryActivity.startActivityForResult(this, CHOOSE_CATEGORY_REQUEST)
    }

    override fun navigateToEvent(eventId: String) {
        context?.let { EventActivity.startActivity(it, eventId) }
    }

    override fun navigateToConfirmAttendance(eventId: String) {
        context?.let { ConfirmAttendanceActivity.startActivity(it, eventId) }
    }

    override fun onEventClick(event: EventsAdapter.EventEntry) {
        presenter.onEventClick(event)
    }

    override fun onConfirmAttendanceClick(event: EventsAdapter.EventEntry) {
        presenter.onConfirmAttendanceClick(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onDataChangeEvent(event: DataChangeEvent) {
        Timber.d("DataChangeEvent")
        presenter.onDataChangeEvent()
    }

}