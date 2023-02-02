package com.blueiobase.api.android.networkvalidator

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.telephony.TelephonyManager

/**
 * This class handles the logic behind retrieving current network state and monitoring airplane mode changes.
 * @author IO DevBlue.
 * @since 1.0.0
 */
class NetworkValidator(private val context: Context) {

    /** Android network [ConnectivityManager]. */
    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    /** Android [TelephonyManager]. */
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    /** [BroadcastReceiver] for airplane modes. */
    private var airplaneModeReceiver: AirplaneModeReceiver? = null

    /**
     * Listener for Network state changes.
     * @see setOnNetworkStateChangedListener
     */
    var onNetworkChangedListener: OnNetworkChangedListener? = null
        set(value) {
            field = value
            value?.let {
                addCapabilities()
            }
        }

    /** Listener for airplane mode changes. */
    var onAirplaneModeSwitchListener: OnAirplaneModeSwitchListener? = null
        set(value) {
            field = value
            airplaneModeReceiver = if(value != null) {
                AirplaneModeReceiver()
            } else {
                airplaneModeReceiver?.unregister()
                null
            }
        }

    /**
     * Validates if there is internet connection.
     * @return `true` if there is an internet connection, `false` if otherwise.
     */
    fun isOnline(): Boolean {
        var isOnline = false
        val network = connectivityManager.activeNetwork
        network?.let {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(it)
            networkCapabilities?.let {it1 ->
                isOnline = (it1.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) || (it1.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
            }
        }
        return isOnline
    }

    /**
     * Validates if internet connection is available through Wifi.
     * @return `true` if internet is available through Wifi, `false` if otherwise.
     */
    fun isWifiAvailable(): Boolean {
        connectivityManager.apply {
            getNetworkCapabilities(activeNetwork)?.let {
                return it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        }
        return false
    }

    /**
     * Validates if internet connection is available through mobile data.
     * @return `true` if internet is available through mobile data, `false` if otherwise.
     */
    fun isCellularAvailable(): Boolean{
        if(telephonyManager.dataState != TelephonyManager.DATA_DISCONNECTED)
            return true
        return false
    }

    /**
     * Validates is airplane mode is active.
     * @return `true` if airplane mode is active, `false` if otherwise.
     */
    fun isAirplaneModeActive() =  Settings.Global.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

    /** Unregisters the [OnAirplaneModeSwitchListener] from listening for airplane mode events. */
    fun unregisterAirplaneModeSwitchListener() {
        airplaneModeReceiver?.unregister()
    }

    /** Registers the [OnAirplaneModeSwitchListener] to start listening for airplane mode events. */
    fun registerAirplaneModeSwitchListener() {
        airplaneModeReceiver?.register()
    }

    /**
     * Sets a network change listener.
     *
     * **NOTE:** The [execute] lambda receiver function is invoked on a separate background [Thread] on the Android OS.
     * All User Interface operations are **NOT** to be run in the [execute] function without explicitly switching to a UI [Thread].
     * @param execute Lambda receiver function executed on network change.
     */
    fun setOnNetworkStateChangedListener(execute: OnNetworkChangedListener.(Boolean, Network) -> Unit) {
        val x = object: OnNetworkChangedListener {
            override fun onNetworkChanged(isOnline: Boolean, network: Network) {
                execute(isOnline, network)
            }
        }
        onNetworkChangedListener = x
    }

    /**
     * Sets an airplane mode switch listener.
     *
     * **NOTE:** The [execute] lambda receiver function is invoked on a separate background [Thread] on the Android OS.
     * All User Interface operations are **NOT** to be run in the [execute] function without explicitly switching to a UI [Thread].
     * @param execute Lambda receiver function executed on network change.
     */
    fun setOnAirplaneModeSwitchListener(execute: OnAirplaneModeSwitchListener.(Boolean) -> Unit) {
        val x = object: OnAirplaneModeSwitchListener {
            override fun onChanged(turnedOn: Boolean) {
                execute(turnedOn)
            }
        }
        onAirplaneModeSwitchListener = x
        airplaneModeReceiver = AirplaneModeReceiver()
    }

    /** Adds [NetworkCapabilities] to the [NetworkRequest.Builder]. */
    private fun addCapabilities() {
        val builder = NetworkRequest.Builder()
        builder.apply {
            addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            addTransportType(NetworkCapabilities.TRANSPORT_VPN)
        }
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                onNetworkChangedListener?.onNetworkChanged(true, network)
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                onNetworkChangedListener?.onNetworkChanged(false, network)
            }
        }
        connectivityManager.registerNetworkCallback(builder.build(), callback)
    }



    //******************** INTERFACES ********************//
    /** Interface containing callbacks to monitor changes in network state. */
    interface OnNetworkChangedListener {

        /**
         * Callback for network connectivity state.
         *
         * **NOTE:** This function is invoked on a separate background [Thread] on the Android OS. Therefore,
         * All User Interface operations are **NOT** to be executed in this function without explicitly
         * switching to a UI [Thread].
         * @param isOnline `true` if there is network, `false` if otherwise.
         * @param network The [Network] identifier.
         */
        fun onNetworkChanged(isOnline: Boolean, network: Network)
    }

    /** Interface containing callbacks to monitor changes in airplane mode. */
    interface OnAirplaneModeSwitchListener {
        /**
         * Callback for airplane mode changes.
         *
         * **NOTE:** This function is invoked on a separate background [Thread] on the Android OS. Therefore,
         * All User Interface operations are **NOT** to be executed in this function without explicitly
         * switching to a UI [Thread].
         * @param turnedOn `true` if airplane mode is switched on, `false` if otherwise.
         */
        fun onChanged(turnedOn: Boolean)
    }



    //******************** CLASSES ********************//
    /**  Internal [BroadcastReceiver] handling listening to airplane mode events. */
    private inner class AirplaneModeReceiver: BroadcastReceiver() {

        init {
            register()
        }

        override fun onReceive(p0: Context?, p1: Intent?) {
            if(Settings.System.getInt(context.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) == 0) {
                onAirplaneModeSwitchListener?.onChanged(false)
            } else {
                onAirplaneModeSwitchListener?.onChanged(true)
            }
        }

        /** Registers this [AirplaneModeReceiver] to receive changes to airplane mode. */
        fun register() {
            IntentFilter().apply {
                addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                context.registerReceiver(this@AirplaneModeReceiver, this)
            }
        }

        /** Unregisters this [AirplaneModeReceiver] from receiving changes to airplane mode. */
        fun unregister() {
            context.unregisterReceiver(this)
        }
    }
}