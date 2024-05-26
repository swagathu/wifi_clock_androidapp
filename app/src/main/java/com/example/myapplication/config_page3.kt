package com.example.myapplication

import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import java.net.Socket
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.charset.Charset
import java.util.Scanner

@OptIn(DelicateCoroutinesApi::class)
fun readDeviceInfo(ipAddress: String, port: Int, onResult: (String) -> Unit) {
    GlobalScope.launch(Dispatchers.Main) {
        val result = withContext(Dispatchers.IO) {
            MyCoroutineTask.writeToSocket(ipAddress, port, "Hello")
        }
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

                var result = ""
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
fun DeviceInfo_Page(navController: NavController, context: Context, wifiPerm: Boolean, locationPerm: Boolean) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val colors = when {
        isSystemInDarkTheme() -> darkColors()
        else -> lightColors()
    }
    Text(text = "New Screen!")
//    MaterialTheme(colorScheme = colors) {
//        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
//
//        ModalNavigationDrawer(
//            drawerState = drawerState,
//            drawerContent = {
//                ModalDrawerSheet {
//                    Text(
//                        "Other Options",
//                        style = TextStyle(
//                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
//                            fontWeight = FontWeight.W100
//                        )
//                    )
//                    Divider()
//                    NavigationDrawerItem(
//                        label = { Text(text = "About", style = TextStyle(fontWeight = FontWeight.Bold)) },
//                        selected = false,
//                        onClick = {
//                            // Handle click
//                        }
//                    )
//                }
//            },
//        ) {
////            val coroutineScope = rememberCoroutineScope()
////            LaunchedEffect(Unit) {
////                // Your code that needs to be executed once when the composable is first composed
////            }
//
//            // Body content here
//        }
//    }
}
