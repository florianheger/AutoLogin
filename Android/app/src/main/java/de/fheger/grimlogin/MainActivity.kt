package de.fheger.grimlogin

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
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
import java.util.Calendar
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import de.fheger.grimlogin.services.AutoLoginBroadcastReceiverService
import de.fheger.grimlogin.services.NetworkService
import de.fheger.grimlogin.services.NotificationService

class MainActivity() : ComponentActivity() {
    private val networkService: NetworkService = NetworkService()
    private val notificationService: NotificationService = NotificationService(this)
    private val dataStore: DataStore = DataStore(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationService.createNotificationChannel()

        scheduleDailyLoginIfActive(this)
        enableEdgeToEdge()
        setContent {
            LoginForm(context = this)
        }
    }

    private fun scheduleDailyLoginIfActive(context: Context) {
        val isAutoLoginActive = runBlocking { dataStore.getAutomaticLoginActive() }

        if (!isAutoLoginActive) {
            cancelDailyLogin(context)
            return
        }

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, AutoLoginBroadcastReceiverService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 30)

            if (timeInMillis < System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelDailyLogin(context: Context) {
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AutoLoginBroadcastReceiverService::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    @Composable
    fun LoginForm(context: Context) {
        val dataStore = remember { dataStore }
        val coroutineScope = rememberCoroutineScope()

        var loginId by remember { mutableStateOf(TextFieldValue()) }
        var password by remember { mutableStateOf(TextFieldValue()) }
        var isLoaded by remember { mutableStateOf(false) }
        var isAutoLoginActive by remember { mutableStateOf(false) }


        LaunchedEffect(Unit) {
            val savedLoginId = dataStore.getLoginId() ?: ""
            val savedPassword = dataStore.getPassword() ?: ""
            val savedAutoLoginActive = dataStore.getAutomaticLoginActive()

            loginId = TextFieldValue(savedLoginId)
            password = TextFieldValue(savedPassword)
            isAutoLoginActive = savedAutoLoginActive
            isLoaded = true
        }

        if (!isLoaded) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            return
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp), Arrangement.Center
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_round),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(120.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "RUB LoginID", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = loginId,
                onValueChange = { loginId = it },
                label = { Text("LoginID") },
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
                    updateCredentials(dataStore, loginId, password, context)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { login(loginId.text, password.text, context) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login!")
            }
            Spacer(modifier = Modifier.height(12.dp))
            // Switch for Auto Login
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isAutoLoginActive,
                    onCheckedChange = { isChecked ->
                        isAutoLoginActive = isChecked
                        coroutineScope.launch {
                            dataStore.setAutomaticLoginActive(isChecked)
                        }
                        scheduleDailyLoginIfActive(context)
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAutoLoginActive) "Automatic login active" else "Automatic login not active",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
            }
        }
    }

    private fun updateCredentials(
        dataStore: DataStore,
        email: TextFieldValue,
        password: TextFieldValue,
        context: Context
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.saveCredentials(email.text, password.text)
        }
        Toast.makeText(context, "Credentials updated successfully", Toast.LENGTH_SHORT)
            .show()
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
        LoginForm(context = this)
    }
}