package com.fs90.fs90controller

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import kotlin.math.*

class Fragment3DOFController : Fragment() {
    private val TAG = this::class.java.simpleName
    private lateinit var mainActivity: MainActivity
    private lateinit var buttonSend: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_3dof_controller, container, false)
        mainActivity = activity as MainActivity
        buttonInit(view)
        return view
    }

    private fun buttonInit(view: View) {
        // get buttons
        buttonSend = view.findViewById(R.id.button3dofSend)
        buttonSend.setOnClickListener {
            val x: String = view.findViewById<EditText>(R.id.editText3dofX).text.toString()
            val y: String = view.findViewById<EditText>(R.id.editText3dofY).text.toString()
            val z: String = view.findViewById<EditText>(R.id.editText3dofZ).text.toString()
            if (x.isNotEmpty() && y.isNotEmpty() && z.isNotEmpty()) {
                val (th1, th2, th3) = inverseKinematics(x.toFloat(), y.toFloat(), z.toFloat())
                mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + Math.toDegrees(th1)).toByteArray())
                Thread.sleep(100)
                mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE2 + Math.toDegrees(th2)).toByteArray())
                Thread.sleep(100)
                mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE3 + Math.toDegrees(th3) + 90).toByteArray())
            }
            view.findViewById<EditText>(R.id.editText3dofX).setText("")
            view.findViewById<EditText>(R.id.editText3dofY).setText("")
            view.findViewById<EditText>(R.id.editText3dofZ).setText("")
        }
    }

    private fun forwardKinematics(th1: Double, th2: Double, th3: Double): List<Float> {
        val base = mainActivity.BASE_HEIGHT
        val arm1 = mainActivity.ARM_LENGTH1
        val arm2 = mainActivity.ARM_LENGTH2
        val x = (arm1 * cos(th1) + arm2 * cos(th2)) * cos(th3)
        val y = (arm1 * cos(th1) + arm2 * cos(th2)) * sin(th3)
        val z =  arm1 * sin(th1) + arm2 * sin(th1 + th2) + base
        return listOf(x.toFloat(), y.toFloat(), z.toFloat())
    }

    private fun inverseKinematics(x: Float, y: Float, z: Float): List<Double> {
        val base = mainActivity.BASE_HEIGHT
        val arm1 = mainActivity.ARM_LENGTH1
        val arm2 = mainActivity.ARM_LENGTH2
        val op = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2))
        val oppow   = op.pow(2)
        val arm1pow = arm1.toDouble().pow(2)
        val arm2pow = arm2.toDouble().pow(2)
        val alpha = acos((arm1pow + arm2pow - oppow) / (2 * arm1 * arm2))
        val beta  = acos((arm1pow + oppow - arm2pow) / (2 * arm1 * op))
        val gamma = asin((z - base) / op)
        val th1 = atan2(y, x).toDouble()
        val th2 = gamma + beta
        val th3 = -(Math.PI - alpha)
        Log.d(TAG, "theta1 = ${th1}(${Math.toDegrees(th1)}°), theta2 = ${th2}(${Math.toDegrees(th2)}°), theta3 = ${th3}(${Math.toDegrees(th3) + 90}°)")
        return listOf(th1, th2, th3)
    }
}