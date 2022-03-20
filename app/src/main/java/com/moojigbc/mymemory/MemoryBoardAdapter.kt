package com.moojigbc.mymemory

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.moojigbc.mymemory.model.BoardSize
import com.moojigbc.mymemory.model.MemoryCard
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val memoryCards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
    ) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object {
        private const val MARGIN_SIZE = 10
        private const val TAG = "MemoryBoardAdapter"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardWidth, cardHeight)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card, parent, false)
        view.findViewById<CardView>(R.id.cardView).layoutParams.apply { this as ViewGroup.MarginLayoutParams
            width = cardSideLength
            height = cardSideLength
            setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        }
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount() = boardSize.cardCount

    interface CardClickListener {
        fun onClick(position: Int)
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
            val card = memoryCards[position];
            imageButton.setImageResource(if (card.isFaceUp) card.id else R.drawable.ic_launcher_background)
            imageButton.alpha = if (card.isMatched) .4f else 1.0f
            val colorStateList = if (card.isMatched) ContextCompat.getColorStateList(context, R.color.color_gray) else null;
            ViewCompat.setBackgroundTintList(imageButton, colorStateList);
            imageButton.setOnClickListener {
                cardClickListener.onClick(position)
                Log.i(TAG, "clicked on position $position")
            }
        }
    }
}