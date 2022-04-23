package com.moojigbc.mymemory

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.moojigbc.mymemory.model.MemoryGame

class YouWin(private val memoryGame: MemoryGame) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage(R.string.you_win)
                .setPositiveButton(R.string.replay
                ) { _, _0 ->
                    memoryGame.restart()
                }
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}
