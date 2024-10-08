package com.example.savethebunny

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.util.Random

class Spike(context: Context) {
    private var spike = arrayOfNulls<Bitmap>(3)
    var spikeFrame = 0
    var spikeX = 0
    var spikeY = 0
    var spikeVelocity = 0

    private val random = Random()



    init {
        spike[0] = BitmapFactory.decodeResource(context.resources, R.drawable.spike0)
       spike[1] = BitmapFactory.decodeResource(context.resources, R.drawable.spike1)
       spike[2] = BitmapFactory.decodeResource(context.resources, R.drawable.spike2)
        resetPosition()
    }

    fun getSpike(spikeFrame: Int): Bitmap? {
        return spike[spikeFrame]
    }

    fun getSpikeWidth(): Int {
        return spike[0]?.width ?: 0
    }

    fun getSpikeHeight(): Int {
        return spike[0]?.height ?: 0
    }

    fun resetPosition() {
        spikeX = random.nextInt(GameView.dWidth  - getSpikeWidth())
        spikeY = -200 + random.nextInt(600) * -1
        spikeVelocity = 35 + random.nextInt(16)
    }
}

