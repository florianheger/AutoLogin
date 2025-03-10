package de.fheger.autologin

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern
import javax.net.ssl.HttpsURLConnection

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EmailPasswordForm(context = this)
        }
    }

    @Composable
    fun EmailPasswordForm(context: Context) {
        val loginId = remember { LoginId(context) }

        var email by remember { mutableStateOf(TextFieldValue()) }
        var password by remember { mutableStateOf(TextFieldValue()) }
        var isLoaded by remember { mutableStateOf(false) } // Ensure async loading happens only once

        // Load saved credentials when the composable first starts
        LaunchedEffect(Unit) {
            val savedEmail = loginId.getEmail() ?: ""
            val savedPassword = loginId.getPassword() ?: ""
            email = TextFieldValue(savedEmail)
            password = TextFieldValue(savedPassword)
            isLoaded = true
        }

        if (!isLoaded) {
            // Show a loading indicator while data is being retrieved
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Auto Login", style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Text(text = "RUB LoginID", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        loginId.saveCredentials(email.text, password.text)
                    }
                    Toast.makeText(context, "Credentials updated successfully", Toast.LENGTH_SHORT)
                        .show()
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { login(email.text, password.text, context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login!")
            }
        }
    }

    private fun login(email: String, password: String, context: Context) {
        runBlocking {
            val ipAddress: String = getLoginIpAddress()
            val message = makePostRequestViaWifi(
                context,
                "https://login.ruhr-uni-bochum.de/cgi-bin/laklogin",
                email,
                password,
                ipAddress
            )
            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private suspend fun makePostRequestViaWifi(
        context: Context, url: String, email: String, password: String, ipAddress: String
    ): String {
        println("Try to login: mail: $email, password: $password, ipaddr: $ipAddress")

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Get the active WiFi network
        val wifiNetwork = connectivityManager.allNetworks.firstOrNull { network ->
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        } ?: return "Login failed: No active WiFi connection found."

        // Bind the request to the WiFi network
        val client = OkHttpClient.Builder().socketFactory(wifiNetwork.socketFactory).build()

        // Form-encoded request body
        val requestBody = FormBody.Builder()
            .add("code", "1")
            .add("loginid", email)
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
        val html = response.body?.string();
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

                    // Extract the IP using regex
                    val regex = """<input\s+type="ipaddr"\s+name="ipaddr"\s+value="([\d.]+)""""
                    val pattern = Pattern.compile(regex)
                    val matcher = pattern.matcher(response)

                    if (matcher.find()) {
                        return@withContext matcher.group(1) // Extracted IP Address
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return@withContext ""
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewEmailPasswordForm() {
        EmailPasswordForm(context = this)
    }
}