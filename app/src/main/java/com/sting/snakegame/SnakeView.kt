package com.sting.snakegame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View
import java.util.Timer
import java.util.TimerTask
import kotlin.random.Random

class SnakeView(
    context: Context,
    private val onScoreChange: (Int) -> Unit
) : View(context) {

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    data class Point(var x: Int, var y: Int)

    private val gridSize = 30
    private val cellSize get() = width / gridSize

    private val snake = mutableListOf<Point>()
    private var food = Point(0, 0)
    private var direction = Direction.RIGHT
    private var nextDirection = Direction.RIGHT
    private var score = 0
    private var running = false

    private val snakePaint = Paint().apply {
        color = Color.parseColor("#4CAF50")
        style = Paint.Style.FILL
    }
    private val headPaint = Paint().apply {
        color = Color.parseColor("#2E7D32")
        style = Paint.Style.FILL
    }
    private val foodPaint = Paint().apply {
        color = Color.parseColor("#F44336")
        style = Paint.Style.FILL
    }
    private val gridPaint = Paint().apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val bgPaint = Paint().apply {
        color = Color.parseColor("#0F0F1A")
        style = Paint.Style.FILL
    }

    var onGameOver: ((Int) -> Unit)? = null

    private val timer = Timer()
    private var gameTask: TimerTask? = null

    init {
        setBackgroundColor(Color.parseColor("#0F0F1A"))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (!running) startGame()
    }

    private fun startGame() {
        snake.clear()
        val mid = gridSize / 2
        snake.add(Point(mid, mid))
        snake.add(Point(mid - 1, mid))
        snake.add(Point(mid - 2, mid))
        direction = Direction.RIGHT
        nextDirection = Direction.RIGHT
        score = 0
        onScoreChange(score)
        spawnFood()
        running = true
        startLoop()
    }

    fun restart() {
        stopLoop()
        startGame()
    }

    private fun startLoop() {
        gameTask?.cancel()
        gameTask = object : TimerTask() {
            override fun run() {
                post { update() }
            }
        }
        timer.schedule(gameTask, 0, 150)
    }

    private fun stopLoop() {
        gameTask?.cancel()
        running = false
    }

    fun setDirection(dir: Direction) {
        val opposite = when (direction) {
            Direction.UP -> Direction.DOWN
            Direction.DOWN -> Direction.UP
            Direction.LEFT -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
        if (dir != opposite) nextDirection = dir
    }

    private fun update() {
        direction = nextDirection
        val head = snake.first()
        val newHead = when (direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        // 穿墙检测
        if (newHead.x < 0 || newHead.x >= gridSize || newHead.y < 0 || newHead.y >= gridSize) {
            stopLoop()
            post { onGameOver?.invoke(score) }
            return
        }

        // 撞自己
        if (snake.any { it.x == newHead.x && it.y == newHead.y }) {
            stopLoop()
            post { onGameOver?.invoke(score) }
            return
        }

        snake.add(0, newHead)

        if (newHead.x == food.x && newHead.y == food.y) {
            score++
            onScoreChange(score)
            spawnFood()
        } else {
            snake.removeAt(snake.size - 1)
        }

        invalidate()
    }

    private fun spawnFood() {
        var p: Point
        do {
            p = Point(Random.nextInt(gridSize), Random.nextInt(gridSize))
        } while (snake.any { it.x == p.x && it.y == p.y })
        food = p
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (cellSize <= 0) return

        val cs = cellSize.toFloat()

        // 画蛇
        for ((i, p) in snake.withIndex()) {
            val paint = if (i == 0) headPaint else snakePaint
            canvas.drawRoundRect(
                (p.x * cs + 1), (p.y * cs + 1),
                ((p.x + 1) * cs - 1), ((p.y + 1) * cs - 1),
                8f, 8f, paint
            )
        }

        // 画食物
        canvas.drawRoundRect(
            (food.x * cs + 2), (food.y * cs + 2),
            ((food.x + 1) * cs - 2), ((food.y + 1) * cs - 2),
            8f, 8f, foodPaint
        )
    }
}
