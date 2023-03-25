package com.example.pecscreator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pecscreator.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    // creating variable that handles Animations loading
    // and initializing it with animation files that we have created
    private  val rotateOpen : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_open_anim) }
    private  val rotateClose : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.rotate_close_anim) }
    private  val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim) }
    private  val toBottom : Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim) }
    //used to check if fab menu are opened or closed
    private var closed = false
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private lateinit var db : CardsDatabase
    private lateinit var dao : CardDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)


        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        val all = dao.getAll()





        val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {

                val intent = Intent(this@MainActivity, CreateCardActivity::class.java)
                intent.putExtra("uri", uri)
                intent.putExtra("name", "fromGallery-" + SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                    .format(System.currentTimeMillis()))
                startActivity(intent)
            }
        }


        binding.mainButton.setOnClickListener {
            OnAddButtonClick()
        }

        binding.cameraButton.setOnClickListener {
            val intent = Intent(this@MainActivity, TakePhoto::class.java)
            startActivity(intent)
        }

        binding.galleryButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }



        binding.recyclerView.apply {

            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = CardsAdapter(all)
        }


    }

    private fun OnAddButtonClick() {
        setVisibility(closed)
        setAnimation(closed)
        closed = !closed;
    }


    private fun setAnimation(closed:Boolean) {
        if(!closed){
            binding.cameraButton.startAnimation(fromBottom)
            binding.galleryButton.startAnimation(fromBottom)
            binding.mainButton.startAnimation(rotateOpen)
        }else{
            binding.cameraButton.startAnimation(toBottom)
            binding.galleryButton.startAnimation(toBottom)
            binding.mainButton.startAnimation(rotateClose)
        }
    }


    private fun setVisibility(closed:Boolean) {
        if(!closed)
        {
            binding.cameraButton.visibility = View.VISIBLE
            binding.galleryButton.visibility = View.VISIBLE
        }else{
            binding.cameraButton.visibility = View.INVISIBLE
            binding.galleryButton.visibility = View.INVISIBLE
        }
    }









}