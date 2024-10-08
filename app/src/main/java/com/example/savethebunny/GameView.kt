package com.example.savethebunny

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Handler
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat

import java.util.*

class GameView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    companion object {
        var dWidth: Int = 0
    }
    private val background: Bitmap
    private val ground: Bitmap
    private var rabbit: Bitmap // Modified to var for resizing
    private val rectBackground: Rect
    private val rectGround: Rect
    private val handler: Handler
    private val UPDATE_MILLIS = 30L
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val TEXT_SIZE = 120f
    private var points = 0
    private var life = 3
    private var random: Random? = null
    private var rabbitX = 0f
    private var rabbitY = 0f
    private var oldX = 0f
    private var oldRabbitX = 0f
    private val spikes: MutableList<Spike> = ArrayList()
    private val explosions: MutableList<Explosion> = ArrayList()
    private var runnable: Runnable

    init {
        background = BitmapFactory.decodeResource(resources, R.drawable.background)
        ground = BitmapFactory.decodeResource(resources, R.drawable.ground)
        rabbit = BitmapFactory.decodeResource(resources, R.drawable.rabbit)

        // Resize the rabbit image
        rabbit = resizeBitmap(rabbit, rabbit.width , rabbit.height )

        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val dWidth = size.x
        val dHeight = size.y
        GameView.dWidth = dWidth
        rectBackground = Rect(0, 0, dWidth, dHeight)
        val newGroundHeight = ground.height / 2 // Change this value as needed
        rectGround = Rect(0, dHeight - newGroundHeight, dWidth, dHeight)
        handler = Handler()
        textPaint.color = Color.rgb(255, 165, 0)
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        textPaint.typeface = ResourcesCompat.getFont(context, R.font.kenney_blocks)
        healthPaint.color = Color.GREEN
        random = Random()
        rabbitX = dWidth / 2 - rabbit.width / 2.toFloat()
        rabbitY = dHeight - ground.height.toFloat()
        for (i in 0..2) {
            val spike = Spike(context)

            spikes.add(spike)
        }
        runnable = Runnable { invalidate() }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(background, null, rectBackground, null)
        canvas.drawBitmap(ground, null, rectGround, null)
        canvas.drawBitmap(rabbit, rabbitX, rabbitY, null)
        for (i in spikes.indices) {
            val spikeBitmap = spikes[i].getSpike(spikes[i].spikeFrame)
            spikeBitmap?.let {
                canvas.drawBitmap(it, spikes[i].spikeX.toFloat(), spikes[i].spikeY.toFloat(), null)
            }
            spikes[i].spikeFrame++
            if (spikes[i].spikeFrame > 2) {
                spikes[i].spikeFrame = 0
            }
            spikes[i].spikeY += spikes[i].spikeVelocity
            if (spikes[i].spikeY + spikes[i].getSpikeHeight() >= rectGround.top) {
                points += 10
                val explosion = Explosion(context)
                explosion.explosionX = spikes[i].spikeX
                explosion.explosionY = spikes[i].spikeY
                explosions.add(explosion)
                spikes[i].resetPosition()
            }
        }
        for (i in spikes.indices) {
            if (spikes[i].spikeX + spikes[i].getSpikeWidth() >= rabbitX && spikes[i].spikeX <= rabbitX + rabbit.width && spikes[i].spikeY + spikes[i].getSpikeHeight() >= rabbitY && spikes[i].spikeY + spikes[i].getSpikeHeight() <= rabbitY + rabbit.height) {
                life--
                spikes[i].resetPosition()
                if (life == 0) {
                    // Start the activity
                    val intent = Intent(context, GameOver::class.java)
                    intent.putExtra("points", points)
                    context.startActivity(intent)
                    (context as Activity).finish()
                }
            }
        }
        for (i in explosions.indices) {
            val explosionBitmap = explosions[i].getExplosion(explosions[i].explosionFrame)
            explosionBitmap?.let {
                canvas.drawBitmap(it, explosions[i].explosionX.toFloat(), explosions[i].explosionY.toFloat(), null)
            }
            explosions[i].explosionFrame++
            if (explosions[i].explosionFrame > 3) {
                explosions.removeAt(i)
            }
        }
        healthPaint.color = when (life) {
            2 -> Color.YELLOW
            1 -> Color.RED
            else -> Color.GREEN
        }
        canvas.drawRect(rectGround.right - 200f, 30f, rectGround.right - 200 + 60 * life.toFloat(), 80f, healthPaint)
        canvas.drawText("$points", 20f, TEXT_SIZE, textPaint)
        handler.postDelayed(runnable, UPDATE_MILLIS)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= rabbitY) {
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                oldX = event.x
                oldRabbitX = rabbitX
            }
            if (action == MotionEvent.ACTION_MOVE) {
                val shift = oldX - touchX
                val newRabbitX = oldRabbitX - shift
                rabbitX = when {
                    newRabbitX <= 0 -> 0f
                    newRabbitX >= rectGround.right - rabbit.width -> (rectGround.right - rabbit.width).toFloat()
                    else -> newRabbitX
                }
            }
        }
        return true
    }

    // Function to resize bitmap
    private fun resizeBitmap(bitmap: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false)
    }
}
