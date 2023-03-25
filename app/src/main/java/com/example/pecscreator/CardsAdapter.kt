package com.example.pecscreator

import android.graphics.Color
import android.graphics.Typeface
import android.media.Image
import android.opengl.Visibility
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.marginEnd
import androidx.recyclerview.widget.RecyclerView
import com.example.pecscreator.R.layout.card_item

class CardsAdapter(private val allCards: List<Card>, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = allCards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tv.text = allCards.get(position).description
        holder.iv.setImageBitmap(allCards.get(position).imageUri)

        holder.view.setOnLongClickListener {
            if (addCardToList(allCards.get(position))) {
                holder.ch.visibility = View.VISIBLE
            } else {
                holder.ch.visibility = View.GONE
            }
            true
        }
    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tv : TextView = view.findViewById(R.id.name)
        val iv : ImageView = view.findViewById(R.id.recyclerImageView)
        val ch : ImageView = view.findViewById(R.id.check)
    }

    fun addCardToList(card : Card) : Boolean{
        if (!(card in viewModel.selectedCards)) {
            viewModel.selectedCards.add(card)
            viewModel.selectionMode = true
            return true
        } else {
            viewModel.selectedCards.remove(card)
            if (viewModel.selectedCards.isEmpty()) {
                viewModel.selectionMode = false
            }
            return false
         }
    }



}
