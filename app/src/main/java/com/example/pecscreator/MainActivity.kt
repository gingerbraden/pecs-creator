package com.example.pecscreator

import android.content.Intent
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.pecscreator.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*


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

        // Create the observer which updates the UI.
        val nameObserver = androidx.lifecycle.Observer<Int> {x ->
//            if (x.equals(6)) {
                invalidateOptionsMenu()
//            }
        }
        viewModel.numOfCards.observe(this, nameObserver)




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
            adapter = CardsAdapter(all, viewModel)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.pdf_menu, menu)
        if (menu != null) {
            Log.d("ahoj", "oncreate ${menu.getItem(0).isVisible}")
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
//            menu.getItem(0).isVisible = false
            Log.d("ahoj", "onprepare ${menu.getItem(0).isVisible}, ${viewModel.numOfCards.value}")
//            menu.getItem(0).isVisible = true
            if (viewModel.numOfCards.value?.equals(6) == true) menu.getItem(0).isVisible = true
            else menu.getItem(0).isVisible = false
        }
        return super.onPrepareOptionsMenu(menu)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //TODO save pdf
        return super.onOptionsItemSelected(item)
    }









}