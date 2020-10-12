package com.fs90.fs90controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.marcinmoskala.arcseekbar.ArcSeekBar
import com.marcinmoskala.arcseekbar.ProgressListener
import kotlin.math.cos
import kotlin.math.sin

class FragmentOneLinkController : Fragment() {
    private val TAG = this::class.java.simpleName
    private lateinit var mainActivity: MainActivity
    private lateinit var buttonSendAngle: Button
    private lateinit var buttonSendDutyCycle: Button
    private lateinit var arcSeekBar: ArcSeekBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_one_link_controller, container, false)
        mainActivity = activity as MainActivity
        buttonInit(view)
        arcSeekBarInit(view)
        return view
    }

    private fun buttonInit(view: View) {
        // get buttons
        buttonSendAngle = view.findViewById(R.id.buttonSendAngle)
        buttonSendAngle.setOnClickListener {
            val angle: String = view.findViewById<EditText>(R.id.editTextAngle).text.toString()
            mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + angle).toByteArray())
            view.findViewById<EditText>(R.id.editTextAngle).setText("")
        }
        buttonSendDutyCycle = view.findViewById(R.id.buttonSendDutyCycle)
        buttonSendDutyCycle.setOnClickListener {
            val dutyCycle: String = view.findViewById<EditText>(R.id.editTextDutyCycle).text.toString()
            mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_DUTYCYCLE + dutyCycle).toByteArray())
            view.findViewById<EditText>(R.id.editTextDutyCycle).setText("")
        }
    }

    private fun arcSeekBarInit(view: View) {
        arcSeekBar = view.findViewById(R.id.arcSeekBar)
        arcSeekBar.maxProgress = 175
        arcSeekBar.onProgressChangedListener =
            ProgressListener { v ->
                var angle = v.toDouble()
                if (v < 5) {
                    angle = 5.0
                }
                val x = mainActivity.ARM_LENGTH1 * cos(angle)
                val y = mainActivity.ARM_LENGTH1 * sin(angle)
                view.findViewById<TextView>(R.id.textViewSeekValue).text = "Lcos(θ) = $x\n Lsin(θ) = $y"
                mainActivity.bluetoothService?.writeRXCharacteristic((mainActivity.CMD_ANGLE1 + angle).toByteArray())
            }
    }
}