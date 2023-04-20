package com.example.pecscreator

import android.content.ClipData
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pecscreator.databinding.ActivityMainBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var viewModel: MainViewModel

    // creating variable that handles Animations loading
    // and initializing it with animation files that we have created
    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            this,
            R.anim.to_bottom_anim
        )
    }

    //used to check if fab menu are opened or closed
    private var closed = false
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private lateinit var db: CardsDatabase
    private lateinit var dao: CardDao



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        val all = dao.getAll()

        val nameObserver = androidx.lifecycle.Observer<Int> { x ->
            if (x >= 1) {
                binding.deleteFab.visibility = View.VISIBLE
                binding.exportFab.visibility = View.VISIBLE
                binding.mainButton.visibility = View.INVISIBLE
            } else {
                binding.deleteFab.visibility = View.INVISIBLE
                binding.exportFab.visibility = View.INVISIBLE
                binding.mainButton.visibility = View.VISIBLE
            }
            invalidateOptionsMenu()
        }

        viewModel.numOfCards.observe(this, nameObserver)


        val pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    val intent = Intent(this@MainActivity, CreateCardActivity::class.java)
                    intent.putExtra("uri", uri)
                    intent.putExtra(
                        "name", "fromGallery-" + SimpleDateFormat(FILENAME_FORMAT, Locale.US)
                            .format(System.currentTimeMillis())
                    )
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

        binding.exportFab.setOnClickListener {
            viewModel.createPDFWithMultipleImage()
            resetSelection()

            var photoURI : Uri? = null;
            if (viewModel.pdfFile != null && viewModel.pdfFile!!.exists()) {
                photoURI = FileProvider.getUriForFile(
                    applicationContext,
                    applicationContext.getPackageName() + ".provider",
                    viewModel.pdfFile!!
                )
            }
            if (photoURI != null) {
                ShareCompat.IntentBuilder.from(this).setType("application/pdf").addStream(photoURI).startChooser()
            }

//            viewModel.pdfFile?.delete()
            viewModel.pdfFile = null
        }

        binding.deleteFab.setOnClickListener {
            deleteSelection()
            resetSelection()
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = CardsAdapter(all, viewModel)
        }



    }

    private fun deleteSelection() {
        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        for (c : Card in viewModel.selectedCards) {
            dao.delete(c)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = CardsAdapter(dao.getAll(), viewModel)
        }
    }

    private fun OnAddButtonClick() {
        setVisibility(closed)
        setAnimation(closed)
        closed = !closed;
    }

    private fun setAnimation(closed: Boolean) {
        if (!closed) {
            binding.cameraButton.startAnimation(fromBottom)
            binding.galleryButton.startAnimation(fromBottom)
            binding.mainButton.startAnimation(rotateOpen)
        } else {
            binding.cameraButton.startAnimation(toBottom)
            binding.galleryButton.startAnimation(toBottom)
            binding.mainButton.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(closed: Boolean) {
        if (!closed) {
            binding.cameraButton.visibility = View.VISIBLE
            binding.galleryButton.visibility = View.VISIBLE
        } else {
            binding.cameraButton.visibility = View.INVISIBLE
            binding.galleryButton.visibility = View.INVISIBLE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pdf_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menu.getItem(0).isVisible = viewModel.numOfCards.value?.compareTo(0)
                ?.equals(1) == true
//                    && viewModel.numOfCards.value?.compareTo(9)
//                ?.equals(-1) == true
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        resetSelection()
        return super.onOptionsItemSelected(item)
    }

    private fun resetSelection() {
        viewModel.selectedCards = mutableListOf<Card>()
        viewModel.numOfCards.value = 0


        binding.recyclerView.adapter?.itemCount?.let {
            binding.recyclerView.adapter?.notifyItemRangeChanged(-1,
                it + 1, "RESET")
        }
    }




}