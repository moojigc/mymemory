package com.moojigbc.mymemory.model

import com.moojigbc.mymemory.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize) {
    val cards: List<MemoryCard>
    var pairsFoundCount = 0

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

    fun flipCard(position: Int) {
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
    }

    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }
}