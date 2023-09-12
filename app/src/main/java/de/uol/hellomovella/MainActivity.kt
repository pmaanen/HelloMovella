package de.uol.hellomovella

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanSettings
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.xsens.dot.android.sdk.DotSdk
import com.xsens.dot.android.sdk.events.DotData
import com.xsens.dot.android.sdk.interfaces.DotDeviceCallback
import com.xsens.dot.android.sdk.interfaces.DotScannerCallback
import com.xsens.dot.android.sdk.models.FilterProfileInfo
import com.xsens.dot.android.sdk.models.DotDevice
import com.xsens.dot.android.sdk.models.DotPayload
import com.xsens.dot.android.sdk.utils.DotScanner
import de.uol.hellomovella.utils.Utils.isBluetoothAdapterEnabled
import de.uol.hellomovella.utils.Utils.isLocationPermissionGranted
import de.uol.hellomovella.utils.Utils.requestEnableBluetooth
import de.uol.hellomovella.utils.Utils.requestLocationPermission
import edu.ucsd.sccn.LSL
import edu.ucsd.sccn.LSL.StreamOutlet
import java.util.UUID
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), DotDeviceCallback, DotScannerCallback {
    var isScanning: Boolean = false
    var mXsScanner: DotScanner = DotScanner(null, null);
    var mDeviceList: ArrayList<DotDevice> = ArrayList<DotDevice>()
    var mActiveDeviceList: ArrayList<DotDevice> = ArrayList<DotDevice>()
    var mSensorOutlet: StreamOutlet? = null

    //var mLSLOutlets: ArrayList
    var btnScan: Button? = null
    var btnConnect: Button? = null
    var tvResultsList: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnScan = findViewById<Button>(R.id.btnScan)
        tvResultsList = findViewById<TextView>(R.id.DataTextView)

        mXsScanner = DotScanner(applicationContext, this)

        //DotScanner(this@MainActivity, this);
        mXsScanner.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        Log.i(TAG, "Movella DOT SDK version ${DotSdk.getSdkVersion()} initialized")
        //DotSdk.setDebugEnabled(true)
        DotSdk.setReconnectEnabled(true)
        btnScan!!.setOnClickListener { onScanBtnClicked() }
        btnConnect = findViewById<Button>(R.id.btnConnect)
        btnConnect!!.setOnClickListener { onConnectBtnClicked() }

        findViewById<Button>(R.id.btnDisconnect)?.setOnClickListener { onDisconnectBtnClicked() }
    }

    override fun onDotConnectionChanged(address: String?, state: Int) {
        if (state == DotDevice.CONN_STATE_CONNECTED) {
            var device = mDeviceList.find { it.address == address }
            if (device != null) {
                Log.i(
                    TAG,
                    "Connected to ${device.name} ${device.tag} ${device.serialNumber.takeLast(4)}"
                )
            }
        }
        if (state == DotDevice.CONN_STATE_DISCONNECTED) {
            var device = mDeviceList.find { it.address == address }
            if (device != null) {
                Log.i(
                    TAG,
                    "Disconnected from ${device.name} ${device.tag} ${device.serialNumber.takeLast(4)}"
                )
            }

        }
    }

    override fun onDotServicesDiscovered(address: String?, state: Int) {
        Log.i(TAG, "onDotServicesDiscovered $address")
    }

    override fun onDotFirmwareVersionRead(address: String?, state: String?) {
        Log.i(TAG, "onDotFirmwareVersionRead $address")
    }

    override fun onDotTagChanged(address: String?, state: String?) {
        Log.i(TAG, "onDotTagChanged $address")
    }

    override fun onDotBatteryChanged(address: String?, state: Int, p2: Int) {
        Log.i(TAG, "onDotBatteryChanged $address")
    }

    override fun onDotDataChanged(address: String?, data: DotData?) {
        mSensorOutlet?.push_chunk(data?.calFreeAcc)
    }

    override fun onDotInitDone(address: String?) {
        var device = mDeviceList.find { it.address == address }
        if (device != null) {
            Log.i(
                TAG,
                "Init done ${device.name} ${device.tag}"
            )
        }
    }

    override fun onDotButtonClicked(address: String?, state: Long) {
        Log.i(TAG, "onDotButtonClicked $address $state")
        var xsDevice = mDeviceList.find { it.address == address }
        if (xsDevice == null)
            Log.e(TAG, "Button pressed for device $address but is not in device list!")
        xsDevice?.identifyDevice()
        Log.i(
            TAG,
            "Button pressed on ${xsDevice?.name}/${xsDevice?.tag}/${
                xsDevice?.serialNumber?.takeLast(4)
            }/${xsDevice?.address}"
        )
        xsDevice?.tag = "Test"
        xsDevice?.powerOffDevice()
