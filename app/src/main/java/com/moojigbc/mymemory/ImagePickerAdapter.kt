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
import com.moojigbc.mymemory.model.SelectedImage
import kotlin.math.min

class ImagePickerAdapter(
    private val context: Context,
    private val selectedImageUris: MutableList<SelectedImage>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object {
        private const val TAG = "ImagePickerAdapter";
    }

    interface ImageClickListener {
        fun onPlaceHolderClick()
        fun onExistingImageClick(current: SelectedImage, previous: SelectedImage?)
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
        if (position < selectedImageUris.size) {
            holder.bind(selectedImageUris[position])
        } else {
            holder.bind()
        }
    }

    override fun getItemCount() = boardSize.getPairCount()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var selectedImage: SelectedImage? = null;
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage)

        fun bind(selectedImage: SelectedImage) {
            ivCustomImage.setImageURI(selectedImage.uri)
            ivCustomImage.setOnClickListener {
                imageClickListener.onExistingImageClick(selectedImage, this.selectedImage)
            }
            this.selectedImage = selectedImage;
        }
        fun bind() {
            ivCustomImage.setOnClickListener {
                imageClickListener.onPlaceHolderClick()
            }
        }
    }
}