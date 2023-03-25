package com.example.pecscreator

import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.pecscreator.R.layout.card_item

class CardsAdapter(private val allCards: List<Card>) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = allCards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv.text = allCards.get(position).description
        holder.iv.setImageBitmap(allCards.get(position).imageUri)
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tv : TextView = view.findViewById(R.id.name)
        val iv : ImageView = view.findViewById(R.id.recyclerImageView)
    }
}
