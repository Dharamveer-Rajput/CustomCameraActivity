package rotation.com.customcameraactivity

import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast

class MainActivity : Activity() {

    private var mCameraImageView: ImageView? = null
    private var mCameraBitmap: Bitmap? = null
    private var mSaveImageButton: Button? = null

    private val mCaptureImageButtonClickListener = OnClickListener { startImageCapture() }

    private val mSaveImageButtonClickListener = OnClickListener {
        val saveFile = openFileForImage()
        if (saveFile != null) {
            saveImageToFile(saveFile)
        } else {
            Toast.makeText(this@MainActivity, "Unable to open file for saving image.",
                    Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val MY_PERMISSIONS_REQUEST_CAMERA = 0
        val MY_PERMI_WRITE_EXTERNAL_STORAGE = 0
        // Here, this is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMI_WRITE_EXTERNAL_STORAGE)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }



        mCameraImageView = findViewById<View>(R.id.camera_image_view) as ImageView

        findViewById<View>(R.id.capture_image_button).setOnClickListener(mCaptureImageButtonClickListener)

        mSaveImageButton = findViewById<View>(R.id.save_image_button) as Button
        mSaveImageButton!!.setOnClickListener(mSaveImageButtonClickListener)
        mSaveImageButton!!.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == Activity.RESULT_OK) {
                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap = null
                }
                val extras = data.extras
                mCameraBitmap = extras!!.get("data") as Bitmap
                val cameraData = extras.getByteArray(CameraActivity.EXTRA_CAMERA_DATA)
                if (cameraData != null) {
                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.size)
                    mCameraImageView!!.setImageBitmap(mCameraBitmap)
                    mSaveImageButton!!.isEnabled = true
                }
            } else {
                mCameraBitmap = null
                mSaveImageButton!!.isEnabled = false
            }
        }
    }

    private fun startImageCapture() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B)
        startActivityForResult(Intent(this@MainActivity, CameraActivity::class.java), TAKE_PICTURE_REQUEST_B)
    }

    private fun openFileForImage(): File? {
        var imageDirectory: File? = null
        val storageState = Environment.getExternalStorageState()
        if (storageState == Environment.MEDIA_MOUNTED) {
            imageDirectory = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "a3dyou")
            if (!imageDirectory.exists() && !imageDirectory.mkdirs()) {
                imageDirectory = null
            } else {
                val dateFormat = SimpleDateFormat("yyyy_mm_dd_hh_mm",
                        Locale.getDefault())

                return File(imageDirectory.path +
                        File.separator + "image_" +
                        dateFormat.format(Date()) + ".png")
            }
        }
        return null
    }

    private fun saveImageToFile(file: File?) {
        if (mCameraBitmap != null) {
            var outStream: FileOutputStream? = null
            try {
                outStream = FileOutputStream(file!!)
                if (!mCameraBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                    Toast.makeText(this@MainActivity, "Unable to save image to file.",
                            Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "Saved image to: " + file.path,
                            Toast.LENGTH_LONG).show()
                }
                outStream.close()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Unable to save image to file.",
                        Toast.LENGTH_LONG).show()
            }

        }
    }

    companion object {

        private val TAKE_PICTURE_REQUEST_B = 100
    }
}