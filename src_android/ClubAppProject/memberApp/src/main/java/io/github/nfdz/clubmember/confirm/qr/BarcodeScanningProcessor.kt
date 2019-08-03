package io.github.nfdz.clubmember.confirm.qr

import android.graphics.Bitmap
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import timber.log.Timber
import java.io.IOException

/** Barcode Detector Demo.  */
class BarcodeScanningProcessor(val listener: (result: List<String>) -> Unit) : VisionProcessorBase<List<FirebaseVisionBarcode>>() {

    // Note that if you know which format of barcode your app is dealing with, detection will be
    // faster to specify the supported barcode formats one by one, e.g.
    // FirebaseVisionBarcodeDetectorOptions.Builder()
    //     .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_QR_CODE)
    //     .build()
    private val detector: FirebaseVisionBarcodeDetector by lazy {
        FirebaseVision.getInstance().visionBarcodeDetector
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Timber.e("Exception thrown while trying to close Barcode Detector: $e")
        }
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionBarcode>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap,
        results: List<FirebaseVisionBarcode>
    ) {
        val values = results.map { it.displayValue ?: return }
        if (values.isNotEmpty()) {
            Timber.d("Barcode detection success: ${values.size}")
            listener(values)
        }
    }

    override fun onFailure(e: Exception) {
        Timber.e(e, "Barcode detection failed")
    }

}