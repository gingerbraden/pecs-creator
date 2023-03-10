package com.example.pecscreator

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.pecscreator.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService


class MainActivity : AppCompatActivity() {

    private var imageCapture: ImageCapture? = null

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener {
            val intent = Intent(this@MainActivity, TakePhoto::class.java)
            startActivity(intent)
        }


    }









}