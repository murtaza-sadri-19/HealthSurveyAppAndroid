package com.example.healthsurveyappandroid.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.healthsurveyappandroid.repository.SurveyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NetworkReceiver(
    private val repository: SurveyRepository,
    private val onSyncComplete: (Boolean) -> Unit = {}
) : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkReceiver"
    }

    private var syncJob: Job? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            ConnectivityManager.CONNECTIVITY_ACTION -> {
                if (isInternetAvailable(context)) {
                    Log.d(TAG, "Network connection available - attempting sync")
                    syncJob?.cancel() // Cancel any pending sync
                    syncJob = CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val syncResult = repository.syncPendingSurveys()
                            val success = when (syncResult) {
                                is SurveyRepository.SyncState.Success -> true
                                else -> false
                            }
                            onSyncComplete(success)
                            Log.d(TAG, "Sync completed with success: $success")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error during sync", e)
                            onSyncComplete(false)
                        }
                    }
                } else {
                    Log.d(TAG, "Network connection lost")
                }
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                    as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            Log.e(TAG, "Error checking network status", e)
            false
        }
    }
}