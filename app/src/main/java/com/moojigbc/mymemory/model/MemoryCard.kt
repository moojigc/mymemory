package com.moojigbc.mymemory.model

import android.util.Log

data class MemoryCard(
    val id: Int,
    var isFaceUp: Boolean = false,
    var isMatched: Boolean = false
) {
    fun isMatch(card: MemoryCard): Boolean {
        return card.id == this.id;
    }
}