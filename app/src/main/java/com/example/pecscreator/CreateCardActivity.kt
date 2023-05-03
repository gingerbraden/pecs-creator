package com.example.pecscreator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
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
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.streams.toList

class CreateCardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCreateCardBinding

    private val viewModel: MainViewModel by viewModels()

    lateinit var module : PyObject

    var coords : MutableList<Int> = mutableListOf()

    var oldRect : Rect? = Rect()

    var editedBmp : Bitmap? = null

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
        binding.cropImageView.setAspectRatio(600, 700)
        oldRect = binding.cropImageView.cropRect

        binding.cropButton.setOnClickListener {
            if (coords.isNotEmpty()) {
                binding.cropImageView.resetCropRect()
                binding.cropImageView.cropRect = Rect(coords[0], coords[1], coords[0]+coords[2], coords[1]+coords[3])
            }
        }

        binding.rotateButton.setOnClickListener {
            rotateImageAndCoordinates()
        }

        binding.resetButton.setOnClickListener {
            binding.cropImageView.cropRect = oldRect
            binding.cropImageView.resetCropRect()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        invalidateOptionsMenu()
        return super.onPrepareOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PECS Creator")
        if (!dir.exists()) dir.mkdir()

        val card = Card(
            binding.textView.text.toString(),
            binding.cropImageView.getCroppedImage(600, 700)?.let {
                Bitmap.createScaledBitmap(
                    it, 600, 700, false)
            })

        val db = CardsDatabase.getInstance(this)
        val dao = db.cardsDao()
        dao.insert(card)
        val file2 = File(dir, viewModel.savedPhotoName + ".jpg")
        file2.delete()

        val intent = Intent(this@CreateCardActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val dir: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/PECS Creator")
        if (!dir.exists()) dir.mkdir()

        val file2 = File(dir, viewModel.savedPhotoName + ".jpg")
        file2.delete()
    }


    override fun onStart() {
        super.onStart()
        getCroppedCoordinates()
    }

    fun getCroppedCoordinates() = run {
        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, viewModel.savedPhotoUri)
            if (bitmap.width <= 1500) {
                val bytes = module.callAttr("returnCoordinates",
                    getStringFromImageView(bitmap)
                )
                coords = bytes.toString().split(":").stream().map { x -> x.toInt() }.toList().toMutableList()
            } else {
                val bytes = module.callAttr("returnCoordinates",
                    getStringFromImageView(Bitmap.createScaledBitmap(bitmap, bitmap.width/2, bitmap.width/2, false))
                )
                coords = bytes.toString().split(":").stream().map { x -> x.toInt()*2 }.toList().toMutableList()
            }
            editedBmp = bitmap
            editCoordsToSquare()
            binding.cropButton.isClickable = true
            binding.cropButton.alpha = 1F
        }
    }

    fun editCoordsToSquare() {

        val oldW = coords[2]
        val oldH = coords[3]

        if (oldW > oldH) {
            val diff = (oldW - oldH) / 2
            coords[1] = coords[1] - diff
            coords[3] = coords[3] + diff*2
        }

        if (oldW < oldH) {
            val diff = (oldH - oldW) / 2
            coords[0] = coords[0] - diff
            coords[2] = coords[2] + diff*2
        }
    }

    fun getStringFromImageView(b : Bitmap) : String {
        val baos = ByteArrayOutputStream()
        b.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        val imageBytes = baos.toByteArray()
        val encodedImage = android.util.Base64.encodeToString(imageBytes, android.util.Base64.DEFAULT)
        return encodedImage
    }

    fun rotateImageAndCoordinates() {
        binding.cropImageView.rotateImage(90)
    }




}