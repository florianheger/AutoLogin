package de.fheger.autologin.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import de.fheger.autologin.LoginId
import kotlinx.coroutines.runBlocking

class AutoLoginBroadcastReceiverService() : BroadcastReceiver() {
    private val networkService: NetworkService = NetworkService()

    override fun onReceive(context: Context, intent: Intent?) {
        runBlocking {
            val loginId = LoginId(context)
            val message = networkService.makePostRequestViaWifi(
                context,
                loginId.getEmail()!!,
                loginId.getPassword()!!
            )
            print(message)
        }
    }
}