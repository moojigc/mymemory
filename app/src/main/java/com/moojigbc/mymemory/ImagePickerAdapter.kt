package com.moojigbc.mymemory

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.moojigbc.mymemory.model.BoardSize
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: MutableList<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "ImagePickerAdapter";
    }

    interface ImageClickListener {
        fun onPlaceHolderClick()
        fun onExistingImageClick(current: Uri, previous: Uri?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / boardSize.getWidth()
        val cardHeight = parent.height / boardSize.getHeight()
        val cardSideHeight = min(cardHeight, cardWidth)
        val layoutParams = view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams
        layoutParams.height = cardSideHeight
        layoutParams.width = cardSideHeight

        Log.i(TAG, "Item count: $itemCount")

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder position=$position")
        if (position < imageUris.size) {
            holder.bind(imageUris[position])
        } else {
            holder.bind()
        }
    }

    override fun getItemCount() = boardSize.getPairCount()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var uri: Uri? = null
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(uri: Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener {
                imageClickListener.onExistingImageClick(uri, this.uri)
            }
            this.uri = uri;
        }
        fun bind() {
            ivCustomImage.setOnClickListener {
                imageClickListener.onPlaceHolderClick()
            }
        }
    }
}