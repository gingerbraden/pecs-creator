package com.example.pecscreator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModelProvider
import com.example.pecscreator.databinding.ActivityMainBinding
import java.io.File
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.mainButton.setOnClickListener {
            OnAddButtonClick()
        }

        binding.cameraButton.setOnClickListener {
            val intent = Intent(this@MainActivity, TakePhoto::class.java)
            startActivity(intent)
        }

        binding.galleryButton.setOnClickListener {
            Toast.makeText(this,"Search",Toast.LENGTH_SHORT).show();
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