package io.github.nfdz.clubadmin.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_chat.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class ChatActivity : AppCompatActivity() {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, ChatActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        chat_btn_delete.setOnClickListener { deleteMessages() }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    private fun deleteMessages() {
        val deleteTo = try {
            val simpleFormatter = SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault())
            val timestampDate = simpleFormatter.parse(chat_et_limit_date.text.toString())
            timestampDate.time
        } catch (e: Exception) {
            Timber.e(e)
            toast(R.string.chat_date_error)
            return
        }

        chat_container.fadeOut()
        chat_pb_loading.fadeIn()
        FirebaseFirestore.getInstance().collection("chat_messages")
            .whereLessThan("timestamp", deleteTo)
            .get()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val msgs = task.result?.documents
                    if (msgs == null || msgs.isEmpty()) {
                        navigateToFinish()
                    } else {
                        val size = msgs.size
                        val counter = AtomicInteger(0)
                        msgs.forEach { doc ->
                            FirebaseFirestore.getInstance().collection("chat_messages")
                                .document(doc.id)
                                .delete()
                                .addOnCompleteListener(this) {
                                    Timber.d("Count=$counter")
                                    if (counter.incrementAndGet() == size) {
                                        navigateToFinish()
                                    }
                                }
                        }
                    }

                } else {
                    navigateToError()
                }
            }
    }

    private fun navigateToError() {
        chat_container.fadeIn()
        chat_pb_loading.fadeOut()
        toast(R.string.chat_delete_error)
    }

    private fun navigateToFinish() {
        finish()
    }

}
