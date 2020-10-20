package com.fs90.fs90controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import kotlin.math.*

class FragmentTwoLinkController : Fragment() {
    private val TAG = this::class.java.simpleName
    private lateinit var mainActivity: MainActivity

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_two_link_controller, container, false)
        mainActivity = activity as MainActivity

        val surfaceView: SurfaceView = view.findViewById(R.id.surfaceViewTwoLink)
        val twoLinkSurfaceView = context?.let { TwoLinkSurfaceView(it, surfaceView, mainActivity.ARM_LENGTH1, mainActivity.ARM_LENGTH2) }
        if (twoLinkSurfaceView != null) {
            surfaceView.setOnTouchListener { _, event ->
                when(event.action) {
                    MotionEvent.ACTION_UP -> {
                        Log.d(TAG, "x=${(event.x - twoLinkSurfaceView.screenWidth / 2) / twoLinkSurfaceView.armScale}, y=${(twoLinkSurfaceView.screenHeight - event.y) / twoLinkSurfaceView.armScale}")
                        val (th1, th2) = inverseKinematics(
                            (event.x - twoLinkSurfaceView.screenWidth / 2) / twoLinkSurfaceView.armScale,
                            (twoLinkSurfaceView.screenHeight - event.y) / twoLinkSurfaceView.armScale
                        )
                        val (x1, y1, x2, y2) = forwardKinematics(th1, th2)
                        twoLinkSurfaceView.drawArm(x1, y1, x2, y2)
                    }
                }
                true
            }
        }
        return view
    }

    private fun forwardKinematics(th1: Double, th2: Double): List<Float> {
        val arm1 = mainActivity.ARM_LENGTH1
        val arm2 = mainActivity.ARM_LENGTH2
        val x1 = arm1 * cos(th1)
        val y1 = arm1 * sin(th1)
        val x2 = x1 + arm2 * cos(th1 + th2)
        val y2 = y1 + arm2 * sin(th1 + th2)
        return listOf(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())
    }

    private fun inverseKinematics(x: Float, y: Float): List<Double> {
        val xx = x.toDouble().pow(2)
        val yy = y.toDouble().pow(2)
        val arm1pow = mainActivity.ARM_LENGTH1.toDouble().pow(2)
        val arm2pow = mainActivity.ARM_LENGTH2.toDouble().pow(2)
        Log.d(TAG, "xx = ${xx}, yy = $yy")
        val th1 = acos((xx + yy + arm1pow - arm2pow) / (2 * mainActivity.ARM_LENGTH1 * sqrt(xx + yy))) + atan2(y, x)
        Log.d(TAG, "sqrt(xx + yy) = ${sqrt(xx + yy)}")
//        Log.d(TAG, "(xx + yy + lenArm2 - lenArm2) / (2 * mainActivity.ARM_LENGTH * sqrt(xx + yy)) = ${(xx + yy + lenArm2 - lenArm2) / (2 * mainActivity.ARM_LENGTH * sqrt(xx + yy))}")
        val th2 = atan2(y - mainActivity.ARM_LENGTH1 * sin(th1), x - mainActivity.ARM_LENGTH1 * cos(th1)) - th1
        Log.d(TAG, "theta1 = ${th1}(${Math.toDegrees(th1)}), theta2 = ${th2}(${Math.toDegrees(th2) + 90})")
        mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + Math.toDegrees(th1)).toByteArray())
        Thread.sleep(100)
        mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE2 + (Math.toDegrees(th2) + 90)).toByteArray())

        return listOf(th1, th2)
    }
}