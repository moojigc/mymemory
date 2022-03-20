package com.moojigbc.mymemory

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.moojigbc.mymemory.model.BoardSize
import com.moojigbc.mymemory.model.MemoryCard
import com.moojigbc.mymemory.model.MemoryGame
import com.moojigbc.mymemory.utils.DEFAULT_ICONS

class MainActivity : AppCompatActivity() {

    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMovesCount: TextView
    private lateinit var tvPairsCount: TextView
    private lateinit var memoryGame: MemoryGame;
    private lateinit var adapter: MemoryBoardAdapter;
    private var boardSize: BoardSize = BoardSize.EASY;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvBoard = findViewById(R.id.rvBoard)
        tvMovesCount = findViewById(R.id.tvMovesCount)
        tvPairsCount = findViewById(R.id.tvPairsCount)

        memoryGame = MemoryGame(boardSize);

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener {
            override fun onClick(position: Int) {
                Log.i("CardClick", position.toString());
                updateGameWithFlip(position);
            }
        });

        rvBoard.adapter =adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        memoryGame.flipCard(position);
        adapter.notifyDataSetChanged()
    }
}