package com.blueiobase.api.android.networkvalidator

import android.content.Context
import android.net.Network

/**
 * Extension function on a [Context] to create a [NetworkValidator] instance.
 * @param init Initialization function.
 * @return A valid [NetworkValidator] instance.
 */
fun Context.networkValidator(init: NetworkValidator.() -> Unit): NetworkValidator {
    val nv = NetworkValidator(this)
    nv.apply(init)
    return nv
}

/**
 * Extension function on a [Context] to directly listen for network changes.
 * @param exec Function executed on network changes.
 */
fun Context.listenForNetwork(exec:(Boolean, Network) -> Unit){
    val nv = NetworkValidator(this)
    nv.setOnNetworkStateChangedListener { b, network ->
        exec(b, network)
    }
}