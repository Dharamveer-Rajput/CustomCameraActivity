package rotation.com.customcameraactivity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.Camera.PictureCallback
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

import java.io.IOException

class CameraActivity : Activity(), PictureCallback, SurfaceHolder.Callback {

    private var mCamera: Camera? = null
    private var mCameraImage: ImageView? = null
    private var mCameraPreview: SurfaceView? = null
    private var mCaptureImageButton: Button? = null
    private var mCameraData: ByteArray? = null
    private var mIsCapturing: Boolean = false

    private val mCaptureImageButtonClickListener = OnClickListener { captureImage() }

    private val mRecaptureImageButtonClickListener = OnClickListener { setupImageCapture() }

    private val mDoneButtonClickListener = OnClickListener {
        if (mCameraData != null) {
            val intent = Intent()
            intent.putExtra(EXTRA_CAMERA_DATA, mCameraData)
            setResult(Activity.RESULT_OK, intent)
        } else {
            setResult(Activity.RESULT_CANCELED)
        }
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_camera)

        mCameraImage = findViewById<View>(R.id.camera_image_view) as ImageView
        mCameraImage!!.visibility = View.INVISIBLE

        mCameraPreview = findViewById<View>(R.id.preview_view) as SurfaceView
        val surfaceHolder = mCameraPreview!!.holder
        surfaceHolder.addCallback(this)
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)

        mCaptureImageButton = findViewById<View>(R.id.capture_image_button) as Button
        mCaptureImageButton!!.setOnClickListener(mCaptureImageButtonClickListener)

        val doneButton = findViewById<View>(R.id.done_button) as Button
        doneButton.setOnClickListener(mDoneButtonClickListener)

        mIsCapturing = true
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putBoolean(KEY_IS_CAPTURING, mIsCapturing)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        mIsCapturing = savedInstanceState.getBoolean(KEY_IS_CAPTURING, mCameraData == null)
        if (mCameraData != null) {
            setupImageDisplay()
        } else {
            setupImageCapture()
        }
    }

    override fun onResume() {
        super.onResume()

        if (mCamera == null) {
            try {
                mCamera = Camera.open()
                mCamera!!.setPreviewDisplay(mCameraPreview!!.holder)
                if (mIsCapturing) {
                    mCamera!!.startPreview()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CameraActivity, "Unable to open camera.", Toast.LENGTH_LONG)
                        .show()
            }

        }
    }

    override fun onPause() {
        super.onPause()

        if (mCamera != null) {
            mCamera!!.release()
            mCamera = null
        }
    }

    override fun onPictureTaken(data: ByteArray, camera: Camera) {
        mCameraData = data
        setupImageDisplay()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (mCamera != null) {
            try {
                mCamera!!.setPreviewDisplay(holder)
                if (mIsCapturing) {
                    mCamera!!.startPreview()
                }
            } catch (e: IOException) {
                Toast.makeText(this@CameraActivity, "Unable to start camera preview.", Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {}

    private fun captureImage() {
        mCamera!!.takePicture(null, null, this)
    }

    private fun setupImageCapture() {
        mCameraImage!!.visibility = View.INVISIBLE
        mCameraPreview!!.visibility = View.VISIBLE
        mCamera!!.startPreview()
        mCaptureImageButton!!.setText(R.string.capture_image)
        mCaptureImageButton!!.setOnClickListener(mCaptureImageButtonClickListener)
    }

    private fun setupImageDisplay() {
        val bitmap = BitmapFactory.decodeByteArray(mCameraData, 0, mCameraData!!.size)
        mCameraImage!!.setImageBitmap(bitmap)
        mCamera!!.stopPreview()
        mCameraPreview!!.visibility = View.INVISIBLE
        mCameraImage!!.visibility = View.VISIBLE
        mCaptureImageButton!!.setText(R.string.recapture_image)
        mCaptureImageButton!!.setOnClickListener(mRecaptureImageButtonClickListener)
    }

    companion object {

        val EXTRA_CAMERA_DATA = "camera_data"

        private val KEY_IS_CAPTURING = "is_capturing"
    }
}
