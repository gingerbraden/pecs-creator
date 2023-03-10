package com.example.pecscreator

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.pecscreator.databinding.ActivityCreateCardBinding
import com.example.pecscreator.ui.takephoto.CropPhoto
import com.example.pecscreator.ui.takephoto.CreateCardViewModel

class CreateCardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCreateCardBinding

    private val viewModel: CreateCardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            val uri: Uri? = intent.getParcelableExtra("uri")
            if (uri != null) {
                viewModel.savedPhotoUri = uri
                Log.d("AHOJ", "IDE TO")
            }
            //The key argument here must match that used in the other activity
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, CropPhoto.newInstance(viewModel))
                .commitNow()
        }




    }








}