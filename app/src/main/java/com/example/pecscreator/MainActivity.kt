package com.example.pecscreator

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.opengl.Visibility
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
import androidx.core.view.size
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

    private var COORDINATES = listOf<Pair<Int, Int>>(
        Pair(38, 38),
        Pair(913, 38),
        Pair(1778, 38),
        Pair(2653, 38),
        Pair(38, 1287),
        Pair(913, 1287),
        Pair(1778, 1287),
        Pair(2653, 1287)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        val all = dao.getAll()

        val nameObserver = androidx.lifecycle.Observer<Int> { x ->
            if (x in 1..8) {
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
            createPDFWithMultipleImage()
        }

        binding.deleteFab.setOnClickListener {
            deleteSelection()
            resetSelection()
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
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
            layoutManager = GridLayoutManager(this@MainActivity, 2)
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
                ?.equals(1) == true && viewModel.numOfCards.value?.compareTo(9)
                ?.equals(-1) == true
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
                it, "RESET")
        }
    }

    private fun createPDFWithMultipleImage() {
        val file = getOutputFile()
        if (file != null) {
            try {
                val fileOutputStream = FileOutputStream(file)
                val pdfDocument = PdfDocument()
                val pageInfo = PageInfo.Builder(3508, 2480, 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                val paint = Paint()
                paint.color = Color.BLACK
                paint.strokeWidth = 1F
                canvas.drawLine(875.5F, 0F, 875.5F, 2480F, paint)
                canvas.drawLine(1750.5F, 0F, 1750.5F, 2480F, paint)
                canvas.drawLine(2626.5F, 0F, 2626.5F, 2480F, paint)
                canvas.drawLine(0F, 1240F, 3508F, 1240F, paint)
                for (c in 0..viewModel.selectedCards.size - 1) {
                    val bitmap = viewModel.selectedCards.get(c).imageUri
                    if (bitmap != null) {
                        canvas.drawBitmap(
                            bitmap,
                            null,
                            Rect(
                                COORDINATES.get(c).first,
                                COORDINATES.get(c).second,
                                COORDINATES.get(c).first + 800,
                                COORDINATES.get(c).second + 800
                            ),
                            null
                        )
                    }
                }
                pdfDocument.finishPage(page)
                pdfDocument.writeTo(fileOutputStream)
                pdfDocument.close()
                Toast.makeText(this, "PDF Exported to Documents", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getOutputFile(): File? {
        val root = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            "PECS Creator"
        )
        var isFolderCreated = true
        if (!root.exists()) {
            isFolderCreated = root.mkdir()
        }
        return if (isFolderCreated) {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imageFileName = "PECS-Creator_$timeStamp"
            File(root, "$imageFileName.pdf")

        } else {
            null
        }
    }


}