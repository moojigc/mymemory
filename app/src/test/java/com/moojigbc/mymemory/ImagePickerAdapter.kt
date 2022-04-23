package com.moojigbc.mymemory

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.moojigbc.mymemory.model.BoardSize

class ImagePickerAdapter(
    gameCreationActivity: GameCreationActivity,
    chosenImageUris: MutableList<Uri>,
    boardSize: BoardSize
) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
