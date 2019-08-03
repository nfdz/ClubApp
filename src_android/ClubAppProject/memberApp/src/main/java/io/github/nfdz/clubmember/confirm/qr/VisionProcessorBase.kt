package io.github.nfdz.clubmember.confirm.qr

import android.graphics.Bitmap
import androidx.annotation.GuardedBy
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import java.nio.ByteBuffer

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(T, FrameMetadata, GraphicOverlay)} to define what they want to with the detection
 * results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector object.
 *
 * @param <T> The type of the detected feature.
 */
abstract class VisionProcessorBase<T> : VisionImageProcessor {

    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private var latestImage: ByteBuffer? = null
    @GuardedBy("this")
    private var latestImageMetaData: FrameMetadata? = null
    // To keep the images and metadata in process.
    @GuardedBy("this")
    private var processingImage: ByteBuffer? = null
    @GuardedBy("this")
    private var processingMetaData: FrameMetadata? = null
    @Synchronized
    override fun process(
        data: ByteBuffer,
        frameMetadata: FrameMetadata
    ) {
        latestImage = data
        latestImageMetaData = frameMetadata
        if (processingImage == null && processingMetaData == null) {
            processLatestImage()
        }
    }
    // Bitmap version
    override fun process(bitmap: Bitmap) {
        detectInVisionImage(
            null /* bitmap */,
            FirebaseVisionImage.fromBitmap(bitmap)
        )
    }
    @Synchronized
    private fun processLatestImage() {
        processingImage = latestImage
        processingMetaData = latestImageMetaData
        latestImage = null
        latestImageMetaData = null
        if (processingImage != null && processingMetaData != null) {
            processImage(
                processingImage as ByteBuffer,
                processingMetaData as FrameMetadata)
        }
    }
    private fun processImage(
        data: ByteBuffer,
        frameMetadata: FrameMetadata
    ) {
        val metadata = FirebaseVisionImageMetadata.Builder()
            .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
            .setWidth(frameMetadata.getWidth())
            .setHeight(frameMetadata.getHeight())
            .setRotation(frameMetadata.getRotation())
            .build()
        val bitmap = BitmapUtils.getBitmap(data, frameMetadata)
        detectInVisionImage(
            bitmap,
            FirebaseVisionImage.fromByteBuffer(data, metadata)
        )
    }
    private fun detectInVisionImage(
        originalCameraImage: Bitmap?,
        image: FirebaseVisionImage
    ) {
        detectInImage(image)
            .addOnSuccessListener { results ->
                processingImage = null
                processingMetaData = null
                onSuccess(originalCameraImage!!, results)
            }
            .addOnFailureListener { e ->
                processingImage = null
                processingMetaData = null
                this@VisionProcessorBase.onFailure(e)
            }
    }
    override fun stop() {}
    protected abstract fun detectInImage(image: FirebaseVisionImage): Task<T>
    protected abstract fun onSuccess(
        originalCameraImage: Bitmap,
        results: T)
    protected abstract fun onFailure(e: Exception)
}