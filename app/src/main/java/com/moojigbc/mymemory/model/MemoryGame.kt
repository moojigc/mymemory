package com.moojigbc.mymemory.model

import com.moojigbc.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>
    var pairsFoundCount = 0
    var movesCount = 0

    private var indexOfSingleFlippedCard: Int? = null

    init {
        val icons = DEFAULT_ICONS.shuffled().take(boardSize.getPairCount())
        val paired = (icons + icons).shuffled()
        // ok this is a shortcut for `it -> MemoryCard(it)`
        // kotlin is crazy lmao
        cards = paired.map { MemoryCard(it) }
    }

    private fun getFlippedCardCount(): Int {
        return cards.filter { it.isFaceUp }.size
    }

    fun isCardFaceUp(position: Int): Boolean {
        return cards[position].isFaceUp
    }

    fun flipCard(position: Int): Boolean {
        val card = cards[position]
        val otherCard = if (indexOfSingleFlippedCard != null) cards[indexOfSingleFlippedCard!!] else null

        if (indexOfSingleFlippedCard == null) {
            restoreCards()
            indexOfSingleFlippedCard = position
        } else {
            if (otherCard?.isMatch(card) == true) {
                card.isMatched = true
                otherCard.isMatched = true
                pairsFoundCount ++
            }
            indexOfSingleFlippedCard = null
        }

        card.isFaceUp = !card.isFaceUp
        movesCount ++
        return card.isMatched
    }

    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun restart() {
       for (card in cards) {
           card.isMatched = false
           card.isFaceUp = false
           movesCount = 0
           pairsFoundCount = 0
       }
    }

    fun isGameWon(): Boolean {
        return this.pairsFoundCount == this.boardSize.getPairCount()
    }

    fun getMoveCount(): Int {
        // kotlin automatically converts float to int
        return movesCount / 2
    }
}