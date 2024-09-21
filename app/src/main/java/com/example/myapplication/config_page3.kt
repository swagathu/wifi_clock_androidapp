package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException
import java.util.Scanner


fun parseJson(resultx: String?): Map<String, Any>? {
    val result: String = resultx ?: ""
    Log.d("json parse", result)
    return try {
        val jsonObject = JSONObject(result)
        val parsedData = mutableMapOf<String, Any>()

        // Parse the lastConnected object
        if (jsonObject.has("lastConnected")) {
            val lastConnected = jsonObject.getJSONObject("lastConnected")
            parsedData["lastConnectedSSID"] = lastConnected.getString("ssid")
            parsedData["lastConnectedPassword"] = lastConnected.getString("password")
        }

        // Parse the networks array
        if (jsonObject.has("networks")) {
            val networksArray = jsonObject.getJSONArray("networks")
            val networksList = mutableListOf<Map<String, String>>()

            for (i in 0 until networksArray.length()) {
                val network = networksArray.getJSONObject(i)
                networksList.add(
                    mapOf(
                        "ssid" to network.getString("ssid"),
                        "password" to network.getString("password")
                    )
                )
            }
            parsedData["networks"] = networksList
        }

        parsedData
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun readDeviceInfo(ipAddress: String, port: Int, query: String, payload: String, onResult: (String) -> Unit) {

    val jsonObject = JSONObject()
    jsonObject.put("query", query)
    jsonObject.put("payload", payload)
    val data: String = jsonObject.toString()
    GlobalScope.launch(Dispatchers.Main) {
        val result = withContext(Dispatchers.IO) {
            MyCoroutineTask.writeToSocket(ipAddress, port, data)
        }
        Log.d("recive", result)
        onResult(result)
    }
}

class MyCoroutineTask {
    companion object {
        private fun getSocket(ipAddress: String, port: Int): Socket {
            val socket = Socket()
            val address = InetSocketAddress(ipAddress, port)
            socket.connect(address, 10000)  // 10 seconds connect timeout
            socket.soTimeout = 10000  // 10 seconds read timeout
            return socket
        }

        fun writeToSocket(ipAddress: String, port: Int, message: String): String {
            return try {
                val socket = getSocket(ipAddress, port)
                val writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream(), "ASCII"))
                val reader = Scanner(socket.getInputStream(), "ASCII")
                writer.write(message)
                writer.flush()

                var result : String = ""
                while (reader.hasNextLine()) {
                    val line = reader.nextLine()
                    if (line.isEmpty()) {
                        break
                    }
                    result += line
                }

                result
            } catch (e: SocketException) {
                // Handle SocketException (e.g., socket closed prematurely)
                "SocketException: ${e.message}"
            } catch (e: Exception) {
                // Handle other exceptions
                "Error: ${e.message}"
            }
        }

    }
}

@Composable
fun DeviceInfo_Page(
    navController: NavController,
    context: Context,
    wifiPerm: Boolean,
    locationPerm: Boolean,
    result: String?
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()
    var showContent by remember { mutableStateOf(false) }
    val colors = when {
        isSystemInDarkTheme() -> darkColors()
        else -> lightColors()
    }
    Log.d("deviceinfo composable", result?:"")
    val parsedData = remember(result) { parseJson(result) }
    MaterialTheme(colorScheme = colors) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text(
                        "Other Options",
                        style = TextStyle(
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            fontWeight = FontWeight.W100
                        )
                    )
                    Divider()
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = "About",
                                style = TextStyle(fontWeight = FontWeight.Bold)
                            )
                        },
                        selected = false,
                        onClick = {
                            showContent = true
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background)
            ) {
                if (showContent) {
                    navController.navigate("details")
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(color = MaterialTheme.colorScheme.inversePrimary)
                                .padding(horizontal = 10.dp)
                        ) {
                            Button(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Details",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.inverseOnSurface,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }
                            Text(
                                "Wifi Clock",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary,
                                style = TextStyle(
                                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }

                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .background(
//                                    color = MaterialTheme.colorScheme.primary,
//                                    shape = RoundedCornerShape(10.dp)
//                                )
                        ) {
                            Column()
                            {
//                        192.168.29.19
                                Spacer(modifier = Modifier.height(20.dp))
                                if (parsedData != null) {
                                    val lastConnectedSSID =
                                        parsedData["lastConnectedSSID"] as? String
                                    val lastConnectedPassword =
                                        parsedData["lastConnectedPassword"] as? String

                                    if (lastConnectedSSID != null && lastConnectedPassword != null) {
                                        Text("Last Connected:")
                                        Text("SSID: $lastConnectedSSID")
                                        Text("Password: $lastConnectedPassword")
                                    }

                                    // Display networks list
                                    val networks =
                                        parsedData["networks"] as? List<Map<String, String>>
                                    if (networks != null) {
                                        Text("Available Networks:")
                                        networks.forEach { network ->
                                            Text("SSID: ${network["ssid"]}, Password: ${network["password"]}")
                                        }
                                    }
                                } else {
                                    Text("No data available")
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}