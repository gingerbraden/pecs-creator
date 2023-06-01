package com.example.pecscreator

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class CardsAdapter(private val allCards: List<Card>, private val viewModel: MainViewModel) :
    RecyclerView.Adapter<CardsAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.card_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    }

    override fun getItemCount() = allCards.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {

        if (allCards.get(position).description.length > 15) {
            holder.tv.text = allCards.get(position).description.substring(0, 18)
        } else {
            holder.tv.text = allCards.get(position).description
        }
        holder.iv.setImageBitmap(allCards.get(position).imageUri)

        if (!payloads.isEmpty()) {
            for (a: Any in payloads) {
                if (a.equals("RESET")) {
                    holder.ch.visibility = View.GONE
                    holder.iv.clearColorFilter()
                }
            }
        } else {
            if (allCards.get(position).description.length > 15) {
                holder.tv.text = allCards.get(position).description.substring(0, 15) + "..."
            } else {
                holder.tv.text = allCards.get(position).description
            }
            holder.iv.setImageBitmap(allCards.get(position).imageUri)
            holder.view.setOnLongClickListener {
                if (addCardToList(allCards.get(position))) {
                    val greyFilter =
                        PorterDuffColorFilter(Color.parseColor("#FF6F00"), PorterDuff.Mode.MULTIPLY)
                    holder.iv.colorFilter = greyFilter
                    holder.ch.visibility = View.VISIBLE
                } else {
                    holder.ch.visibility = View.GONE
                    holder.iv.clearColorFilter()
                }
                true
            }
            holder.view.setOnClickListener {
                if (viewModel.numOfCards.value != null) {
                    if (viewModel.numOfCards.value!! >= 1) {
                        if (addCardToList(allCards.get(position))) {
                            val greyFilter =
                                PorterDuffColorFilter(
                                    Color.parseColor("#FF6F00"),
                                    PorterDuff.Mode.MULTIPLY
                                )
                            holder.iv.colorFilter = greyFilter
                            holder.ch.visibility = View.VISIBLE
                        } else {
                            holder.ch.visibility = View.GONE
                            holder.iv.clearColorFilter()
                        }
                    }
                }
            }
        }

    }

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val tv: TextView = view.findViewById(R.id.name)
        val iv: ImageView = view.findViewById(R.id.recyclerImageView)
        val ch: ImageView = view.findViewById(R.id.check)
    }

    fun addCardToList(card: Card): Boolean {
        if (!(card in viewModel.selectedCards)) {
            viewModel.selectedCards.add(card)
            viewModel.numOfCards.value = viewModel.selectedCards.size
            return true
        } else {
            viewModel.selectedCards.remove(card)
            viewModel.numOfCards.value = viewModel.selectedCards.size
            return false
        }
    }


}
