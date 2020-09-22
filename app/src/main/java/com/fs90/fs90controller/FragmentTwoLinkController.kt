package com.fs90.fs90controller

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
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
        val twoLinkSurfaceView = context?.let { TwoLinkSurfaceView(it, surfaceView) }
        if (twoLinkSurfaceView != null) {
            surfaceView.setOnTouchListener { _, event ->
                Log.d(TAG, "x=${(event.x - 525F) / 3}, y=${(525F - event.y) / 3}")
                inverseKinematics((event.x - 525F) / 3, (525F - event.y) / 3)
                twoLinkSurfaceView.onTouch(event)
            }
        }
        return view
    }

    private fun inverseKinematics(x: Float, y: Float) {
        val xx = x.toDouble().pow(2)
        val yy = y.toDouble().pow(2)
        val lenArm2 = mainActivity.ARM_LENGTH.toDouble().pow(2)
        Log.d(TAG, "xx = ${xx}, yy = ${yy}")
        val theta1 = acos((xx + yy + lenArm2 - lenArm2) / (2 * mainActivity.ARM_LENGTH * sqrt(xx + yy))) + atan2(y, x)
        Log.d(TAG, "sqrt(xx + yy) = ${sqrt(xx + yy)}")
//        Log.d(TAG, "(xx + yy + lenArm2 - lenArm2) / (2 * mainActivity.ARM_LENGTH * sqrt(xx + yy)) = ${(xx + yy + lenArm2 - lenArm2) / (2 * mainActivity.ARM_LENGTH * sqrt(xx + yy))}")
        val theta2 = atan2(y - mainActivity.ARM_LENGTH * sin(theta1), x - mainActivity.ARM_LENGTH * cos(theta1)) - theta1
        Log.d(TAG, "theta1 = ${theta1}(${Math.toDegrees(theta1)}), theta2 = ${theta2}(${Math.toDegrees(theta2)})")
//        mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + Math.toDegrees(theta1)).toByteArray())
        mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + Math.toDegrees(theta1) + "\r\n" + mainActivity.CMD_ANGLE2 + Math.toDegrees(theta2)).toByteArray())
//        Thread.sleep(500)
//        mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE2 + Math.toDegrees(theta2)).toByteArray())
    }
}