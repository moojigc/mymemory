package com.moojigbc.mymemory.model

enum class BoardSize(val cardCount: Int) {
    EASY(8),
    MEDIUM(18),
    HARD(24);

    fun getWidth(): Int {
        return when (this) {
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight(): Int {
        return cardCount / getWidth()
    }

    fun getPairCount(): Int {
        return cardCount / 2
    }

}