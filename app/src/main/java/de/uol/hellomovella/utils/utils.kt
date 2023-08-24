package de.uol.hellomovella.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.app.ActivityCompat

object Utils {
    /**
     * Check the current thread is main thread or background thread.
     *
     * @return True - If running on main thread
     */
    val isMainThread: Boolean
        get() = Looper.myLooper() == Looper.getMainLooper()

    /**
     * Check the Bluetooth adapter is enabled or not.
     *
     * @param context The application context
     * @return True - if the Bluetooth adapter is on
     */
    @JvmStatic
    fun isBluetoothAdapterEnabled(context: Context): Boolean {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager != null) {
            val bluetoothAdapter = bluetoothManager.adapter
            if (bluetoothAdapter != null) return bluetoothAdapter.isEnabled
        }
        return false
    }



    /**
     * If the Bluetooth adapter is disabled, popup a system dialog for user to enable it.
     *
     * @param activity    The main activity
     * @param requestCode The request code for this intent
     */
    @JvmStatic
    fun requestEnableBluetooth(activity: Activity, requestCode: Int) {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        activity.startActivityForResult(intent, requestCode)
    }

    /**
     * Above Android 6.0+, user have to  allow app to access location information then scan BLE device.
     *
     * @param activity The activity class
     * @return True - if the permission is granted
     */
    @JvmStatic
    fun isLocationPermissionGranted(activity: Activity): Boolean {
        return activity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * If the location permission isn't granted, popup a system dialog for user to enable it.
     *
     * @param activity    The main activity
     * @param requestCode The request code for this action
     */
    @JvmStatic
    fun requestLocationPermission(activity: Activity, requestCode: Int) {
        activity.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
    }
}