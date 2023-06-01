package com.example.pecscreator.tutorial

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.pecscreator.R
import me.relex.circleindicator.CircleIndicator3


class TutorialActivity : AppCompatActivity() {

    private var textList = mutableListOf<String>()
    private var videoList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_tutorial)

        postToList()

        findViewById<ViewPager2>(R.id.view_pager2).adapter =
            ViewPagerAdapter(videoList, textList, this)
        val indicator = findViewById<CircleIndicator3>(R.id.indicator)
        indicator.setViewPager(findViewById(R.id.view_pager2))

    }


    private fun addToList(text: String, image: Int) {
        textList.add(text)
        videoList.add(image)
    }

    private fun postToList() {

        addToList("Welcome to PECS Creator\nLet\'s show you around", R.raw.main)
        addToList("Choose a photo from the gallery", R.raw.gallery)
        addToList("Take a photo with the camera", R.raw.photo)
        addToList(
            "Edit the photo\nUse the \"Auto Crop\" to automatically crop your image!",
            R.raw.edit
        )
        addToList(
            "To export cards for print, just select them by long-pressing and tap export",
            R.raw.export
        )
        addToList(
            "To delete unwanted cards, just select them by long-pressing and tap delete",
            R.raw.delete
        )
        addToList("", R.raw.main)
    }


}