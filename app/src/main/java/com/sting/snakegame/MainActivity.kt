package com.sting.snakegame

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: SnakeView
    private lateinit var scoreText: TextView
    private lateinit var touchArea: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scoreText = findViewById(R.id.scoreText)
        touchArea = findViewById(R.id.touchArea)
        gameView = SnakeView(this) { score ->
            scoreText.text = "得分: $score"
        }

        val gameContainer = findViewById<FrameLayout>(R.id.gameContainer)
        gameContainer.addView(gameView)

        touchArea.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                val w = touchArea.width.toFloat()
                val h = touchArea.height.toFloat()
                val x = event.x
                val y = event.y

                // 根据触摸位置决定方向
                val centerX = w / 2
                val centerY = h / 2
                val dx = x - centerX
                val dy = y - centerY

                if (kotlin.math.abs(dx) > kotlin.math.abs(dy)) {
                    if (dx > 0) gameView.setDirection(SnakeView.Direction.RIGHT)
                    else gameView.setDirection(SnakeView.Direction.LEFT)
                } else {
                    if (dy > 0) gameView.setDirection(SnakeView.Direction.DOWN)
                    else gameView.setDirection(SnakeView.Direction.UP)
                }
            }
            true
        }

        findViewById<View>(R.id.btnUp).setOnClickListener { gameView.setDirection(SnakeView.Direction.UP) }
        findViewById<View>(R.id.btnDown).setOnClickListener { gameView.setDirection(SnakeView.Direction.DOWN) }
        findViewById<View>(R.id.btnLeft).setOnClickListener { gameView.setDirection(SnakeView.Direction.LEFT) }
        findViewById<View>(R.id.btnRight).setOnClickListener { gameView.setDirection(SnakeView.Direction.RIGHT) }
        findViewById<View>(R.id.btnRestart).setOnClickListener { gameView.restart() }

        gameView.onGameOver = { showGameOverDialog(it) }
    }

    private fun showGameOverDialog(score: Int) {
        AlertDialog.Builder(this)
            .setTitle("游戏结束")
            .setMessage("得分: $score\n是否再来一局？")
            .setPositiveButton("再来") { _, _ -> gameView.restart() }
            .setNegativeButton("退出", { _, _ -> finish() })
            .setCancelable(false)
            .show()
    }
}
