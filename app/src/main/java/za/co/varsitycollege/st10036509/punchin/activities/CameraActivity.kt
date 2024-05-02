package za.co.varsitycollege.st10036509.punchin.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import za.co.varsitycollege.st10036509.punchin.R
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityCameraBinding
import za.co.varsitycollege.st10036509.punchin.databinding.ActivityTimesheetCreationBinding
import za.co.varsitycollege.st10036509.punchin.utils.IntentHandler

class CameraActivity : AppCompatActivity(){
    private lateinit var binding: ActivityCameraBinding
    private var REQUEST_CODE = 22
    lateinit var btnCamera: Button
    lateinit var imageView: ImageView
    private var capturedPhoto: Bitmap? = null
    private lateinit var intentHandler: IntentHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        btnCamera = binding.btnCamera
        imageView = binding.imageView

        binding.btnReturn.setOnClickListener { returnToCreate() }
        binding.btnSave.setOnClickListener { sendPhotoToNextActivity() }

        btnCamera.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Start the camera activity
            startActivityForResult(cameraIntent, REQUEST_CODE)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            capturedPhoto = data?.extras?.get("data") as Bitmap?
            imageView.setImageBitmap(capturedPhoto)

        }
        else {

            super.onActivityResult(requestCode, resultCode, data)

        }
    }

    // Function to send the captured photo to another activity
    private fun sendPhotoToNextActivity() {
        capturedPhoto?.let { photo ->
            val intent = Intent(this, ActivityTimesheetCreationBinding::class.java)
            intent.putExtra("photo", photo) // Pass the photo bitmap to the intent
            startActivity(intent)
        }
    }

    private fun returnToCreate() {

        intentHandler.openActivityIntent(TimesheetCreationActivity::class.java)

    }

}