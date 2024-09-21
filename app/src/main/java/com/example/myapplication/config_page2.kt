package com.example.myapplication

//import androidx.compose.foundation.layout.FlowRowScopeInstance.align
import android.content.Context
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch


@Composable
fun isSystemInDarkTheme(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Composable
fun MainScreen(navController: NavController, context: Context, wifiPerm: Boolean, locationPerm: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
    val coroutineScope = rememberCoroutineScope()

    var showContent by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf("Enter IP") }
    var mainButtonClicked by remember { mutableStateOf(false) }
    var buttonEnabled by remember { mutableStateOf(true) }
    var resultText by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("Search the Device") }

    MaterialTheme(colorScheme = colors) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Other Options", style = TextStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize, fontWeight = FontWeight.W100))
                    Divider()
                    NavigationDrawerItem(
                        label = { Text(text = "About", style = TextStyle(fontWeight = FontWeight.Bold)) },
                        selected = false,
                        onClick = {
                            showContent = true
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            },
        ) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background)) {
                if (showContent) {
                    navController.navigate("details")
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(color = MaterialTheme.colorScheme.inversePrimary)
                            .padding(horizontal = 10.dp)) {
                            Button(onClick = { coroutineScope.launch { drawerState.open() } }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground), shape = RoundedCornerShape(10.dp)) {
                                Text("Details", style = TextStyle(color = MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.ExtraBold))
                            }
                            Text("Wifi Clock", modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary, style = TextStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize, fontWeight = FontWeight.ExtraBold))
                        }
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(10.dp)
                            )) {
                            Column {
                                Text("Connection to Wifi Clock:", modifier = Modifier.align(Alignment.Start), color = MaterialTheme.colorScheme.secondary, style = TextStyle(fontSize = MaterialTheme.typography.titleLarge.fontSize, fontWeight = FontWeight.ExtraBold))
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(modifier = Modifier
                                    .border(width = 1.dp, color = Color.Gray)
                                    .width(200.dp)
                                    .height(40.dp)
                                    .padding(horizontal = 10.dp)
                                    .align(Alignment.Start)
                                    .onFocusEvent { focusState ->
                                        if (focusState.hasFocus && ipAddress == "Enter IP") {
                                            ipAddress = ""
                                        } else if (!focusState.hasFocus && ipAddress == "") {
                                            ipAddress = "Enter IP"
                                        }
                                    }) {
                                    BasicTextField(
                                        value = ipAddress,
                                        onValueChange = { newValue -> ipAddress = newValue },
                                        singleLine = true,
                                        textStyle = TextStyle(color = Color.Gray, fontSize = 20.sp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                            .background(Color.Transparent)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(modifier = Modifier
                                    .align(Alignment.Start)
                                    .width(200.dp)
                                    .height(240.dp)
                                    .padding(horizontal = 10.dp)) {
                                    Button(
                                        onClick = {
                                            mainButtonClicked = true
                                            buttonEnabled = false
                                            if (!isValidIpAddress(ipAddress)) {
                                                buttonEnabled = true
                                                statusText = "Enter valid IP"
                                            } else {
                                                statusText = "Searching..."
                                                readDeviceInfo(ipAddress, 80, query = "get_config", payload = "") { result ->
                                                    if (result == "" || result.startsWith("Error") || result.contains("SocketException") || result.contains("timeout", ignoreCase = true)) {
                                                        statusText = "Connection failed"
                                                        buttonEnabled = true
                                                    } else {
                                                        statusText = "Connection successful"
                                                        resultText = result
                                                        Log.d("MainScreen", resultText)
                                                        navController.navigate("result?result=${Uri.encode(result)}")
                                                    }
                                                }
                                            }
                                        },
                                        enabled = buttonEnabled,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(statusText, color = Color.Red.takeIf { statusText.contains("valid IP") } ?: MaterialTheme.colorScheme.inverseOnSurface, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}



fun lightColors(): ColorScheme {
    Log.d("MainScreen", "lightColors")
    return lightColorScheme(
        primary = Color.White,
        background = Color.LightGray
    )
}
fun darkColors(): ColorScheme {
    Log.d("MainScreen", "darkColors")
    return lightColorScheme(
        primary = Color.Black,
        background = Color.DarkGray,
        inversePrimary = Color.White,
        inverseSurface = Color.LightGray,
        secondary = Color.Blue
    )
}
private fun isValidIpAddress(ipAddress: String): Boolean {
    val pattern = ("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")
    return ipAddress.matches(Regex(pattern))
}