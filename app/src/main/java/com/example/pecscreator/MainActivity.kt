package com.example.pecscreator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pecscreator.databinding.ActivityMainBinding
import com.example.pecscreator.tutorial.TutorialActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private var closed = true
    private val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

    private lateinit var db: CardsDatabase
    private lateinit var dao: CardDao





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val sharedPreferences = getSharedPreferences("PECS_SHARED", MODE_PRIVATE)
        if (!sharedPreferences.contains("launched")) {
            sharedPreferences.edit().putString("launched", "yes").apply()
            val intent = Intent(this@MainActivity, TutorialActivity::class.java)
            startActivity(intent)
        }

        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        val all = dao.getAll()

        val nameObserver = androidx.lifecycle.Observer<Int> { x ->
            clearAnimation()
            if (x >= 1) {
//                OnAddButtonClick()
                binding.deleteFab.visibility = View.VISIBLE
                binding.exportFab.visibility = View.VISIBLE
                binding.mainButton.visibility = View.GONE
                binding.cameraButton.visibility = View.GONE
                binding.galleryButton.visibility = View.GONE
            } else {
                if (closed == true) closed = !closed
                binding.deleteFab.visibility = View.GONE
                binding.exportFab.visibility = View.GONE
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
                    Thread.sleep(200)
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

            val numbers = arrayOf("4", "8")
            val builder = MaterialAlertDialogBuilder(this)

            builder.setTitle("Number of cards on a single page")

            builder.setNegativeButton("Cancel") { _, _ ->
                resetSelection()
                viewModel.numberOfCardsOnSinglePage = 0
            }

            builder.setPositiveButton("OK") { _, _ ->
                //TODO AK NEMA NIC SELECTUNTE
                viewModel.createPDFWithMultipleImage()
                resetSelection()

                var photoURI: Uri? = null;
                if (viewModel.pdfFile != null && viewModel.pdfFile!!.exists()) {
                    photoURI = FileProvider.getUriForFile(
                        applicationContext,
                        applicationContext.packageName + ".provider",
                        viewModel.pdfFile!!
                    )
                }
                if (photoURI != null) {
                    ShareCompat.IntentBuilder.from(this).setType("application/pdf")
                        .addStream(photoURI).startChooser()
                }

                viewModel.pdfFile = null
            }

            builder.setSingleChoiceItems(numbers, -1) { _, which ->
                viewModel.numberOfCardsOnSinglePage = numbers[which].toInt()
            }

            builder.show()
        }

        binding.deleteFab.setOnClickListener {

            val builder = MaterialAlertDialogBuilder(this@MainActivity)
            builder.setMessage("Are you sure you want to Delete?")
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    deleteSelection()
                    resetSelection()
                }
                .setNegativeButton("No") { dialog, id ->
                    // Dismiss the dialog
                    dialog.dismiss()
                }
            val alert = builder.create()
            alert.show()



        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = CardsAdapter(all, viewModel)
        }


    }

    override fun onBackPressed() {
        if (viewModel.selectedCards.size >= 1) {
            if (closed == true) closed = !closed
            binding.deleteFab.visibility = View.GONE
            binding.exportFab.visibility = View.GONE
            binding.mainButton.visibility = View.VISIBLE
            invalidateOptionsMenu()
            resetSelection()
        } else {

            super.onBackPressed()
        }

    }

    private fun deleteSelection() {
        db = CardsDatabase.getInstance(this)
        dao = db.cardsDao()
        for (c: Card in viewModel.selectedCards) {
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

    private fun clearAnimation() {
        binding.cameraButton.clearAnimation()
        binding.galleryButton.clearAnimation()
        binding.mainButton.clearAnimation()
    }

    private fun setVisibility(closed: Boolean) {
        if (!closed) {
            binding.cameraButton.visibility = View.VISIBLE
            binding.galleryButton.visibility = View.VISIBLE
        } else {
            binding.cameraButton.visibility = View.GONE
            binding.galleryButton.visibility = View.GONE
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

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = CardsAdapter(dao.getAll(), viewModel)
        }
    }


}