//        var info = LSL.StreamInfo(
//            "${xsDevice?.name} ${xsDevice?.address}",
//            "eeg",
//            3,
//            xsDevice!!.currentOutputRate.toDouble(),
//            LSL.ChannelFormat.float32,
//            UUID.randomUUID().toString()
//        )
//        if (mSensorOutlet == null) {
//            mSensorOutlet = LSL.StreamOutlet(info)
//            xsDevice?.measurementMode = DotPayload.PAYLOAD_TYPE_COMPLETE_EULER
//            xsDevice?.startMeasuring()
//        } else {
//            Log.e(TAG, "Stream already active!")
//        }
    }

    override fun onDotPowerSavingTriggered(address: String?) {

    }

    override fun onReadRemoteRssi(address: String?, state: Int) {

    }

    override fun onDotOutputRateUpdate(address: String?, state: Int) {

    }

    override fun onDotFilterProfileUpdate(address: String?, state: Int) {

    }

    override fun onDotGetFilterProfileInfo(
        address: String?,
        state: ArrayList<FilterProfileInfo>?
    ) {

    }

    override fun onSyncStatusUpdate(address: String?, state: Boolean) {

    }

    override fun onDotScanned(device: BluetoothDevice?, rssi: Int) {
        if (device != null) {
            var xsDevice = DotDevice(applicationContext, device, this)
            if (mDeviceList.find { it.address == xsDevice.address } == null) {
                mDeviceList.add(xsDevice)
                xsDevice.connect()
            }
        } else {
            Log.i(TAG, "address is null!")
        }
        val tvMyTextView = findViewById<TextView>(R.id.DataTextView)
        tvMyTextView.text = "Found ${mDeviceList.size} devices"
    }

    /**
     * Check the state of Bluetooth adapter and location permission.
     */
    private fun checkBluetoothAndPermission(): Boolean {
        val isBluetoothEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else isBluetoothAdapterEnabled(this)
        val isPermissionGranted = isLocationPermissionGranted(this)
        if (isBluetoothEnabled) {
            if (!isPermissionGranted) requestLocationPermission(this, REQUEST_PERMISSION_LOCATION)
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT
                    )
                )
            } else {
                requestEnableBluetooth(this, REQUEST_ENABLE_BLUETOOTH)
            }
        }
        val status = isBluetoothEnabled && isPermissionGranted
        Log.i(TAG, "checkBluetoothAndPermission() - $status")
        return status
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }

    private fun onScanBtnClicked() {
        if (checkBluetoothAndPermission())
            if (!isScanning) {
                for (d in mDeviceList) {
                    d.disconnect()
                }
                mSensorOutlet?.close()
                mSensorOutlet = null
                mDeviceList.clear()
                mXsScanner.startScan()
                btnScan!!.text = "Stop Scan"
                isScanning = true
            } else {
                btnScan!!.text = "Start Scan"
                mXsScanner.stopScan()
                tvResultsList!!.text = "Found ${mDeviceList.size} device
                var res = "Found ${mDeviceList.size} devices:\n"
                for (d in mDeviceList) {
                    res += "${d.name} ${d.tag} ${d.serialNumber.takeLast(4)}!\n"
                }
                tvResultsList!!.text = res
                isScanning = false
            }
    }

    private fun onConnectBtnClicked() {
        for (d in mDeviceList) {
            if (d.name != null)
                d.connect()
        }
    }


    private fun onDisconnectBtnClicked() {
        for (d in mDeviceList) {
            if (d.name != null)
                d.disconnect()
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName

        // The code of request
        private const val REQUEST_ENABLE_BLUETOOTH = 1001
        private const val REQUEST_PERMISSION_LOCATION = 1002

    }
}
