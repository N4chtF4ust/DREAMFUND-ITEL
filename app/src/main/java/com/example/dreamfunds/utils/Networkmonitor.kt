package com.example.dreamfunds.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/** Emits `true` when the device has a validated internet connection, `false` otherwise. */
fun observeNetworkConnectivity(context: Context): Flow<Boolean> = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // Track all networks that are currently validated
    val validatedNetworks = mutableSetOf<Network>()

    fun hasAnyValidatedNetwork(): Boolean = validatedNetworks.isNotEmpty()

    val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            caps: NetworkCapabilities,
        ) {
            val isValidated =
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

            // Add or remove from the validated set based on current state
            if (isValidated) validatedNetworks.add(network)
            else validatedNetworks.remove(network)

            trySend(hasAnyValidatedNetwork())
        }

        override fun onLost(network: Network) {
            // Remove the lost network; we may still have others
            validatedNetworks.remove(network)
            trySend(hasAnyValidatedNetwork())
        }

        override fun onUnavailable() {
            validatedNetworks.clear()
            trySend(false)
        }
    }

    val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .build()

    cm.registerNetworkCallback(request, callback)

    // Seed the set with any currently validated networks before the first callback fires
    cm.allNetworks.forEach { network ->
        val caps = cm.getNetworkCapabilities(network) ?: return@forEach
        if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        ) {
            validatedNetworks.add(network)
        }
    }
    trySend(hasAnyValidatedNetwork())

    awaitClose {
        cm.unregisterNetworkCallback(callback)
        validatedNetworks.clear()
    }
}.distinctUntilChanged()