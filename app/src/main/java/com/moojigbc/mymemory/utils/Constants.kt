package com.moojigbc.mymemory.utils

import com.moojigbc.mymemory.R

val DEFAULT_ICONS = listOf(
    R.drawable.ic_emoji,
    R.drawable.ic_face,
    R.drawable.ic_flower,
    R.drawable.ic_leaf,
    R.drawable.ic_subway,
    R.drawable.ic_umbrella,
    R.drawable.ic_couch,
    R.drawable.ic_euro,
    R.drawable.ic_liquor,
    R.drawable.ic_timelapse,
    R.drawable.ic_hanger,
    R.drawable.ic_tractor
)

const val EXTRA_BOARD_SIZE = "EXTRA_BOARD_SIZE"

class Prompts {
    companion object {
        const val RESTART = "Quit your current game?"
        const val CHOOSE_DIFFICULTY = "Choose board size"
        const val CREATE_CUSTOM = "Create custom board"
        const val CANCEL = "Cancel"
        const val OK = "OK"
        const val YOU_WIN = "You won!!! Congratulations!!!"
    }
}