package com.kchienja.biomet

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.kchienja.biomet.ui.theme.BiometTheme

class MainActivity : ComponentActivity() {

    private var cancellationSignal: CancellationSignal? = null
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BiometTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Greeting(onClick = {launchBiometric()})
                }
            }
        }
    }

    private val authenticationCalBack: BiometricPrompt.AuthenticationCallback
        get() = @RequiresApi(Build.VERSION_CODES.P)
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                notifyUser("Authentication Error $errorCode")
                super.onAuthenticationError(errorCode, errString)
            }

            override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {
                super.onAuthenticationHelp(helpCode, helpString)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                notifyUser("Authentication Succeeded")
                super.onAuthenticationSucceeded(result)
            }
        }


    private fun checkBiometricSupport(): Boolean{
        val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

        if (!keyguardManager.isDeviceSecure){
            notifyUser("lock screen security not enabled in the setting")
            return false
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC) != PackageManager.PERMISSION_GRANTED){
            notifyUser("Finger print authentication permission not enabled")
            return false
        }
        return packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)


    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun launchBiometric(){
        if (checkBiometricSupport()){
            val  biometricPrompt = BiometricPrompt
                .Builder(this)
                .setTitle("Allow Biometric Authentication")
                .setSubtitle("You will no longer required username and password during login")
                .setDescription("We use biometric authentication to protect your data")
                .setNegativeButton("Not Now", this.mainExecutor, {
                    dialogInterface,i ->
                    notifyUser("Authentication cancelled")

                })
                .build()

            biometricPrompt.authenticate(getCancelletionSignal(), mainExecutor, authenticationCalBack)

        }
    }


    private fun getCancelletionSignal(): CancellationSignal{
        cancellationSignal = CancellationSignal()
        cancellationSignal?.setOnCancelListener {
            notifyUser("Ath Cancelled via Signal")
        }

        return cancellationSignal as CancellationSignal
    }

    private fun notifyUser(message: String){
        Log.d("BIOMETRIC", message)
    }
}

@Composable
fun Greeting(onClick: () -> Unit) {

    val username = remember {mutableStateOf(TextFieldValue())}
    val password = remember {mutableStateOf(TextFieldValue())}
    val checked = remember {mutableStateOf(false)}

    Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(20.dp)) {
        OutlinedTextField(
            value = username.value,
            onValueChange = {
                username.value = it
            },
            leadingIcon = {Icon(Icons.Filled.Person, contentDescription = "person")},
            label = { Text(text = "Username")}
        )
        OutlinedTextField(
            value = password.value,
            onValueChange = {
                password.value = it
            },
            leadingIcon = {Icon(Icons.Filled.Edit, contentDescription = "person")},
            label = { Text(text = "password")}
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Row{
                Text(text = "Enable Biometric Auth")
                Switch(checked = checked.value, onCheckedChange = {
                  checked.value = it

                  if (checked.value){
                      onClick()
                  }
                })
            }

            Button(onClick){
                Text(text = "Login")
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BiometTheme {
        Greeting{}
    }
}