package com.example.pecscreator.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pecscreator.R

class ViewPagerAdapter(private var images : List<Int>) :
    RecyclerView.Adapter<ViewPagerAdapter.Pager2ViewHolder>() {

    inner class Pager2ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val imageView : ImageView = itemView.findViewById(R.id.ivTutorial)

    }

    override fun onCreateViewHolder(
        parent : ViewGroup,
        viewType : Int
    ) : ViewPagerAdapter.Pager2ViewHolder {
        return Pager2ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_page, parent, false))
    }

    override fun getItemCount(): Int {
       return images.size
    }

    override fun onBindViewHolder(holder: ViewPagerAdapter.Pager2ViewHolder, position : Int) {
        holder.imageView.setImageResource(images[position])
    }


}