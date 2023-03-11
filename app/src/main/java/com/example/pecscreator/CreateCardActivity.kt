package com.example.pecscreator

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pecscreator.databinding.ActivityCreateCardBinding
import com.example.pecscreator.ui.takephoto.CreateCardViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import kotlin.streams.toList

class CreateCardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCreateCardBinding

    private val viewModel: CreateCardViewModel by viewModels()

    lateinit var module : PyObject

    var coords : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        module = py.getModule("opencv-scripts")

        val extras = intent.extras
        if (extras != null) {
            val uri: Uri? = intent.getParcelableExtra("uri")
            if (uri != null) {
                viewModel.savedPhotoUri = uri
                viewModel.module = module
            }
        }

        binding.cropImageView.setImageUriAsync(viewModel.savedPhotoUri)



        binding.button.setOnClickListener {

            if (!coords.equals("")) {
                val coords = coords.split(":").stream().map { x -> x.toInt() }.toList()
                binding.cropImageView.cropRect = Rect(coords[0], coords[1], coords[0]+coords[2], coords[1]+coords[3])
            }

        }

    }

    override fun onStart() {
        super.onStart()
        getCroppedCoordinates()

    }

    fun getCroppedCoordinates() = run {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1500)
            val bytes = module.callAttr("returnCoordinates",
                getStringFromImageView(binding.cropImageView.getCroppedImage()!!))
            coords = bytes.toString()
            Log.d("ahoj", "hotovo koordinacky")
            binding.button.isClickable = true
            binding.button.alpha = 1F
        }
    }

    suspend fun getStringFromImageView(b : Bitmap) : String {
        val baos = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.PNG, 10, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
        return encodedImage
    }












}