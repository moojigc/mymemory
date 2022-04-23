package com.moojigbc.mymemory

import android.animation.ArgbEvaluator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.moojigbc.mymemory.model.BoardSize
import com.moojigbc.mymemory.model.MemoryGame
import com.moojigbc.mymemory.utils.EXTRA_BOARD_SIZE
import com.moojigbc.mymemory.utils.Prompts

class MainActivity : AppCompatActivity() {

    private lateinit var clRoot: ConstraintLayout
    private lateinit var rvBoard: RecyclerView
    private lateinit var tvMovesCount: TextView
    private lateinit var tvPairsCount: TextView
    private lateinit var memoryGame: MemoryGame
    private lateinit var adapter: MemoryBoardAdapter
    private var boardSize: BoardSize = BoardSize.EASY

    companion object {
//        val TAG = "MainActivity";
        private const val CREATE_REQUEST_CODE = 203
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard)
        tvMovesCount = findViewById(R.id.tvMovesCount)
        tvPairsCount = findViewById(R.id.tvPairsCount)

        val intent = Intent(this, GameCreationActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE, BoardSize.MEDIUM)
        startActivity(intent)

        setupBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mi_refresh -> {
                if (memoryGame.movesCount > 0 && !memoryGame.isGameWon()) {
                    showAlertDialog(Prompts.RESTART, null, View.OnClickListener {
                        setupBoard()
                    })
                } else {
                    setupBoard()
                }
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
            }
            R.id.mi_custom -> {
                showCreationDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewSizeDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            else -> radioGroupSize.check(R.id.rbHard)
        }

        showAlertDialog(Prompts.CREATE_CUSTOM, boardSizeView, View.OnClickListener {
            boardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            setupBoard()
        })
    }

    private fun showCreationDialog() {
        val boardSizeView = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)

        showAlertDialog(Prompts.CHOOSE_DIFFICULTY, boardSizeView, View.OnClickListener {
            val desiredBoardSize = when (radioGroupSize.checkedRadioButtonId) {
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            val intent = Intent(this, GameCreationActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showAlertDialog(title: String, view: View?, positiveClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton(Prompts.CANCEL, null)
            .setPositiveButton(Prompts.OK) { _, _ ->
                positiveClickListener.onClick(null)
            }
            .show()
    }

    private fun setupBoard() {
        memoryGame = MemoryGame(boardSize)

        tvPairsCount.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        tvPairsCount.text = "Pairs: 0/${boardSize.getPairCount()}"
        tvMovesCount.text = "Moves: ${memoryGame.getMoveCount()}"

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener {
            override fun onClick(position: Int) {
                Log.i("CardClick", position.toString())
                updateGameWithFlip(position)
            }
        })

        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun updateGameWithFlip(position: Int) {
        if (memoryGame.isCardFaceUp(position)) {
            return
        }

        tvMovesCount.text = "Moves: ${memoryGame.getMoveCount()}"

        val isMatch = memoryGame.flipCard(position)

        if (isMatch) {
            val color = ArgbEvaluator().evaluate(
                memoryGame.pairsFoundCount.toFloat() / boardSize.getPairCount(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_complete)
            ) as Int
            tvPairsCount.setTextColor(color)
            tvPairsCount.text = "Pairs: ${memoryGame.pairsFoundCount}/${boardSize.getPairCount()}"
            if (memoryGame.isGameWon()) {
                this.messageUser(Prompts.YOU_WIN)
            }
        }

        adapter.notifyDataSetChanged()
    }

    private fun messageUser(text: String) {
        return Snackbar.make(clRoot, text, Snackbar.LENGTH_LONG).show()
    }
}
