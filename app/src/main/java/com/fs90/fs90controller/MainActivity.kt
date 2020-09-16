package com.fs90.fs90controller

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.marcinmoskala.arcseekbar.ArcSeekBar
import com.marcinmoskala.arcseekbar.ProgressListener
import java.util.*

class MainActivity : AppCompatActivity() {
    private val TAG = this::class.java.simpleName
    private val REQUEST_DEVICE = 1
    private val REQUEST_ENABLE_FINE_LOCATION = 1
    val CMD_ANGLE = "0,"
    val CMD_DUTYCYCLE = "1,"
    val ARM_LENGTH = 85 // 85mm

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            bluetoothService = (binder as BluetoothService.LocalBinder).getService()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bluetoothService?.close()
            bluetoothService = null
            bound = false
        }
    }

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var buttonConnect: Button

    private var bound = false
    private var isConnected = false
    var bluetoothService: BluetoothService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermission()
        serviceStart()
        rootLayout = findViewById(R.id.rootLayout)
        buttonInit()

        val tabLayout: TabLayout = findViewById(R.id.tablayout)
        val viewPager: ViewPager2 = findViewById(R.id.pager)
        val adapter = PagerAdapter(this)
        adapter.addFragment(FragmentOneLinkController())
        adapter.addFragment(FragmentTwoLinkController())
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        TabLayoutMediator(tabLayout, viewPager) {tab, position ->
            tab.text = adapter.fragments[position]::class.java.simpleName.toLowerCase(Locale.ROOT)
        }.attach()
    }

    private fun buttonInit() {
        // get buttons
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonConnect.setOnClickListener {
            if (!isConnected) {
                val intent = Intent(this, ScanActivity::class.java)
                startActivityForResult(intent, REQUEST_DEVICE)
            } else {
                bluetoothService?.disconnect()
                (findViewById<TextView>(R.id.textViewDeviceName)).text = ""
                isConnected = false
            }
        }
    }

    private fun checkPermission() {
        // Check for FINE LOCATION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ENABLE_FINE_LOCATION)
            }
        }
    }

    private fun serviceInit() {
        Intent(this, BluetoothService::class.java).also {
            bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(statusChangeReceiver, makeGattUpdateIntentFilter())
    }
    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BluetoothService.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BluetoothService.ACTION_DATA_AVAILABLE)
        intentFilter.addAction(BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART)
        return intentFilter
    }

    private fun serviceStart() {
        Intent(this, BluetoothService::class.java).also {
            startService(it)
        }
    }

    private val statusChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when(intent.action) {
                BluetoothService.ACTION_GATT_CONNECTED -> {
                    runOnUiThread {
                        Log.d(TAG, "UART_CONNECT_MSG")
                        buttonConnect.text = getString(R.string.disconnect)
                        isConnected = true
                        (findViewById<TextView>(R.id.textViewDeviceName)).text =
                            bluetoothService?.getDeviceName() ?: "null"
                        showSnackbar(rootLayout, "Connection succeeded!")
                    }
                }

                BluetoothService.ACTION_GATT_DISCONNECTED -> {
                    runOnUiThread {
                        Log.d(TAG, "UART_DISCONNECT_MSG")
                        buttonConnect.text = getString(R.string.connect)
                        isConnected = false
                        (findViewById<TextView>(R.id.textViewDeviceName)).text = ""
                        showSnackbar(rootLayout, "Disconnected...")
                        bluetoothService?.disconnect()
                        if (bound) {
                            unbindService(serviceConnection)
                            bound = false
                        }
                    }
                }

                BluetoothService.ACTION_GATT_SERVICES_DISCOVERED -> {
                    bluetoothService?.enableTXNotification()
                }

                BluetoothService.ACTION_DATA_AVAILABLE -> {
                    val txValue = intent.getByteArrayExtra(BluetoothService.EXTRA_DATA)
                    runOnUiThread {
                        try {
                            val text = txValue?.toString(charset("UTF-8"))
                            Log.d(TAG, "Recieved data: ${text}")
                        } catch (e: Exception) {
                            Log.e(TAG, e.toString())
                        }
                    }
                }

                BluetoothService.DEVICE_DOES_NOT_SUPPORT_UART -> {
                    Log.d(TAG, "Device doesn't support UART. Disconnecting")
                    bluetoothService?.disconnect()
                    (findViewById<TextView>(R.id.textViewDeviceName)).text = ""
                    buttonConnect.text = getString(R.string.connect)
                    isConnected = false
                    if (bound) {
                        unbindService(serviceConnection)
                        bound = false
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_DEVICE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val address = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (address?.let { bluetoothService?.connect(it) }!!) {
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        serviceInit()
    }

    override fun onStop() {
        super.onStop()
//        bluetoothService?.disconnect()
//        if (bound) {
//            unbindService(serviceConnection)
//            bound = false
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBluetoothService()
    }

    private fun stopBluetoothService() {
        bluetoothService?.disconnect()
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        bluetoothService?.stopSelf()
        bluetoothService?.close()
        bluetoothService = null
    }

    fun showSnackbar(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.show()
    }
}