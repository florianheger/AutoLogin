package de.fheger.autologin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.runBlocking

class AutoLoginBroadcastReceiver() : BroadcastReceiver() {
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