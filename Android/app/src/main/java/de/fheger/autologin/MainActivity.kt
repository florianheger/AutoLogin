package de.fheger.autologin

import android.content.Context
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

class MainActivity() : ComponentActivity() {
    private var networkService: NetworkService = NetworkService()

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
            val message = networkService.makePostRequestViaWifi(
                context,
                email,
                password
            )
            Toast.makeText(context, message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewEmailPasswordForm() {
        EmailPasswordForm(context = this)
    }
}