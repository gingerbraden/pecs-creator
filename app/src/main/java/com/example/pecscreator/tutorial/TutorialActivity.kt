package com.example.pecscreator.tutorial

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.pecscreator.R
import me.relex.circleindicator.CircleIndicator3


class TutorialActivity : AppCompatActivity() {

    private var textList = mutableListOf<String>()
    private var imageList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial)

        postToList()

        findViewById<ViewPager2>(R.id.view_pager2).adapter = ViewPagerAdapter(imageList)

        val indicator = findViewById<CircleIndicator3>(R.id.indicator)
        indicator.setViewPager(findViewById<ViewPager2>(R.id.view_pager2))

    }

    private fun addToList(text : String, image : Int) {
        textList.add(text)
        imageList.add(image)
    }

    private fun postToList() {
        for (i in 1..5) {
            addToList("ahoj", R.mipmap.ic_launcher)
        }
    }


}