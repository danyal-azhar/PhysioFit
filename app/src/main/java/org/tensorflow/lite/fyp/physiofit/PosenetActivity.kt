/*
 * Copyright 2019 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tensorflow.lite.fyp.physiofit

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.speech.tts.TextToSpeech
import android.util.Log
import android.util.Size
import android.util.SparseIntArray
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import org.tensorflow.lite.examples.posenet.R
import org.tensorflow.lite.fyp.physiofit.java.MainActivity
import org.tensorflow.lite.examples.posenet.lib.BodyPart
import org.tensorflow.lite.examples.posenet.lib.Person
import org.tensorflow.lite.examples.posenet.lib.Posenet
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PosenetActivity() :
    Fragment(),
    ActivityCompat.OnRequestPermissionsResultCallback,
     TextToSpeech.OnInitListener{

    /////customs
    var set1 = MainActivity.customset1;
    var set2 = MainActivity.customset2;
    var set3 = MainActivity.customset3;
    var customexno = MainActivity.customexno;


    private var tts: TextToSpeech? = null
    private var ttswelcomed = false
    private var ttsexercisename = false

    private var intenttype = 0;


    private var armsexercise = 1;
    private var legsexercise = 1;
    private var chestexercise = 1;
    private var shoulderexercise = 1;
    private var backexercise = 1;


    ///////////////////////////////////////////////ARMS//////////////////////////////////////////////////
    private var countedrep = false;
    private var armssetno = 1;
    private var leftbiceprepscount = 0;
    private var rightbiceprepscount = 0;
    private var bicepcurlcount = 0;
    private var bothupcheck = false;
    private var leftupcheck = false;
    private var leftlowcheck = false;
    private var rightupcheck = false;
    private var rightlowcheck = false;
    private var bicepturn = "left";


    //////////////////////////////////////////LEGS////////////////////////////////////////////////////
    ///////////////////////////////////////////////////squats//////////////////////////////////////////////
    private var squatsetno = 1;
    private var squatrepcount = 0;
    private var squatupcheck = false;
    private var squatdowncheck = false;

    /////////////////////////////////////////Shoulders///////////////////////////////////////////
    private var shouldersetno = 1;
    private var shoulderrepcount = 0;

    /////////////////////////////////////////Chest///////////////////////////////////////////
    private var chestsetno = 1;
    private var chestrepcount = 0;

    /////////////////////////////////////////back///////////////////////////////////////////
    private var backsetno = 1;
    private var backrepcount = 0;



    /** List of body joints that should be connected.    */
    private val bodyJoints = listOf(
        Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
        Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
        Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
        Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
        Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
        Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
        Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
        Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
        Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    )

    /** Threshold for confidence score. */
    private val minConfidence = 0.5

    /** Radius of circle used to draw keypoints.  */
    private val circleRadius = 8.0f

    /** Paint class holds the style and color information to draw geometries,text and bitmaps. */
    private var paint = Paint()

    /** A shape for extracting frame data.   */
    private val PREVIEW_WIDTH = 640
    private val PREVIEW_HEIGHT = 480

    /** An object for the Posenet library.    */
    private lateinit var posenet: Posenet

    /** ID of the current [CameraDevice].   */
    private var cameraId: String? = null

    /** A [SurfaceView] for camera preview.   */
    private var surfaceView: SurfaceView? = null

    /** A [CameraCaptureSession] for camera preview.   */
    private var captureSession: CameraCaptureSession? = null

    /** A reference to the opened [CameraDevice].    */
    private var cameraDevice: CameraDevice? = null

    /** The [android.util.Size] of camera preview.  */
    private var previewSize: Size? = null

    /** The [android.util.Size.getWidth] of camera preview. */
    private var previewWidth = 0

    /** The [android.util.Size.getHeight] of camera preview.  */
    private var previewHeight = 0

    /** A counter to keep count of total frames.  */
    private var frameCounter = 0

    /** An IntArray to save image data in ARGB8888 format  */
    private lateinit var rgbBytes: IntArray

    /** A ByteArray to save image data in YUV format  */
    private var yuvBytes = arrayOfNulls<ByteArray>(3)

    /** An additional thread for running tasks that shouldn't block the UI.   */
    private var backgroundThread: HandlerThread? = null

    /** A [Handler] for running tasks in the background.    */
    private var backgroundHandler: Handler? = null

    /** An [ImageReader] that handles preview frame capture.   */
    private var imageReader: ImageReader? = null

    /** [CaptureRequest.Builder] for the camera preview   */
    private var previewRequestBuilder: CaptureRequest.Builder? = null

    /** [CaptureRequest] generated by [.previewRequestBuilder   */
    private var previewRequest: CaptureRequest? = null

    /** A [Semaphore] to prevent the app from exiting before closing the camera.    */
    private val cameraOpenCloseLock = Semaphore(1)

    /** Whether the current camera device supports Flash or not.    */
    private var flashSupported = false

    /** Orientation of the camera sensor.   */
    private var sensorOrientation: Int? = null

    /** Abstract interface to someone holding a display surface.    */
    private var surfaceHolder: SurfaceHolder? = null

    /** [CameraDevice.StateCallback] is called when [CameraDevice] changes its state.   */
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            this@PosenetActivity.cameraDevice = cameraDevice
            createCameraPreviewSession()
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            cameraOpenCloseLock.release()
            cameraDevice.close()
            this@PosenetActivity.cameraDevice = null
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            onDisconnected(cameraDevice)
            this@PosenetActivity.activity?.finish()
        }
    }

    /**
     * A [CameraCaptureSession.CaptureCallback] that handles events related to JPEG capture.
     */
    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureProgressed(
            session: CameraCaptureSession,
            request: CaptureRequest,
            partialResult: CaptureResult
        ) {
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
        }
    }

    /**
     * Shows a [Toast] on the UI thread.
     *
     * @param text The message to show
     */
    private fun showToast(text: String) {
        val activity = activity
        activity?.runOnUiThread { Toast.makeText(activity, text, Toast.LENGTH_SHORT).show() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intenttype = requireArguments().getInt("type");

        tts = TextToSpeech(activity, this)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.tfe_pn_activity_posenet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        surfaceView = view.findViewById(R.id.surfaceView)
        surfaceHolder = surfaceView!!.holder
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }

    override fun onStart() {
        super.onStart()
        openCamera()
        posenet = Posenet(this.requireContext())
    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        posenet.close()
    }

    private fun requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            ConfirmationDialog().show(childFragmentManager, FRAGMENT_DIALOG)
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted(grantResults)) {
                ErrorDialog.newInstance(getString(R.string.tfe_pn_request_permission))
                    .show(childFragmentManager, FRAGMENT_DIALOG)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun allPermissionsGranted(grantResults: IntArray) = grantResults.all {
        it == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Sets up member variables related to camera.
     */
    private fun setUpCameraOutputs() {
        val activity = activity
        val manager = activity!!.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in manager.cameraIdList) {
                val characteristics = manager.getCameraCharacteristics(cameraId)

                // We don't use a front facing camera in this sample.
                val cameraDirection = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (cameraDirection != null &&
                    cameraDirection == CameraCharacteristics.LENS_FACING_FRONT
                ) {
                    continue
                }

                previewSize = Size(PREVIEW_WIDTH, PREVIEW_HEIGHT)

                imageReader = ImageReader.newInstance(
                    PREVIEW_WIDTH, PREVIEW_HEIGHT,
                    ImageFormat.YUV_420_888, /*maxImages*/ 2
                )

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

                previewHeight = previewSize!!.height
                previewWidth = previewSize!!.width

                // Initialize the storage bitmaps once when the resolution is known.
                rgbBytes = IntArray(previewWidth * previewHeight)

                // Check if the flash is supported.
                flashSupported =
                    characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                this.cameraId = cameraId

                // We've found a viable camera and finished setting up member variables,
                // so we don't need to iterate through other available cameras.
                return
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: NullPointerException) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
            ErrorDialog.newInstance(getString(R.string.tfe_pn_camera_error))
                .show(childFragmentManager, FRAGMENT_DIALOG)
        }
    }

    /**
     * Opens the camera specified by [PosenetActivity.cameraId].
     */
    private fun openCamera() {
        val permissionCamera = requireContext().checkPermission(
            Manifest.permission.CAMERA, Process.myPid(), Process.myUid()
        )
        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission()
        }
        setUpCameraOutputs()
        val manager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Wait for camera to open - 2.5 seconds is sufficient
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw RuntimeException("Time out waiting to lock camera opening.")
            }
            manager.openCamera(cameraId!!, stateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera opening.", e)
        }
    }

    /**
     * Closes the current [CameraDevice].
     */
    private fun closeCamera() {
        if (captureSession == null) {
            return
        }

        try {
            cameraOpenCloseLock.acquire()
            captureSession!!.close()
            captureSession = null
            cameraDevice!!.close()
            cameraDevice = null
            imageReader!!.close()
            imageReader = null
        } catch (e: InterruptedException) {
            throw RuntimeException("Interrupted while trying to lock camera closing.", e)
        } finally {
            cameraOpenCloseLock.release()
        }
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("imageAvailableListener").also { it.start() }
        backgroundHandler = Handler(backgroundThread!!.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    /** Fill the yuvBytes with data from image planes.   */
    private fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
        // Row stride is the total number of bytes occupied in memory by a row of an image.
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i]!!)
        }
    }

    /** A [OnImageAvailableListener] to receive frames as they are available.  */
    private var imageAvailableListener = object : OnImageAvailableListener {
        override fun onImageAvailable(imageReader: ImageReader) {
            // We need wait until we have some size from onPreviewSizeChosen
            if (previewWidth == 0 || previewHeight == 0) {
                return
            }

            val image = imageReader.acquireLatestImage() ?: return
            fillBytes(image.planes, yuvBytes)

            ImageUtils.convertYUV420ToARGB8888(
                yuvBytes[0]!!,
                yuvBytes[1]!!,
                yuvBytes[2]!!,
                previewWidth,
                previewHeight,
                /*yRowStride=*/ image.planes[0].rowStride,
                /*uvRowStride=*/ image.planes[1].rowStride,
                /*uvPixelStride=*/ image.planes[1].pixelStride,
                rgbBytes
            )

            // Create bitmap from int array
            val imageBitmap = Bitmap.createBitmap(
                rgbBytes, previewWidth, previewHeight,
                Bitmap.Config.ARGB_8888
            )

            // Create rotated version for portrait display
            val rotateMatrix = Matrix()
            rotateMatrix.postRotate(90.0f)

            val rotatedBitmap = Bitmap.createBitmap(
                imageBitmap, 0, 0, previewWidth, previewHeight,
                rotateMatrix, true
            )
            image.close()

            processImage(rotatedBitmap)
        }
    }

    /** Crop Bitmap to maintain aspect ratio of model input.   */
    private fun cropBitmap(bitmap: Bitmap): Bitmap {
        val bitmapRatio = bitmap.height.toFloat() / bitmap.width
        val modelInputRatio = MODEL_HEIGHT.toFloat() / MODEL_WIDTH
        var croppedBitmap = bitmap

        // Acceptable difference between the modelInputRatio and bitmapRatio to skip cropping.
        val maxDifference = 1e-5

        // Checks if the bitmap has similar aspect ratio as the required model input.
        when {
            abs(modelInputRatio - bitmapRatio) < maxDifference -> return croppedBitmap
            modelInputRatio < bitmapRatio -> {
                // New image is taller so we are height constrained.
                val cropHeight = bitmap.height - (bitmap.width.toFloat() / modelInputRatio)
                croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    0,
                    (cropHeight / 2).toInt(),
                    bitmap.width,
                    (bitmap.height - cropHeight).toInt()
                )
            }
            else -> {
                val cropWidth = bitmap.width - (bitmap.height.toFloat() * modelInputRatio)
                croppedBitmap = Bitmap.createBitmap(
                    bitmap,
                    (cropWidth / 2).toInt(),
                    0,
                    (bitmap.width - cropWidth).toInt(),
                    bitmap.height
                )
            }
        }
        return croppedBitmap
    }

    /** Set the paint color and size.    */
    private fun setPaint() {
        paint.color = Color.RED
        paint.textSize = 30.0f
        paint.strokeWidth = 2.0f
    }

    /** Draw bitmap on Canvas.   */
    private fun draw(canvas: Canvas, person: Person, bitmap: Bitmap) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        // Draw `bitmap` and `person` in square canvas.
        val screenWidth: Int
        val screenHeight: Int
        val left: Int
        val right: Int
        val top: Int
        val bottom: Int
        if (canvas.height > canvas.width) {
            screenWidth = canvas.width
            screenHeight = canvas.width
            left = 0
            top = (canvas.height - canvas.width) / 2
        } else {
            screenWidth = canvas.height
            screenHeight = canvas.height
            left = (canvas.width - canvas.height) / 2
            top = 0
        }
        right = left + screenWidth
        bottom = top + screenHeight

        setPaint()
        canvas.drawBitmap(
            bitmap,
            Rect(0, 0, bitmap.width, bitmap.height),
            Rect(left, top, right, bottom),
            paint
        )

        val widthRatio = screenWidth.toFloat() / MODEL_WIDTH
        val heightRatio = screenHeight.toFloat() / MODEL_HEIGHT

        // Draw key points over the image.
        for (keyPoint in person.keyPoints) {
            if (keyPoint.score > minConfidence) {
                val position = keyPoint.position
                val adjustedX: Float = position.x.toFloat() * widthRatio + left
                val adjustedY: Float = position.y.toFloat() * heightRatio + top
                canvas.drawCircle(adjustedX, adjustedY, circleRadius, paint)
            }
        }

        for (line in bodyJoints) {
            if (
                (person.keyPoints[line.first.ordinal].score > minConfidence) and
                (person.keyPoints[line.second.ordinal].score > minConfidence)
            ) {
                canvas.drawLine(
                    person.keyPoints[line.first.ordinal].position.x.toFloat() * widthRatio + left,
                    person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio + top,
                    person.keyPoints[line.second.ordinal].position.x.toFloat() * widthRatio + left,
                    person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio + top,
                    paint
                )

                if (intenttype == 1) {//ARMS BEGINNER

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to arms beginner "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you arms exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }


                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////DUMBBLE CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    if (armsexercise == 1) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 8) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 6) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 1;
                                armsexercise = 2;
                                ttsexercisename = false
                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////BARBELL CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 2) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "leftup")
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "leftdown")

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "rightup")

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "rightdown")

                                }
                            }
                        }

                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 8) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 6) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                ttsexercisename = false;
                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////HAMMER CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 3) {
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Third exercise is dumbble hammer Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (bicepturn == "left") {
                            /////////////////////////////////////////////////////left side HAMMER bicepcurls//////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    leftupcheck = true;
                                    leftlowcheck = false;

                                }
                                if (leftupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        leftlowcheck = true;
                                        leftbiceprepscount += 1;
                                        tts!!.speak("left"+ leftbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                        bicepturn = "right";
                                        leftupcheck = false;
                                    }
                                }
                            }
                        }

                        if (bicepturn == "right") {

                            ///////////////////////////////////////////////Right side HAMMER bicepcurls/////////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    rightupcheck = true;
                                    rightlowcheck = false;

                                }
                                if (rightupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        rightlowcheck = true;
                                        rightbiceprepscount += 1;
                                        bicepturn = "left";
                                        tts!!.speak("right"+rightbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                        rightupcheck = false;
                                    }
                                }
                            }

                        }
                        if (armssetno == 1 && rightbiceprepscount + leftbiceprepscount >= 20) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 2;
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")


                        } else if (armssetno == 2 && rightbiceprepscount + leftbiceprepscount >= 16) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 3;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 3 && rightbiceprepscount + leftbiceprepscount >= 10) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 1;
                            armsexercise = 4
                            ttsexercisename = false
                        }
                    }


                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////triceps CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 4) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Fourth exercise is seated tricep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 8) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 6) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                ttsexercisename = false
                                tts!!.speak("Your arms workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                requireActivity().finish()
                            }
                        }

                    }

                }



                else if (intenttype == 2) {//ARMSINTERMEDIATE


                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to arms intermediate "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you arms exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////DUMBBLE CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    if (armsexercise == 1) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 2;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 15) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 3;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 1;
                                armsexercise = 2;
                                ttsexercisename = false

                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////BARBELL CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 2) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is barbell bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "leftup")
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "leftdown")

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "rightup")

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "rightdown")

                                }
                            }
                        }

                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 15) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                ttsexercisename = false

                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////HAMMER CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 3) {
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble hammer Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (bicepturn == "left") {
                            /////////////////////////////////////////////////////left side HAMMER bicepcurls//////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    leftupcheck = true;
                                    leftlowcheck = false;

                                }
                                if (leftupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        leftlowcheck = true;
                                        leftbiceprepscount += 1;
                                        bicepturn = "right";
                                        tts!!.speak("left"+leftbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        leftupcheck = false;
                                    }
                                }
                            }
                        }

                        if (bicepturn == "right") {

                            ///////////////////////////////////////////////Right side HAMMER bicepcurls/////////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    rightupcheck = true;
                                    rightlowcheck = false;

                                }
                                if (rightupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        rightlowcheck = true;
                                        rightbiceprepscount += 1;
                                        bicepturn = "left";
                                        tts!!.speak("right"+rightbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        rightupcheck = false;
                                    }
                                }
                            }


                        }
                        if (armssetno == 1 && rightbiceprepscount + leftbiceprepscount >= 40) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 2;
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")


                        } else if (armssetno == 2 && rightbiceprepscount + leftbiceprepscount >= 30) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 3;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 3 && rightbiceprepscount + leftbiceprepscount >= 20) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 1;
                            armsexercise = 4
                            ttsexercisename = false

                        }
                    }


                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////triceps CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 4) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Fourth exercise is seated tricep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 15) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("Your arms workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                requireActivity().finish()
                            }
                        }

                    }


                }


                else if (intenttype == 3) {//ARMSPRO


                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to arms profesional "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you arms exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }


                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////DUMBBLE CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    if (armsexercise == 1) {
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == 30) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 2;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 3;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 1;
                                armsexercise = 2;
                                ttsexercisename = false

                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////BARBELL CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 2) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is barbell bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "leftup")
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "leftdown")

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "rightup")

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "rightdown")

                                }
                            }
                        }

                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == 30) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                ttsexercisename = false

                            }
                        }
                    }

                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////HAMMER CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 3) {

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble hammer Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (bicepturn == "left") {
                            /////////////////////////////////////////////////////left side HAMMER bicepcurls//////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    leftupcheck = true;
                                    leftlowcheck = false;

                                }
                                if (leftupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        leftlowcheck = true;
                                        leftbiceprepscount += 1;
                                        bicepturn = "right";
                                        tts!!.speak("left"+leftbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        leftupcheck = false;
                                    }
                                }
                            }
                        }

                        if (bicepturn == "right") {

                            ///////////////////////////////////////////////Right side HAMMER bicepcurls/////////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    rightupcheck = true;
                                    rightlowcheck = false;

                                }
                                if (rightupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        rightlowcheck = true;
                                        rightbiceprepscount += 1;
                                        bicepturn = "left";
                                        tts!!.speak("right"+rightbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        rightupcheck = false;
                                    }
                                }
                            }


                        }
                        if (armssetno == 1 && rightbiceprepscount + leftbiceprepscount >= 60) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 2;
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 2 && rightbiceprepscount + leftbiceprepscount >= 40) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 3;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 3 && rightbiceprepscount + leftbiceprepscount >= 20) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 1;
                            armsexercise = 4
                            ttsexercisename = false

                        }
                    }


                    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////triceps CURLS////////////////////////////////////////////////////////////////
                    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    else if (armsexercise == 4) {



                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Fourth exercise is seated tricep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == 30) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == 20) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == 10) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("Your arms workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                requireActivity().finish()
                            }
                        }

                    }


                }


                /////////////////////////////////////////////LEGSS/////////////////////////////////
                else if (intenttype == 4) {//LEGSBEG


                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to legs beginner "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you legs exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }

                    if(legsexercise == 1){//SQUATS
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Squats",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 10){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 8){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >=6){
                            squatsetno = 0;
                            squatrepcount = 0;
                            ttsexercisename = false

                        }

                    }
                    else if(legsexercise == 2){//lunges
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is Lunges",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 10){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 8){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >=6){
                            squatsetno = 0;
                            squatrepcount = 0;
                            tts!!.speak("Your legs workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                            requireActivity().finish()
                        }

                    }
                    else if(legsexercise == 3){//leg press
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Third exercise is Leg press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    }

                } else if (intenttype == 5) {//LEGSINTEER


                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to legs Intermediate "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you legs exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }

                    if(legsexercise == 1){//SQUATS
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Squats",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 20){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 15){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >= 10){
                            squatsetno = 0;
                            squatrepcount = 0;
                            ttsexercisename = false

                        }

                    }
                    else if(legsexercise == 2){//lunges
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is Lunges",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 20){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 15){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >= 10){
                            squatsetno = 0;
                            squatrepcount = 0;
                            tts!!.speak("Your legs workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                            requireActivity().finish()
                        }

                    }
                    else if(legsexercise == 3){//leg press
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Third exercise is Leg press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    }

                } else if (intenttype == 6) {//LEGSPRO


                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to legs Professional "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you legs exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }

                    if(legsexercise == 1){//SQUATS
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Squats",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 30){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 20){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >= 10){
                            squatsetno = 0;
                            squatrepcount = 0;
                            ttsexercisename = false

                        }

                    }
                    else if(legsexercise == 2){//lunges
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is Lunges",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= 30){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= 20){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >=10){
                            squatsetno = 0;
                            squatrepcount = 0;
                            tts!!.speak("Your legs workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                            requireActivity().finish()
                        }

                    }
                    else if(legsexercise == 3){//leg press
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Third exercise is Leg press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    }

                }

                //////////////////////////////////////////CHEST////////////////////////////
                else if (intenttype == 7) {//CHESTBEG
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to Chest beginner "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you chest exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(chestexercise == 1){ // pushups

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is push-ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 8) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 6) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(chestexercise == 2){//dumbell flys

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is dumbbell flys",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 8) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 6) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 3;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                    else if(chestexercise == 3){//barbell benchpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell bench press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 8) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 6) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 0;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                } else if (intenttype == 8) {//CHESTINTEER
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to Chest intermediate "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you chest exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(chestexercise == 1){ // pushups

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is push-ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 15) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(chestexercise == 2){//dumbell flys

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is dumbbell flys",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 15) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 3;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                    else if(chestexercise == 3){//barbell benchpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell bench press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 15) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 0;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                } else if (intenttype == 9) {//CHESTPRO
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to Chest professional "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you chest exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(chestexercise == 1){ // pushups

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is push-ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 30) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(chestexercise == 2){//dumbell flys

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is dumbbell flys",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 30) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 3;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                    else if(chestexercise == 3){//barbell benchpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell bench press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == 30) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == 20) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == 10) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 0;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                }


                //////////////////////////////////////////SHOULDER////////////////////////////
                else if (intenttype == 10) {//SHOULDERSBEG

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to shoulders beginner "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you shoulders exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(shoulderexercise == 1){ // shoulder press

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Dumbbell shoulder press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 8) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 6) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(shoulderexercise == 2){//overheadpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell overhead press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 8) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 6) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 0;
                                tts!!.speak("Your shoulder workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                } else if (intenttype == 11) {//SHOULDERINTEER
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to shoulders Intermeiate "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you shoulders exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(shoulderexercise == 1){ // shoulder press

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Dumbbell shoulder press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 20) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 15) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(shoulderexercise == 2){//overheadpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell overhead press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 20) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 15) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 0;
                                tts!!.speak("Your shoulder workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                } else if (intenttype == 12) {//SHOULDERSPRO
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to Shoulder pro "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you shoulder exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to shoulders Professional "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you shoulders exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(shoulderexercise == 1){ // shoulder press

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Dumbbell shoulder press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 30) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 20) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(shoulderexercise == 2){//overheadpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell overhead press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == 30) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == 20) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == 10) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 0;
                                tts!!.speak("Your shoulder workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }
                }


                /////////////////////////////////////////BACK///////////////////////////
                else if (intenttype == 13) {//BACKBEG

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to back beginner "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you back exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(backexercise == 1){//latpulldown
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Lat pulldown",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 8) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 6) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                            }
                        }
                    }
                    else if(backexercise == 2){//chinups
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is chin ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 8) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 6) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }
                    }
                    else if(backexercise == 3){//seatedrowing

                    }
                } else if (intenttype == 14) {//BACKINTEER

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to back Intermediate "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you back exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(backexercise == 1){//latpulldown
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Lat pulldown",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 20) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 15) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                            }
                        }
                    }
                    else if(backexercise == 2){//chinups
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is chin ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 20) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 15) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }
                    }
                    else if(backexercise == 3){//seatedrowing

                    }
                } else if (intenttype == 15) {//BACKPRO
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to back pro "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you back exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(backexercise == 1){//latpulldown
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Lat pulldown",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 30) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 20) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                            }
                        }
                    }
                    else if(backexercise == 2){//chinups
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is chin ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == 30) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == 20) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == 10) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }
                    }
                    else if(backexercise == 3){//seatedrowing

                    }
                }




                ///////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////CUSTOMSS/////////////////////////////////////////////////////////////////////
                /////////////////////////////////////////////////////////////////////////////////////////////////////////////
                else if(intenttype == 16){//armscustom

                    if(customexno == 1){//dumbbellbicepcurls
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "this exercise is dumbble bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == set1) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 2;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == set2) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 3;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == set3) {
                                bicepcurlcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                armssetno = 1;
                                armsexercise = 2;
                                ttsexercisename = false
                                requireActivity().finish()

                            }
                        }
                    }
                    else if(customexno == 2){//barbellbicepcurls
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is barbell bicep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "leftup")
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "leftdown")

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    Log.d("barbelcurl", "rightup")

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;
                                    Log.d("barbelcurl", "rightdown")

                                }
                            }
                        }

                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (armssetno == 1 && bicepcurlcount == set1) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == set2) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == set3) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                ttsexercisename = false
                                requireActivity().finish()

                            }
                        }
                    }
                    else if(customexno == 3){//hammercurls

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is dumbble hammer Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (bicepturn == "left") {
                            /////////////////////////////////////////////////////left side HAMMER bicepcurls//////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    leftupcheck = true;
                                    leftlowcheck = false;

                                }
                                if (leftupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        leftlowcheck = true;
                                        leftbiceprepscount += 1;
                                        bicepturn = "right";
                                        tts!!.speak("left"+leftbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        leftupcheck = false;
                                    }
                                }
                            }
                        }

                        if (bicepturn == "right") {

                            ///////////////////////////////////////////////Right side HAMMER bicepcurls/////////////////////////////////////////////////////////


                            if ((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST)) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                    rightupcheck = true;
                                    rightlowcheck = false;

                                }
                                if (rightupcheck == true) {
                                    if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                        rightlowcheck = true;
                                        rightbiceprepscount += 1;
                                        bicepturn = "left";
                                        tts!!.speak("right"+rightbiceprepscount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                                        rightupcheck = false;
                                    }
                                }
                            }


                        }
                        if (armssetno == 1 && rightbiceprepscount + leftbiceprepscount >= set1*2) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 2;
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 2 && rightbiceprepscount + leftbiceprepscount >= set2*2) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 3;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        } else if (armssetno == 3 && rightbiceprepscount + leftbiceprepscount >= set3*2) {
                            leftbiceprepscount = 0;
                            rightbiceprepscount = 0;
                            armssetno = 1;
                            armsexercise = 4
                            ttsexercisename = false
                            requireActivity().finish();

                        }
                    }
                    else if(customexno == 4){//tricep


                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Fourth exercise is seated tricep Curls",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_WRIST) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_ELBOW))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_WRIST))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                    countedrep = false;

                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            bicepcurlcount += 1;
                            countedrep = true;
                            tts!!.speak(bicepcurlcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")


                            if (armssetno == 1 && bicepcurlcount == set1) {
                                bicepcurlcount = 0;
                                armssetno = 2;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 2 && bicepcurlcount == set2) {
                                bicepcurlcount = 0;
                                armssetno = 3;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (armssetno == 3 && bicepcurlcount == set3) {
                                bicepcurlcount = 0;
                                armssetno = 1;
                                armsexercise = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                tts!!.speak("Your arms workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                requireActivity().finish()
                            }
                        }

                    }

                }
                else if(intenttype == 17){//legscustom

                    if (!ttswelcomed) {
                        ttswelcomed = true;
                        var toSpeak = "Welcome to legs custom "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you legs exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(
                            toSpeak + toSpeak1 + toSpeak2,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            ""
                        )
                    }

                    if(customexno == 1){//SQUATS
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Squats",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= set1){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= set2){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >= set3){
                            squatsetno = 0;
                            squatrepcount = 0;
                            ttsexercisename = false

                        }

                    }
                    else if(legsexercise == 2){//lunges
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is Lunges",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_HIP) || (person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_HIP) ) and ((person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_KNEE) || (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_KNEE )) ) {
                            if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {

                                squatdowncheck = true;
                                squatupcheck = false;

                            }
                            if (squatdowncheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    squatupcheck = true;
                                    squatrepcount += 1;
                                    tts!!.speak(squatrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                    squatdowncheck = false;
                                }
                            }
                        }
                        if(squatsetno == 1 && squatrepcount >= set1){
                            squatsetno = 2;
                            squatrepcount = 0
                            tts!!.speak("second set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 2 && squatrepcount >= set2){
                            squatsetno = 3;
                            squatrepcount = 0;
                            tts!!.speak("third set", TextToSpeech.QUEUE_FLUSH, null, "")

                        }
                        else if(squatsetno == 3 && squatrepcount >= set3){
                            squatsetno = 0;
                            squatrepcount = 0;
                            tts!!.speak("Your legs workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                            requireActivity().finish()
                        }

                    }
                    else if(customexno == 3){//leg press
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Third exercise is Leg press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                    }

                }
                else if(intenttype == 18){//chestcustom
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to Chest custom "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you chest exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(chestexercise == 1){ // pushups

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is push-ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && (leftupcheck == true || rightupcheck == true)) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == set1) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == set2) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == set3) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(chestexercise == 2){//dumbell flys

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is dumbbell flys",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == set1) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == set2) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == set3) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 3;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                    else if(chestexercise == 3){//barbell benchpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell bench press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            chestrepcount += 1;
                            countedrep = true;
                            tts!!.speak(chestrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (chestsetno == 1 && chestrepcount == set1) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 2 && chestrepcount == set2) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (chestsetno == 3 && chestrepcount == set3) {
                                chestrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                chestsetno = 1;
                                chestexercise = 0;
                                tts!!.speak("Your chest workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                }
                else if(intenttype == 19){//shouldercustom

                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to shoulders Custom "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you shoulders exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(customexno == 1){ // shoulder press

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Dumbbell shoulder press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == set1) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == set2) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == set3) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 2;
                                ttsexercisename = false
                            }
                        }

                    }
                    else if(customexno == 2){//overheadpress

                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "Second exercise is barbell overhead press",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            shoulderrepcount += 1;
                            countedrep = true;
                            tts!!.speak(shoulderrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (shouldersetno == 1 && shoulderrepcount == set1) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 2 && shoulderrepcount == set2) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (shouldersetno == 3 && shoulderrepcount == set3) {
                                shoulderrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                shouldersetno = 1;
                                shoulderexercise = 0;
                                tts!!.speak("Your shoulder workout is finished", TextToSpeech.QUEUE_FLUSH, null, "")
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }

                    }

                }
                else if(intenttype == 20){//backcustom
                    if (!ttswelcomed) {

                        var toSpeak = "Welcome to back Custom "
                        var toSpeak1 = "... i will be your assistant today"
                        var toSpeak2 = "... i will guide you through you back exercises"
                        tts!!.setSpeechRate(0.9.toFloat())
                        tts!!.speak(toSpeak + toSpeak1 + toSpeak2, TextToSpeech.QUEUE_FLUSH, null, "")
                        ttswelcomed = true;
                    }

                    if(backexercise == 1){//latpulldown
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is Lat pulldown",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == set1) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == set2) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == set3) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                            }
                        }
                    }
                    else if(backexercise == 2){//chinups
                        if(!tts!!.isSpeaking && !ttsexercisename && ttswelcomed){
                            ttsexercisename = true
                            tts!!.speak(
                                "First exercise is chin ups",
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                ""
                            )
                        }

                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.LEFT_ELBOW) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.LEFT_SHOULDER))) {
                            if (leftupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (leftupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    leftupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (((person.keyPoints[line.first.ordinal].bodyPart == BodyPart.RIGHT_SHOULDER) and (person.keyPoints[line.second.ordinal].bodyPart == BodyPart.RIGHT_ELBOW))) {
                            if (rightupcheck == false) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) > (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = true;
                                    leftlowcheck = false;
                                }
                            }
                            if (rightupcheck == true) {
                                if ((person.keyPoints[line.first.ordinal].position.y.toFloat() * heightRatio) < (person.keyPoints[line.second.ordinal].position.y.toFloat() * heightRatio)) {
                                    rightupcheck = false;
                                    leftlowcheck = true;
                                    countedrep = false;

                                }
                            }
                        }
                        if (countedrep == false && leftupcheck == true && rightupcheck == true) {
                            backrepcount += 1;
                            countedrep = true;
                            tts!!.speak(backrepcount.toString(), TextToSpeech.QUEUE_FLUSH, null, "")

                            if (backsetno == 1 && backrepcount == set1) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 2;
                                tts!!.speak("Second set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 2 && backrepcount == set2) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 3;
                                tts!!.speak("Third set", TextToSpeech.QUEUE_FLUSH, null, "")

                            } else if (backsetno == 3 && backrepcount == set3) {
                                backrepcount = 0;
                                leftupcheck = false;
                                rightupcheck = false;
                                backsetno = 1;
                                backexercise = 2;
                                ttsexercisename = false
                                requireActivity().finish()
                            }
                        }
                    }
                    else if(backexercise == 3){//seatedrowing

                    }

                }


            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////diaplaying reps and exercise////////////////////////////////////////////\
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        if (intenttype == 1) {// counting reps

            var outof = 0;
            if (armssetno == 1) {
                outof = 10
            } else if (armssetno == 2) {
                outof = 8
            } else if (armssetno == 3) {
                outof = 6
            }


            if (armsexercise == 1) {

                canvas.drawText(
                    "Exercise: Dumbbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 2) {
                canvas.drawText(
                    "Exercise: Barbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 3) {
                canvas.drawText(
                    "Exercise: Hammer BicepCurls",
                    (15.0f * widthRatio),
                    (30.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Left Bicep Reps count: " + leftbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Right Bicep Reps count: " + rightbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Hammer BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 4) {
                canvas.drawText(
                    "Exercise: Seated TricepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 2) {//ARMSINTERMEDIATE

            var outof = 0;

            if (armssetno == 1) {
                outof = 20
            } else if (armssetno == 2) {
                outof = 15
            } else if (armssetno == 3) {
                outof = 10
            }

            if (armsexercise == 1) {
                canvas.drawText(
                    "Exercise: Dumbbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 2) {
                canvas.drawText(
                    "Exercise: Barbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 3) {
                canvas.drawText(
                    "Exercise: Hammer BicepCurls",
                    (15.0f * widthRatio),
                    (30.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Left Bicep Reps count: " + leftbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Right Bicep Reps count: " + rightbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Hammer BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 4) {
                canvas.drawText(
                    "Exercise: Seated TricepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }

        } else if (intenttype == 3) {//ARMSPRO

            var outof = 0;

            if (armssetno == 1) {
                outof = 30
            } else if (armssetno == 2) {
                outof = 20
            } else if (armssetno == 3) {
                outof = 10
            }

            if (armsexercise == 1) {
                canvas.drawText(
                    "Exercise: Dumbbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 2) {
                canvas.drawText(
                    "Exercise: Barbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 3) {
                canvas.drawText(
                    "Exercise: Hammer BicepCurls",
                    (15.0f * widthRatio),
                    (30.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Left Bicep Reps count: " + leftbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Right Bicep Reps count: " + rightbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Hammer BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            } else if (armsexercise == 4) {
                canvas.drawText(
                    "Exercise: Seated TricepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }


        /////////////////////////////////////////////LEGSS/////////////////////////////////
        else if (intenttype == 4) {//LEGSBEG
            var outof = 0;

            if (squatsetno == 1) {
                outof = 10
            } else if (squatsetno == 2) {
                outof = 8
            } else if (squatsetno == 3) {
                outof = 6
            }
            if (legsexercise == 1) {//squats
                canvas.drawText(
                    "Exercise: Squats",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 2){//lunges
                canvas.drawText(
                    "Exercise: Lunges",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 3){//leg raises

            }

        } else if (intenttype == 5) {//LEGSINTEER
            var outof = 0;

            if (squatsetno == 1) {
                outof = 20
            } else if (squatsetno == 2) {
                outof = 15
            } else if (squatsetno == 3) {
                outof = 10
            }
            if (legsexercise == 1) {//squats
                canvas.drawText(
                    "Exercise: Squats",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 2){//lunges
                canvas.drawText(
                    "Exercise: Lunges",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 3){//leg raises

            }
        } else if (intenttype == 6) {//LEGSPRO

            var outof = 0;

            if (squatsetno == 1) {
                outof = 30
            } else if (squatsetno == 2) {
                outof = 20
            } else if (squatsetno == 3) {
                outof = 10
            }
            if (legsexercise == 1) {//squats
                canvas.drawText(
                    "Exercise: Squats",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 2){//lunges
                canvas.drawText(
                    "Exercise: Lunges",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(legsexercise == 3){//leg raises

            }
        }



        else if (intenttype == 7) {//CHESTBEG
            var outof = 0;

            if (chestsetno == 1) {
                outof = 10
            } else if (chestsetno == 2) {
                outof = 8
            } else if (chestsetno == 3) {
                outof = 6
            }
            if (chestexercise == 1) {//pushups
                canvas.drawText(
                    "Exercise: Push-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 2){//dumbbell flys
                canvas.drawText(
                    "Exercise: Dumbbell Flys",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 3){//barbell benchpress
                canvas.drawText(
                    "Exercise: barbell benchpress",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 8) {//CHESTINTEER
            var outof = 0;

            if (chestsetno == 1) {
                outof = 20
            } else if (chestsetno == 2) {
                outof = 15
            } else if (chestsetno == 3) {
                outof = 10
            }
            if (chestexercise == 1) {//pushups
                canvas.drawText(
                    "Exercise: Push-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 2){//dumbbell flys
                canvas.drawText(
                    "Exercise: Dumbbell Flys",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 3){//barbell benchpress
                canvas.drawText(
                    "Exercise: barbell benchpress",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 9) {//CHESTPRO
            var outof = 0;

            if (chestsetno == 1) {
                outof = 30
            } else if (chestsetno == 2) {
                outof = 20
            } else if (chestsetno == 3) {
                outof = 10
            }
            if (chestexercise == 1) {//pushups
                canvas.drawText(
                    "Exercise: Push-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 2){//dumbbell flys
                canvas.drawText(
                    "Exercise: Dumbbell Flys",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 3){//barbell benchpress
                canvas.drawText(
                    "Exercise: barbell benchpress",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }


        //////////////////////////////////////////Shoulder////////////////////////////
        else if (intenttype == 10) {//SHOULDERSBEG
            var outof = 0;

            if (shouldersetno == 1) {
                outof = 10
            } else if (shouldersetno == 2) {
                outof = 8
            } else if (shouldersetno == 3) {
                outof = 6
            }
            if (shoulderexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Shoulder press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(shoulderexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Overhead press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overhead press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overheadpress Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 11) {//SHOULDERINTEER
            var outof = 0;

            if (shouldersetno == 1) {
                outof = 20
            } else if (shouldersetno == 2) {
                outof = 15
            } else if (shouldersetno == 3) {
                outof = 10
            }
            if (shoulderexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Shoulder press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(shoulderexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Overhead press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overhead press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overheadpress Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 12) {//SHOULDERSPRO
            var outof = 0;

            if (shouldersetno == 1) {
                outof = 30
            } else if (shouldersetno == 2) {
                outof = 20
            } else if (shouldersetno == 3) {
                outof = 10
            }
            if (shoulderexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Shoulder press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(shoulderexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Overhead press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overhead press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overheadpress Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }


        /////////////////////////////////////////BACK///////////////////////////
        else if (intenttype == 13) {//BACKBEG
            var outof = 0;

            if (backsetno == 1) {
                outof = 10
            } else if (backsetno == 2) {
                outof = 8
            } else if (backsetno == 3) {
                outof = 6
            }
            if (backexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Lat Pulldown",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(backexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Chin-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 14) {//BACKINTEER
            var outof = 0;

            if (backsetno == 1) {
                outof = 20
            } else if (backsetno == 2) {
                outof = 15
            } else if (backsetno == 3) {
                outof = 10
            }
            if (backexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Lat Pulldown",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(backexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Chin-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        } else if (intenttype == 15) {//BACKPRO
            var outof = 0;

            if (backsetno == 1) {
                outof = 30
            } else if (backsetno == 2) {
                outof = 20
            } else if (backsetno == 3) {
                outof = 10
            }
            if (backexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Lat Pulldown",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(backexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Chin-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }



        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////CUSTOMSS/////////////////////////////////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////////
        else if(intenttype == 16){//armscustom
            var outof = 0;
            if (armssetno == 1) {
                outof = set1
            } else if (armssetno == 2) {
                outof = set2
            } else if (armssetno == 3) {
                outof = set3
            }
            if (customexno == 1) {
                canvas.drawText(
                    "Exercise: Dumbbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 2){
                canvas.drawText(
                    "Exercise: Barbell BicepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell Bicepcurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Barbell BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 3){
                canvas.drawText(
                    "Exercise: Hammer BicepCurls",
                    (15.0f * widthRatio),
                    (30.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Left Bicep Reps count: " + leftbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Right Bicep Reps count: " + rightbiceprepscount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Hammer BicepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 4){
                canvas.drawText(
                    "Exercise: Seated TricepCurls",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Reps count: " + bicepcurlcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Seated TricepCurl Set : " + armssetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }

        }
        else if(intenttype == 17){//legscustom
            var outof = 0;
            if (armssetno == 1) {
                outof = set1
            } else if (armssetno == 2) {
                outof = set2
            } else if (armssetno == 3) {
                outof = set3
            }
            if (customexno == 1) {//squats
                canvas.drawText(
                    "Exercise: Squats",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Squats Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 2){//lunges
                canvas.drawText(
                    "Exercise: Lunges",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Reps count: " + squatrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lunges Set : " + squatsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 3){//leg raises

            }

        }
        else if(intenttype == 18){//chestcustom
            var outof = 0;

            if (chestsetno == 1) {
                outof = set1
            } else if (chestsetno == 2) {
                outof = set2
            } else if (chestsetno == 3) {
                outof = set3
            }
            if (chestexercise == 1) {//pushups
                canvas.drawText(
                    "Exercise: Push-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Push-ups Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 2){//dumbbell flys
                canvas.drawText(
                    "Exercise: Dumbbell Flys",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Dumbbell Flys Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(chestexercise == 3){//barbell benchpress
                canvas.drawText(
                    "Exercise: barbell benchpress",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Reps count: " + chestrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "barbell benchpress Set : " + chestsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }

        }
        else if(intenttype == 19){//shouldercustom
            var outof = 0;
            if (armssetno == 1) {
                outof = set1
            } else if (armssetno == 2) {
                outof = set2
            } else if (armssetno == 3) {
                outof = set3
            }
            if (customexno == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Shoulder press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Shoulder press Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(customexno == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Overhead press",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overhead press Reps count: " + shoulderrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Overheadpress Set : " + shouldersetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }
        else if(intenttype == 20){//backcustom
            var outof = 0;

            if (backsetno == 1) {
                outof = set1
            } else if (backsetno == 2) {
                outof = set2
            } else if (backsetno == 3) {
                outof = set3
            }
            if (backexercise == 1) {//dumbbell shoulder  press
                canvas.drawText(
                    "Exercise: Lat Pulldown",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Lat Pulldown Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
            else if(backexercise == 2){//overheadpress
                canvas.drawText(
                    "Exercise: Chin-ups",
                    (15.0f * widthRatio),
                    (50.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Reps count: " + backrepcount + "/" + outof,
                    (15.0f * widthRatio),
                    (70.0f * heightRatio + bottom),
                    paint
                )
                canvas.drawText(
                    "Chin-ups Set : " + backsetno,
                    (15.0f * widthRatio),
                    (90.0f * heightRatio + bottom),
                    paint
                )
            }
        }


/*        canvas.drawText(
            "Score: %.2f".format(person.score),
            (15.0f * widthRatio),
            (30.0f * heightRatio + bottom),
            paint
        )
        canvas.drawText(
            "Device: %s".format(posenet.device),
            (15.0f * widthRatio),
            (50.0f * heightRatio + bottom),
            paint
        )
        canvas.drawText(
            "Time: %.2f ms".format(posenet.lastInferenceTimeNanos * 1.0f / 1_000_000),
            (15.0f * widthRatio),
            (70.0f * heightRatio + bottom),
            paint
        )*/

        // Draw!
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }

    /** Process image using Posenet library.   */
    private fun processImage(bitmap: Bitmap) {
        // Crop bitmap.
        val croppedBitmap = cropBitmap(bitmap)

        // Created scaled version of bitmap for model input.
        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, MODEL_WIDTH, MODEL_HEIGHT, true)

        // Perform inference.
        val person = posenet.estimateSinglePose(scaledBitmap)
        val canvas: Canvas = surfaceHolder!!.lockCanvas()
        draw(canvas, person, scaledBitmap)
    }

    /**
     * Creates a new [CameraCaptureSession] for camera preview.
     */
    private fun createCameraPreviewSession() {
        try {
            // We capture images from preview in YUV format.
            imageReader = ImageReader.newInstance(
                previewSize!!.width, previewSize!!.height, ImageFormat.YUV_420_888, 2
            )
            imageReader!!.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)

            // This is the surface we need to record images for processing.
            val recordingSurface = imageReader!!.surface

            // We set up a CaptureRequest.Builder with the output Surface.
            previewRequestBuilder = cameraDevice!!.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW
            )
            previewRequestBuilder!!.addTarget(recordingSurface)

            // Here, we create a CameraCaptureSession for camera preview.
            cameraDevice!!.createCaptureSession(
                listOf(recordingSurface),
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        // The camera is already closed
                        if (cameraDevice == null) return

                        // When the session is ready, we start displaying the preview.
                        captureSession = cameraCaptureSession
                        try {
                            // Auto focus should be continuous for camera preview.
                            previewRequestBuilder!!.set(
                                CaptureRequest.CONTROL_AF_MODE,
                                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                            )
                            // Flash is automatically enabled when necessary.
                            setAutoFlash(previewRequestBuilder!!)

                            // Finally, we start displaying the camera preview.
                            previewRequest = previewRequestBuilder!!.build()
                            captureSession!!.setRepeatingRequest(
                                previewRequest!!,
                                captureCallback, backgroundHandler
                            )
                        } catch (e: CameraAccessException) {
                            Log.e(TAG, e.toString())
                        }
                    }

                    override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                        showToast("Failed")
                    }
                },
                null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
    }

    private fun setAutoFlash(requestBuilder: CaptureRequest.Builder) {
        if (flashSupported) {
            requestBuilder.set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH
            )
        }
    }

    /**
     * Shows an error message dialog.
     */
    class ErrorDialog : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
            AlertDialog.Builder(activity)
                .setMessage(requireArguments().getString(ARG_MESSAGE))
                .setPositiveButton(android.R.string.ok) { _, _ -> requireActivity().finish() }
                .create()

        companion object {

            @JvmStatic
            private val ARG_MESSAGE = "message"

            @JvmStatic
            fun newInstance(message: String): ErrorDialog = ErrorDialog().apply {
                arguments = Bundle().apply { putString(ARG_MESSAGE, message) }
            }
        }
    }

    companion object {
        /**
         * Conversion from screen rotation to JPEG orientation.
         */
        private val ORIENTATIONS = SparseIntArray()
        private val FRAGMENT_DIALOG = "dialog"

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        /**
         * Tag for the [Log].
         */
        private const val TAG = "PosenetActivity"
    }

    override fun onInit(p0: Int) {
        if (p0 == TextToSpeech.SUCCESS) {
            // set US English as language for tts
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language specified is not supported!")
            } else {
            }

        } else {
            Log.e("TTS", "Initilization Failed!")
        }
    }
}
