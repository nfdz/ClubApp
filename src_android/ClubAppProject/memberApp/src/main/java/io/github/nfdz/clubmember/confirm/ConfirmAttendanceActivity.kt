package io.github.nfdz.clubmember.confirm

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.firebase.ml.common.FirebaseMLException
import io.github.nfdz.clubcommonlibrary.doMainThread
import io.github.nfdz.clubcommonlibrary.fadeIn
import io.github.nfdz.clubcommonlibrary.fadeOut
import io.github.nfdz.clubcommonlibrary.toast
import io.github.nfdz.clubmember.confirm.qr.BarcodeScanningProcessor
import io.github.nfdz.clubmember.confirm.qr.CameraSource
import io.github.nfdz.clubmember.reportException
import YOUR.MEMBER.APP.ID.HERE.R
import kotlinx.android.synthetic.main.activity_confirm_attendance.*
import java.io.IOException


class ConfirmAttendanceActivity : AppCompatActivity(), ConfirmAttendanceView {

    companion object {
        private const val CAMERA_REQUEST: Int = 9623
        private const val EVENT_ID_EXTRA = "event_id"
        @JvmStatic
        fun startActivity(context: Context, eventId: String) {
            context.startActivity(Intent(context, ConfirmAttendanceActivity::class.java).apply { putExtra(EVENT_ID_EXTRA, eventId) })
        }
    }

    private val presenter: ConfirmAttendancePresenter by lazy { ConfirmAttendancePresenterImpl(this, ConfirmAttendanceInteractorImpl(this)) }

    private var cameraSource: CameraSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupView()
        when {
            hasCameraPermission() -> createCameraSource()
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> explainCameraPermission()
            else -> requestCameraPermission()
        }
        val eventId = intent.getStringExtra(EVENT_ID_EXTRA) ?: ""
        if (eventId.isEmpty()) {
            finish()
        } else {
            presenter.onCreate(eventId)
        }
    }

    private fun setupView() {
        setContentView(R.layout.activity_confirm_attendance)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> { onBackPressed(); true }
            else -> { super.onOptionsItemSelected(item) }
        }
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onPause() {
        super.onPause()
        confirm_attendance_csp.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSource?.release()
        presenter.onDestroy()
    }

    private fun hasCameraPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this,
            listOf(Manifest.permission.CAMERA).toTypedArray(),
            CAMERA_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST -> {
                when {
                    hasCameraPermission() -> createCameraSource()
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> explainCameraPermission()
                    else -> navigateToFinishBecauseCameraPermission()
                }
            }
            else -> { super.onRequestPermissionsResult(requestCode, permissions, grantResults) }
        }
    }

    private fun explainCameraPermission() {
        AlertDialog.Builder(this).apply {
            setTitle(R.string.confirm_attendance_permissions_title)
            setMessage(R.string.confirm_attendance_permissions_text)
        }.setPositiveButton(android.R.string.ok) { dialog, _ ->
            requestCameraPermission()
            dialog.dismiss()
        }.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.cancel()
            navigateToFinishBecauseCameraPermission()
        }.show()
    }

    private fun navigateToFinishBecauseCameraPermission() {
        toast(R.string.confirm_attendance_permissions_text)
        finish()
    }

    private fun createCameraSource() {
        if (cameraSource == null) {
            cameraSource = CameraSource(this)
        }
        try {
            cameraSource?.setMachineLearningFrameProcessor(BarcodeScanningProcessor {
                doMainThread {
                    presenter.onBarcodeDetected(it)
                }
            })
        } catch (e: FirebaseMLException) {
            reportException(e)
            toast(R.string.confirm_attendance_qr_reader_error)
        }
    }

    private fun startCameraSource() {
        cameraSource.let {
            try {
                confirm_attendance_csp.start(it)
            } catch (e: IOException) {
                it?.release()
                cameraSource = null
                reportException(e)
                toast(R.string.confirm_attendance_qr_reader_error)
            }
        }
    }

    override fun showScanner() {
        confirm_attendance_csp.visibility = View.VISIBLE
        confirm_attendance_iv_frame.visibility = View.VISIBLE
    }

    override fun hideScanner() {
        confirm_attendance_csp.visibility = View.INVISIBLE
        confirm_attendance_iv_frame.visibility = View.INVISIBLE
    }

    override fun showLoading() {
        confirm_attendance_pb.fadeIn()
    }

    override fun hideLoading() {
        confirm_attendance_pb.fadeOut()
    }

    override fun showInvalidCodeMessage() {
        toast(getString(R.string.confirm_attendance_invalid_code))
    }

    override fun showErrorConfirmingMessage() {
        toast(getString(R.string.confirm_attendance_error_confirming))
    }

    override fun navigateToFinish() {
        finish()
    }

}
