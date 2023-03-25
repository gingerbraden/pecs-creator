package com.example.pecscreator

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.pecscreator.databinding.ActivityCreateCardBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.streams.toList

class CreateCardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCreateCardBinding

    private val viewModel: MainViewModel by viewModels()

    lateinit var module : PyObject

    var coords : MutableList<Int> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getSupportActionBar()?.setTitle("Edit Card");

        if (! Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }

        val py = Python.getInstance()
        module = py.getModule("opencv-scripts")

        val extras = intent.extras
        if (extras != null) {
            val uri: Uri? = intent.getParcelableExtra("uri")
            val name: String? = intent.getStringExtra("name")
            if (uri != null && name != null) {
                viewModel.savedPhotoUri = uri
                viewModel.module = module
                viewModel.savedPhotoName = name
            }
        }



        binding.cropImageView.setImageUriAsync(viewModel.savedPhotoUri)
//        binding.cropImageView.setAspectRatio(1, 1)


        //TODO vyratanie s ohladom na stvorcovy aspect ratio
        binding.cropButton.setOnClickListener {
            if (coords.isNotEmpty()) {
                binding.cropImageView.cropRect = Rect(coords[0], coords[1], coords[0]+coords[2], coords[1]+coords[3])
            }
        }

        binding.rotateButton.setOnClickListener {
            rotateImageAndCoordinates()
        }

        //TODO not working with autocrop
        binding.resetButton.setOnClickListener {
            binding.cropImageView.resetCropRect()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {


        val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PECS Creator")
        if (!dir.exists()) dir.mkdir()
        val file = File(dir, viewModel.savedPhotoName + "E.jpg")



        val resolver = application.contentResolver
        val imagesCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, viewModel.savedPhotoName + "E.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "PECS Creator")
        }
        val imageUri = resolver.insert(imagesCollection, imageDetails)
        val outputStream = resolver.openOutputStream(imageUri!!)
        binding.cropImageView.getCroppedImage()?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        outputStream?.flush()
        outputStream?.close()

        val card = Card(binding.textView.text.toString(), file.absolutePath)

        val db = CardsDatabase.getInstance(this)
        val dao = db.cardsDao()
        dao.insert(card)
        val file2 = File(dir, viewModel.savedPhotoName + ".jpg")
        file2.delete()



        val intent = Intent(this@CreateCardActivity, MainActivity::class.java)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        Log.d("ahoj", "zaciatok koordinacky")
        getCroppedCoordinates()

    }

    fun getCroppedCoordinates() = run {
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1500)
            val bytes = module.callAttr("returnCoordinates",
                getStringFromImageView(binding.cropImageView.getCroppedImage()!!))
            coords = bytes.toString().split(":").stream().map { x -> x.toInt() }.toList().toMutableList()
            Log.d("ahoj", "hotovo koordinacky")
            binding.cropButton.isClickable = true
            binding.cropButton.alpha = 1F
        }
    }

    fun getStringFromImageView(b : Bitmap) : String {
        val baos = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
        return encodedImage
    }

    //TODO NOT WOKRING PROPERLY
    fun rotateImageAndCoordinates() {
        binding.cropImageView.rotateImage(90)
//        val oldX = coords[0]
//        coords[0] = -coords[1]
//        coords[1] = oldX
//
//        val oldWidth = coords[2]
//        coords[2] = coords[3]
//        coords[3] = oldWidth
    }




}