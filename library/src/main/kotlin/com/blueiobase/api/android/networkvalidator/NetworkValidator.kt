package com.blueiobase.api.android.networkvalidator

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings
import android.telephony.TelephonyManager

/**
 * This class handles the logic behind retrieving current network state.
 * @author IO DevBlue.
 * @since 1.0.0
 */
class NetworkValidator(private val context: Context) {

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

    /** Android network [ConnectivityManager]. */
    private val connectivityManager by lazy { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    /** Android [TelephonyManager]. */
    private val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

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

    /**
     * Sets a network change listener.
     *
     * **NOTE:** The [execute] lambda receiver function is invoked on a separate background [Thread]
     * on the Android OS. All User Interface operations are **NOT** to be run in the [execute] function without explicitly switching to a UI [Thread].
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
}