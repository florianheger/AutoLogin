package de.fheger.grimlogin.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.fheger.grimlogin.DataStore
import kotlinx.coroutines.runBlocking

class AutoLoginBroadcastReceiverService() : BroadcastReceiver() {
    private val networkService: NetworkService = NetworkService()

    override fun onReceive(context: Context, intent: Intent?) {
        runBlocking {
            val dataStore = DataStore(context)
            val message = networkService.makePostRequestViaWifi(
                context,
                dataStore.getEmail()!!,
                dataStore.getPassword()!!
            )
            val notificationService = NotificationService(context)
            notificationService.makeNotification("Automatic login", message)
            print(message)
        }
    }
}