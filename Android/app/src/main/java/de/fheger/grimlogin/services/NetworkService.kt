package de.fheger.grimlogin.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class NetworkService {
    suspend fun makePostRequestViaWifi(
        context: Context, loginId: String, password: String
    ): String {
        val url = "https://login.ruhr-uni-bochum.de/cgi-bin/laklogin"
        val ipAddress = getLoginIpAddress()

        println("Try to login: mail: $loginId, ipaddr: $ipAddress")

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val wifiNetwork = connectivityManager.activeNetwork?.let { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true) network else null
        } ?: return "Login failed: No active WiFi connection found."

        val client = OkHttpClient.Builder().socketFactory(wifiNetwork.socketFactory).build()

        val requestBody = FormBody.Builder()
            .add("code", "1")
            .add("loginid", loginId)
            .add("password", password)
            .add("ipaddr", ipAddress)
            .add("action", "Login")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                getSuccessOrErrorMessage(response)
            } catch (e: IOException) {
                "Login failed: ${e.message}"
            }
        }
    }

    private fun getSuccessOrErrorMessage(response: Response): String {
        val html = response.body?.string()
        if (html != null && html.contains("Authentisierung gelungen"))
            return "Login successful"
        return "Login failed"
    }

    private suspend fun getLoginIpAddress(): String {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://login.ruhr-uni-bochum.de/cgi-bin/start")
                val connection = url.openConnection() as HttpsURLConnection
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpsURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }


                    val regex = """<input\s+type="ipaddr"\s+name="ipaddr"\s+value="([\d.]+)""""
                    val pattern = Pattern.compile(regex)
                    val matcher = pattern.matcher(response)

                    if (matcher.find()) {
                        return@withContext matcher.group(1) as String
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext ""
        }
    }
}