package com.fs90.fs90controller

import android.content.Context
import android.graphics.*
import android.view.SurfaceHolder
import android.view.SurfaceView


class TwoLinkSurfaceView(context: Context, surfaceView: SurfaceView,
                         private var armLength1: Float, private var armLength2: Float
) : SurfaceView(context), SurfaceHolder.Callback {
    private val TAG = this::class.java.simpleName
    private val WIDTH  = 1050
    private val HEIGHT = 540
    private val armLength = armLength1 + armLength2
    private var surfaceHolder: SurfaceHolder? = null
    private var paint: Paint? = null
    private var canvas: Canvas? = null
    var armScale     = 0F
    var screenScale  = 1F
    var screenWidth  = WIDTH
    var screenHeight = HEIGHT

    init {
        surfaceHolder = surfaceView.holder
        // transparent
        surfaceHolder!!.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.setZOrderOnTop(true)
        // callback
        surfaceHolder!!.addCallback(this)
        // setting paint
        paint = Paint()
        paint!!.color = Color.WHITE
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.isAntiAlias = true
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        initializeCanvas()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


    // initialize canvas
    private fun initializeCanvas() {
        // create canvas
        canvas = Canvas()
        // lock canvas
        canvas = surfaceHolder!!.lockCanvas()
        // calculate scale
        val scaleX: Float = (canvas!!.width.toFloat()  / WIDTH)
        val scaleY: Float = (canvas!!.height.toFloat() / HEIGHT)
        screenScale = if (scaleX > scaleY) scaleY else scaleX
        // draw background
        drawBackground()
        // set screen size
        screenWidth  = canvas!!.width
        screenHeight = canvas!!.height
        // release lock
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }

    private fun drawBackground() {
        // scale view
        canvas!!.translate((canvas!!.width - WIDTH) / 2 * screenScale, (canvas!!.height - HEIGHT) / 2 * screenScale) // to center
        canvas!!.scale(screenScale, screenScale) // scale
        if (armScale == 0F) {
            // arm scale
            armScale = screenWidth / armLength / 2
        }
        // setting paint
        paint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
        paint!!.strokeWidth = 5F
        paint!!.color = Color.WHITE
        // background
        canvas!!.drawColor(Color.BLACK)
        paint?.let { canvas!!.drawArc(
            canvas!!.width / 2 - armLength * armScale,
            canvas!!.width / 2 - armLength * armScale,
            canvas!!.width / 2 + armLength * armScale,
            canvas!!.height - 5F + armLength * armScale,
            180F,
            180F,
            true,
            it
        ) }
    }

    fun drawArm(x1: Float, y1: Float, x2: Float, y2: Float) {
        canvas = surfaceHolder!!.lockCanvas()
        val sx1 = x1 * armScale + screenWidth / 2
        val sy1 = -(y1 * armScale) + screenHeight
        val sx2 = x2 * armScale + screenWidth / 2
        val sy2 = -(y2 * armScale) + screenHeight
        drawBackground()
        paint!!.pathEffect = null;
        paint!!.color = Color.YELLOW
        paint?.let {
            paint!!.strokeWidth = 30F
            canvas!!.drawPoint(canvas!!.width.toFloat() / 2, canvas!!.height.toFloat() - 5F, it)
            canvas!!.drawPoint(sx1, sy1, it)
            canvas!!.drawPoint(sx2, sy2, it)
            paint!!.strokeWidth = 10F
            canvas!!.drawLines(floatArrayOf(canvas!!.width.toFloat() / 2, canvas!!.height.toFloat() - 5F, sx1, sy1, sx1, sy1, sx2, sy2), it)
        }
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }
}