package com.example.pecscreator

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModelProvider
import com.example.pecscreator.databinding.ActivityMainBinding
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService


class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, TakePhoto::class.java)
            startActivity(intent)
        }

        val db = CardsDatabase.getInstance(this)
        val dao = db.cardsDao()


        val cd = dao.getAll()
        if (cd.size > 0) {
            binding.imageView.setImageBitmap(BitmapFactory.decodeFile(cd.get(cd.size-1).imageUri))
        }


    }









}