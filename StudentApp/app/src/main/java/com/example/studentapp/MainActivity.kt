package com.example.studentapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                StudentApp()
            }
        }
    }
}

// Мора да се совпаѓа со RetrofitClient.BASE_URL во TeacherApp
// 10.0.2.2 = localhost на компјутерот кога се користи емулатор
private const val BASE_URL = "http://192.168.0.134/cps/"

@Composable
fun StudentApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var studentName by remember { mutableStateOf("") }

    if (isLoggedIn) {
        ReadyScreen(studentName = studentName)
    } else {
        LoginScreen(
            onLoginSuccess = { name ->
                studentName = name
                isLoggedIn = true
            }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Student App", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Classroom Presence System", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it; errorMsg = "" },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; errorMsg = "" },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (errorMsg.isNotEmpty()) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (username.isBlank() || password.isBlank()) {
                    errorMsg = "Внеси username и password"
                    return@Button
                }
                isLoading = true
                errorMsg = ""
                // Реален API повик кон backend
                doLogin(username, password,
                    onSuccess = { fullName, studentId ->
                        // Зачувај ги податоците во HCE сервисот
                        MyHostApduService.studentId   = studentId
                        MyHostApduService.studentName = fullName
                        isLoading = false
                        onLoginSuccess(fullName)
                    },
                    onError = { msg ->
                        errorMsg = msg
                        isLoading = false
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }
    }
}

@Composable
fun ReadyScreen(studentName: String) {
    val isReady = MyHostApduService.studentId.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("👋 Здраво, $studentName", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isReady)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(if (isReady) "📱" else "⚠️", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    if (isReady) "Ready for Tap" else "НФЦ не е подготвен",
                    fontSize = 24.sp, fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    if (isReady)
                        "Приближи го телефонот до читачот на професорот"
                    else
                        "Рестартирај ја апликацијата",
                    fontSize = 14.sp
                )
            }
        }
    }
}

/**
 * Повикува POST /api/login.php на backend-от.
 * ЗОШТО HttpURLConnection наместо Retrofit?
 * За да не додаваме Retrofit зависност во StudentApp —
 * потребен е само еден API повик.
 */
private fun doLogin(
    username: String,
    password: String,
    onSuccess: (fullName: String, studentId: String) -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        android.util.Log.d("StudentLogin", "Trying to connect to: ${BASE_URL}api/login.php")
        try {
            android.util.Log.d("StudentLogin", "Opening connection...")
            val url = URL("${BASE_URL}api/login.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000

            val body = JSONObject().apply {
                put("username", username)
                put("password", password)
            }.toString()

            OutputStreamWriter(conn.outputStream).use { it.write(body) }

            val code = conn.responseCode

            val responseText = if (code == 200)
                conn.inputStream.bufferedReader().readText()
            else
                conn.errorStream?.bufferedReader()?.readText() ?: ""

            withContext(Dispatchers.Main) {
                if (code == 200) {
                    val json = JSONObject(responseText)
                    val role = json.getJSONObject("user").getString("role")

                    // Само студенти смеат да се логираат
                    if (role != "student") {
                        onError("Овој профил не е студент")
                        return@withContext
                    }

                    val fullName  = json.getJSONObject("user").getString("full_name")
                    // student_id од базата (S001, S002...) — се праќа преку HCE
                    val studentId = json.getJSONObject("user").optString("student_id", username)
                    onSuccess(fullName, studentId)
                } else {
                    val errJson = runCatching { JSONObject(responseText) }.getOrNull()
                    onError(errJson?.optString("error") ?: "Погрешно корисничко ime или лозинка")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("StudentLogin", "ERROR: ${e.javaClass.name}: ${e.message}")
            withContext(Dispatchers.Main) {
                onError("Не може да се поврзе: ${e.message}")
            }
        }
    }
}
