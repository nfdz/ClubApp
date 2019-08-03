package io.github.nfdz.clubadmin.qrs

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import android.view.MenuItem
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import io.github.nfdz.clubadmin.common.QRCode
import io.github.nfdz.clubcommonlibrary.toast
import YOUR.ADMIN.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_qrs.*
import timber.log.Timber

class QRsActivity : AppCompatActivity(), QRsAdapter.Listener {

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, QRsActivity::class.java))
        }
    }

    private val adapter = QRsAdapter(listener = this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrs)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        qrs_rv.addItemDecoration(DividerItemDecoration(this, RecyclerView.VERTICAL))
        qrs_refresh.setOnRefreshListener { refreshContent() }
        qrs_fab_add.setOnClickListener { askQRCode() }
        qrs_rv.adapter = adapter
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshContent()
    }

    private fun refreshContent() {
        qrs_refresh.isRefreshing = true
        FirebaseFirestore.getInstance().collection("qr_codes")
            .get()
            .addOnCompleteListener(this) { task ->
                try {
                    if (task.isSuccessful) {
                        adapter.data = task.result?.documents?.map { doc ->
                            QRCode(doc.id, doc.data!!["code"] as String)
                        } ?: emptyList()
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                }
                qrs_refresh.isRefreshing = false
            }

    }

    override fun onDeleteQRClick(qr: QRCode) {
        askDeleteConfirmation(qr)
    }

    private fun askDeleteConfirmation(qr: QRCode) {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.qrs_delete_title)
            setMessage(getString(R.string.qrs_delete_message, qr.code))
        }.setPositiveButton(android.R.string.yes) { dialog, _ ->
            deleteQR(qr)
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show()
    }

    private fun deleteQR(qr: QRCode) {
        toast(R.string.qrs_delete_deleting)
        FirebaseFirestore.getInstance().collection("qr_codes")
            .document(qr.id)
            .delete()
            .addOnCompleteListener(this) {
                refreshContent()
            }
    }

    private fun askQRCode() {
        var codeEditText: EditText? = null
        codeEditText = AlertDialog.Builder(this).apply {
            setView(R.layout.dialog_add_qr)
        }.setPositiveButton(R.string.qrs_add_dialog_save) { dialog, _ ->
            val code = codeEditText?.text?.toString()?.trim()
            if (false == code?.isEmpty()) {
                addQRCode(code)
                dialog.dismiss()
            }
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
        }.show().findViewById<EditText>(R.id.qr_add_et_code)
    }

    private fun addQRCode(code: String) {
        qrs_refresh.isRefreshing = true
        val qr = mutableMapOf<String,Any>()
        qr["code"] = code
        FirebaseFirestore.getInstance().collection("qr_codes").document()
            .set(qr)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    refreshContent()
                } else {
                    qrs_refresh.isRefreshing = false
                    toast(R.string.qrs_add_error)
                }
            }
    }

}
