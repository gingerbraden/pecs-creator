package com.example.pecscreator.tutorial

import android.app.Activity
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.VideoView
import androidx.recyclerview.widget.RecyclerView
import com.example.pecscreator.R

class ViewPagerAdapter(private var videos : List<Int>, private var text : List<String>, private var activity: Activity) :

    RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoView : VideoView = itemView.findViewById(R.id.videoView)
        val textView : TextView = itemView.findViewById(R.id.textView2)
        val button : Button = itemView.findViewById(R.id.button)
    }

    override fun onCreateViewHolder(
        parent : ViewGroup,
        viewType : Int
    ) : ViewPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false))
    }

    override fun getItemCount(): Int {
       return videos.size
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position : Int) {

        holder.textView.text = text[position]
        if (position == 6) {
            holder.button.setOnClickListener {
                activity.onBackPressed()
            }
            holder.button.visibility = View.VISIBLE
            holder.videoView.visibility = View.GONE
        } else {
            holder.button.visibility = View.GONE
        }


        holder.videoView.setOnPreparedListener { it.isLooping = true }
        holder.videoView.setVideoURI(Uri.parse("android.resource://com.example.pecscreator/" + videos[position]));
        holder.videoView.start()
    }


    override fun onViewAttachedToWindow(holder: Pager2ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.videoView.start()
    }




}