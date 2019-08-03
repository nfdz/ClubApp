package io.github.nfdz.clubmember.chat

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.toast
import io.github.nfdz.clubmember.common.ChatEvent
import io.github.nfdz.clubmember.main.MainActivity
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_chat.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber


class ChatActivity : AppCompatActivity(), ChatView, ChatAdapter.Listener {

    companion object {
        private const val RECYCLER_POSITION_STATE_KEY = "recycler_position"
        @JvmStatic
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ChatActivity::class.java))
        }
    }

    private val presenter: ChatPresenter by lazy { ChatPresenterImpl(this, ChatInteractorImpl(this)) }
    private val adapter = ChatAdapter(listener = this)
    private var layoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        setupView()
        presenter.onCreate()
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onPause() {
        super.onPause()
        presenter.onPause()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.chat, menu)
        val muteNotificationsItem = menu.findItem(R.id.chat_settings_notifications)
        muteNotificationsItem.isVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                MainActivity.startActivity(this)
                finish()
                true
            }
            R.id.chat_settings_notifications -> { navigateToNotificationSettings(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun navigateToNotificationSettings() {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                 Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                    .putExtra("app_uid", applicationInfo.uid)
                    .putExtra(Settings.EXTRA_CHANNEL_ID, getString(R.string.notification_channel_id))
            } else {
                 Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                    .putExtra("app_package", packageName)
                    .putExtra("app_uid", applicationInfo.uid)
                    .putExtra("android.provider.extra.APP_PACKAGE", packageName)
            }
            startActivity(intent)
        } catch (e: Exception) {
            reportException(e)
            toast(R.string.chat_notifications_settings_error)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putInt(RECYCLER_POSITION_STATE_KEY, layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        chat_rv_messages.scrollToPosition(savedInstanceState?.getInt(RECYCLER_POSITION_STATE_KEY, 0) ?: 0)
    }

    private fun setupView() {
        setContentView(R.layout.activity_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            chat_et_message.elevation = resources.getDimension(R.dimen.chat_message_box_elevation)
            chat_iv_send.elevation = resources.getDimension(R.dimen.chat_message_box_elevation)
        }
        chat_rv_messages.adapter = adapter
        layoutManager = chat_rv_messages.layoutManager as? LinearLayoutManager
        chat_et_message.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                chat_rv_messages.scrollToPosition(0)
                presenter.onSendMessageClick(chat_et_message.text.toString())
                true
            } else {
                false
            }
        }
        chat_iv_send.setOnClickListener {
            chat_rv_messages.scrollToPosition(0)
            presenter.onSendMessageClick(chat_et_message.text.toString())
        }
        chat_et_message.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                updateSendButton(true == s?.isEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }
        })
        updateSendButton(chat_et_message.text.isEmpty())
        chat_rv_messages.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    // Scrolling up
                    chat_fab_down.hide()
                } else {
                    // Scrolling down
                    if ((layoutManager?.findFirstVisibleItemPosition() ?: 0) < 3) {
                        chat_fab_down.hide()
                    } else {
                        chat_fab_down.show()
                    }
                }
            }
        })
        chat_fab_down.setOnClickListener { chat_rv_messages.scrollToPosition(0) }
    }

    private fun updateSendButton(isTextBoxEmpty: Boolean) {
        if (isTextBoxEmpty) {
            chat_iv_send.isEnabled = false
            chat_iv_send.alpha = 0.35f
        } else {
            chat_iv_send.isEnabled = true
            chat_iv_send.alpha = 1f
        }
    }

    override fun setChatContent(items: List<ChatMessage>, alertNewMessage: Boolean) {
        adapter.data = items

        val firstVisibleItem = layoutManager?.findFirstCompletelyVisibleItemPosition() ?: 0
        if (firstVisibleItem == 0) {
            chat_rv_messages.scrollToPosition(0)
        } else {
            if (alertNewMessage) {
                toast(R.string.chat_new_message, Toast.LENGTH_SHORT)
            }
        }
    }

    override fun onChatMessageLongClick(message: ChatMessage) {
        presenter.onChatMessageLongClick(message)
    }

    override fun showLoading() {
        chat_pb_send_loading.fadeIn()
        chat_iv_send.fadeOut()
        chat_et_message.isEnabled = false
    }

    override fun hideLoading() {
        chat_pb_send_loading.fadeOut()
        chat_iv_send.fadeIn()
        chat_et_message.isEnabled = true
    }

    override fun clearSendBox() {
        chat_et_message.setText("")
        chat_et_message.clearFocus()
    }

    override fun showSendErrorMessage() {
        toast(R.string.chat_send_error)
    }

    override fun showMessageCopiedToTheClipboard() {
        toast(R.string.chat_clipboard_message)
    }

    override fun showWelcomeToChat() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.chat_welcome_title)
            setMessage(R.string.chat_welcome_message)
        }.setPositiveButton(android.R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }.show()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onChatEvent(event: ChatEvent) {
        Timber.d("ChatEvent")
        presenter.onChatEvent()
    }

}
