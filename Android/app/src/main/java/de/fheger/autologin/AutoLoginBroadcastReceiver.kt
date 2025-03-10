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
            makeNotification(context, message)
        }
    }

    private fun makeNotification(context: Context, message: String) {
//        var title = "Auto Login"
//        var builder = NotificationCompat.Builder(context, "todo")
//            .setContentTitle(title)
//            .setContentText(message)
//            .setPriority(NotificationCompat.PRIORITY_LOW)
//        notif
    }

    private fun requestNotificationPermission(context: Context) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission)
//                != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(
//                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                    101
//                )
//            }
//        }
    }
}