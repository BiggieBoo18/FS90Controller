package com.fs90.fs90controller

import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager


class TwoLinkSurfaceView(context: Context, surfaceView: SurfaceView) : SurfaceView(context), SurfaceHolder.Callback {
    private val TAG = this::class.java.simpleName
    private var surfaceHolder: SurfaceHolder? = null
    private var paint: Paint? = null
    var color: Int? = null
    private var canvas: Canvas? = null

    init {
        surfaceHolder = surfaceView.holder
        // display の情報（高さ 横）を取得
        val size = Point().also {
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.apply {
                getSize(
                    it
                )
            }
        }
        // 背景を透過させ、一番上に表示
        surfaceHolder!!.setFormat(PixelFormat.TRANSPARENT)
        surfaceView.setZOrderOnTop(true)
        // コールバック
        surfaceHolder!!.addCallback(this)
        // ペイント関連の設定
        paint = Paint()
        color = Color.WHITE
        paint!!.color = color as Int
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeCap = Paint.Cap.ROUND
        paint!!.isAntiAlias = true
    }

    // surfaceViewが作られたとき
    override fun surfaceCreated(holder: SurfaceHolder) {
        /// canvas初期化
        initializeCanvas()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }


    /// canvasの初期化
    private fun initializeCanvas() {
        // create canvas
        canvas = Canvas()
        /// ロックしてキャンバスを取得
        canvas = surfaceHolder!!.lockCanvas()
        drawBackground()
        /// ロックを解除
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }

    private fun drawBackground() {
        // setting paint
        paint!!.pathEffect = DashPathEffect(floatArrayOf(10f, 20f), 0f)
        paint!!.strokeWidth = 5F
        // キャンバスの背景
        canvas!!.drawColor(Color.BLACK)
        paint?.let { canvas!!.drawArc(525F-170F*3, 525F-170F*3, 525F+170F*3, 525F+170F*3, 180F, 180F, true, it) }
    }

    fun drawArm(x1: Float, y1: Float, x2: Float, y2: Float) {
        /// ロックしてキャンバスを取得
        canvas = surfaceHolder!!.lockCanvas()
        drawBackground()
        paint!!.pathEffect = null;
        paint!!.color = Color.YELLOW
        paint?.let {
            paint!!.strokeWidth = 30F
            canvas!!.drawPoint(x1, y1, it)
            canvas!!.drawPoint(x2, y2, it)
            paint!!.strokeWidth = 10F
            canvas!!.drawLines(floatArrayOf(525F, 540F - 5F, x1, y1, x1, y1, x2, y2), it)
        }

        /// ロックを解除
        surfaceHolder!!.unlockCanvasAndPost(canvas)
    }

//    /// 画面をタッチしたときにアクションごとに関数を呼び出す
//    fun onTouch(event: MotionEvent) : Boolean{
//        when (event.action) {
////            MotionEvent.ACTION_DOWN -> touchDown(event.x, event.y)
////            MotionEvent.ACTION_MOVE -> touchMove(event.x, event.y)
//            MotionEvent.ACTION_UP -> touchUp(event.x, event.y)
//        }
//        return true
//    }
//
//    private fun touchUp(x: Float, y: Float) {
//        drawDot(x, y)
//    }
}