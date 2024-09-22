package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.android.gms.location.LocationServices
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
            parsedData["lastConnectedSSID"] = lastConnected.optString("ssid", "none")
            parsedData["lastConnectedPassword"] = lastConnected.optString("password", "none")
        }

        // Parse the networks array
        if (jsonObject.has("networks")) {
            val networksArray = jsonObject.getJSONArray("networks")
            val networksList = mutableListOf<Map<String, String>>()
            for (i in 0 until networksArray.length()) {
                val network = networksArray.getJSONObject(i)
                networksList.add(
                    mapOf(
                        "ssid" to network.optString("ssid", "none"),
                        "password" to network.optString("password", "none")
                    )
                )
            }
            parsedData["networks"] = networksList
        }

        // Parse the location object
        if (jsonObject.has("location")) {
            val location = jsonObject.getJSONObject("location")
            parsedData["location"] = mapOf(
                "longitude" to location.optString("longitude", "none"),
                "latitude" to location.optString("latitude", "none")
            )
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
    Log.d("deviceinfo composable", result ?: "")

    // Parse JSON
    val parsedData = remember(result) { parseJson(result) }

    // Mutable state for networks
    var networks by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var newSSID by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    // Location state
    var longitude by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }

    // Initialize networks state
    LaunchedEffect(parsedData) {
        parsedData?.let {
            networks = it["networks"] as List<Map<String, String>>
            longitude = (it["location"] as? Map<*, *>)?.get("longitude") as? String ?: ""
            latitude = (it["location"] as? Map<*, *>)?.get("latitude") as? String ?: ""
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    @Composable
    fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, handle accordingly
            fun requestLocationPermission(activity: Activity) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            }
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                longitude = it.longitude.toString()
                latitude = it.latitude.toString()
                Log.d("DeviceInfo_Page", "Current location: ${it.latitude}, ${it.longitude}")
            } ?: run {
                Log.d("DeviceInfo_Page", "No location retrieved.")
            }
        }
    }
    fetchCurrentLocation()
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

                        // Scrollable network list
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                if (networks.isNotEmpty()) {
                                    Text("Available Networks:")
                                    networks.forEachIndexed { index, network ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            var password by remember { mutableStateOf(network["password"] ?: "") }
                                            TextField(
                                                value = password,
                                                onValueChange = { password = it },
                                                label = { Text("Password for ${network["ssid"]}") },
                                                modifier = Modifier.weight(1f)
                                            )
                                            Button(onClick = {
                                                    networks = networks.toMutableList().apply { removeAt(index) }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                                            ) {
                                                Text("Remove")
                                            }
                                        }
                                    }
                                } else {
                                    Text("No data available")
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Add Network:")
                                TextField(
                                    value = newSSID,
                                    onValueChange = { newSSID = it },
                                    label = { Text("SSID") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextField(
                                    value = newPassword,
                                    onValueChange = { newPassword = it },
                                    label = { Text("Password") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = {
                                    if (networks.size < 20) {
                                        networks = networks + mapOf("ssid" to newSSID, "password" to newPassword)
                                        newSSID = ""
                                        newPassword = ""
                                    } else {
                                        // Handle the case when the limit is reached (e.g., show a Toast)
                                    }
                                },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground)
                                    ) {
                                    Text("Add Network")
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Location Coordinates:")
                                TextField(
                                    value = longitude,
                                    onValueChange = { longitude = it },
                                    label = { Text("Longitude") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                TextField(
                                    value = latitude,
                                    onValueChange = { latitude = it },
                                    label = { Text("Latitude") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(onClick = {

                                },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                                    ) {
                                    Text("Use my device location")
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(20.dp))
                                // Save button
                                Button(onClick = {
                                    // Prepare data to send back
                                    val lastConnectedSSID = parsedData?.get("lastConnectedSSID") as? String ?: ""
                                    val lastConnectedPassword = parsedData?.get("lastConnectedPassword") as? String ?: ""
                                    val updatedData = mapOf(
                                        "lastConnected" to mapOf(
                                            "ssid" to lastConnectedSSID,
                                            "password" to lastConnectedPassword
                                        ),
                                        "networks" to networks,
                                        "location" to mapOf("longitude" to longitude, "latitude" to latitude)
                                    )
                                    val ipAddress = "192.168.29.19"
                                    val json = JSONObject(updatedData).toString()
                                    readDeviceInfo(ipAddress, 80, query = "set_config", payload = json) { result ->
                                        if (result == "" || result.startsWith("Error") || result.contains("SocketException") || result.contains("timeout", ignoreCase = true)) {
//                                            buttonEnabled = true
                                        } else {
//                                            navController.navigate("result?result=${Uri.encode(result)}")
                                        }
                                    }
                                    // Log updated data or send it to the server
                                    Log.d("DeviceInfo_Page", "Updated data: $updatedData")
                                },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                                    ) {
                                    Text("Save Changes")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

