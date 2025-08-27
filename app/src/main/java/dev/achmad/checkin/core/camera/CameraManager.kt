package dev.achmad.checkin.core.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Exception

sealed class CameraDeviceState {
    data object Initializing : CameraDeviceState()
    data object LoadingPreview: CameraDeviceState()
    data class Ready(val preview: Preview) : CameraDeviceState()
    data object NoPermission : CameraDeviceState()
    data object NoCamera : CameraDeviceState()
    data class Error(val exception: Throwable) : CameraDeviceState()
}

sealed class PictureCaptureState {
    data object Idle : PictureCaptureState()
    data object InProgress : PictureCaptureState()
    data class Success(val bitmap: Bitmap) : PictureCaptureState()
    data class Error(val exception: Throwable) : PictureCaptureState()
}

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val coroutineScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    // State for the camera device
    private val _deviceState = MutableStateFlow<CameraDeviceState>(CameraDeviceState.Initializing)
    val deviceState = _deviceState.asStateFlow()

    // State for the picture capture action
    private val _pictureState = MutableStateFlow<PictureCaptureState>(PictureCaptureState.Idle)
    val pictureState = _pictureState.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null

    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    fun initializeCamera() {
        if (!hasCameraPermission()) {
            _deviceState.value = CameraDeviceState.NoPermission
            return
        }

        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                if (!hasBackCamera() && !hasFrontCamera()) {
                    _deviceState.value = CameraDeviceState.NoCamera
                    return@addListener
                }
                adjustCameraSelector()
                bindCameraUseCases()
            } catch (e: Exception) {
                _deviceState.value = CameraDeviceState.Error(e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun takePicture() {
        if (imageCapture == null || _pictureState.value is PictureCaptureState.InProgress) {
            return // Ignore if not ready or already taking a picture
        }

        coroutineScope.launch(dispatcher) {
            _pictureState.value = PictureCaptureState.InProgress
            val photoFile = File.createTempFile("camerax_", ".jpg", context.cacheDir)
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            try {
                imageCapture?.takePicture(outputOptions, dispatcher.asExecutor(), object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        try {
                            val bitmap = correctBitmapRotation(photoFile)
                            _pictureState.value = PictureCaptureState.Success(bitmap)
                        } catch (e: Exception) {
                            _pictureState.value = PictureCaptureState.Error(e)
                        } finally {
                            photoFile.delete()
                        }
                    }

                    override fun onError(e: ImageCaptureException) {
                        _pictureState.value = PictureCaptureState.Error(e)
                        photoFile.delete()
                    }
                })
            } catch (e: Exception) {
                _pictureState.value = PictureCaptureState.Error(e)
                photoFile.delete()
            }
        }
    }

    /**
     * Resets the picture state to Idle. Call this from the UI after handling
     * a Success or Error state.
     */
    fun resetPictureState() {
        _pictureState.value = PictureCaptureState.Idle
    }

    fun toggleCamera() {
        if (!hasFrontCamera() || !hasBackCamera()) return

        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindCameraUseCases()
    }

    private fun bindCameraUseCases() {
        val provider = cameraProvider ?: throw IllegalStateException("Camera initialization failed.")
        provider.unbindAll()
        val preview = Preview.Builder().build()
        imageCapture = ImageCapture.Builder().build()
        try {
            _deviceState.value = CameraDeviceState.LoadingPreview
            camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
            _deviceState.value = CameraDeviceState.Ready(preview)
        } catch (e: Exception) {
            _deviceState.value = CameraDeviceState.Error(e)
        }
    }

    private fun adjustCameraSelector() {
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA && !hasBackCamera()) {
            cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        } else if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA && !hasFrontCamera()) {
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    private fun hasCameraPermission() = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    private fun hasBackCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    private fun hasFrontCamera() = cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false

    private fun correctBitmapRotation(file: File): Bitmap {
        val exifInterface = ExifInterface(file.inputStream())
        val rotation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        val rotationInDegrees = when (rotation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        if (rotationInDegrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(rotationInDegrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}