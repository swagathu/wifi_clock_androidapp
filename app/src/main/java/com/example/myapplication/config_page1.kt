package com.example.myapplication

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

const val REQUEST_CODE_LOCATION_PERMISSION = 1
const val REQUEST_CODE_WIFI_PERMISSION = 123
// Check if location permissions are granted
fun isLocationPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

// Request location permissions
fun requestLocationPermission(activity: Activity) {
    ActivityCompat.requestPermissions(
        activity,
        arrayOf(ACCESS_FINE_LOCATION),
        REQUEST_CODE_LOCATION_PERMISSION
    )
}

// Check if WiFi network info permissions are granted
fun isWifiPermissionGranted(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        ACCESS_WIFI_STATE
    ) == PackageManager.PERMISSION_GRANTED
}

// Request WiFi network info permissions
    fun requestWifiPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(ACCESS_WIFI_STATE),
            REQUEST_CODE_WIFI_PERMISSION
        )
    }

@Composable
fun CheckPermissionAndStart(context: Context){
    var locationPerm by remember { mutableStateOf(false) }
    var wifiPerm by remember { mutableStateOf(false) }
    var delayPassed by remember { mutableStateOf(false) }

    Box (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray),
    ) {
        Text(
            text = "Check Permissions...",
            style = TextStyle(
                color = Color.White,
                fontSize = 35.sp
            ),
        )

        if (isLocationPermissionGranted(context)) {
            locationPerm = true
            // Use location services
        } else {
            // Request location permission
            requestLocationPermission(MainActivity.instance)
            locationPerm = isLocationPermissionGranted(context)
        }


        if (isWifiPermissionGranted(context)) {
            wifiPerm = true
        } else {
            // Request WiFi network info permission
            requestWifiPermission(MainActivity.instance)
            wifiPerm = isWifiPermissionGranted(context)
        }
        if (!(locationPerm && wifiPerm)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray),
            ) {
                Text(
                    text = "You need to Enable all permissions inside settings!!",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 35.sp
                    )
                )
            }
        } else {
        Box (
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray),
        ){
//            Text(text = "Got all Permissions!!")
        }
        LaunchedEffect(key1 = delayPassed) {
//            delay(2000)
            delayPassed = true
        }
        if (delayPassed) {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home") {
                composable(route= "home", enterTransition = null, exitTransition = null, popExitTransition = null, popEnterTransition = null) {
                    MainScreen(navController, context, wifiPerm, locationPerm)
                }
                composable("details",enterTransition = null, exitTransition = null,popExitTransition = null,popEnterTransition = null) {
                    DetailsScreen(navController, context, wifiPerm, locationPerm)
                }
                composable("result",enterTransition = null, exitTransition = null,popExitTransition = null,popEnterTransition = null) {
                        navBackStackEntry ->
                    val result = navBackStackEntry.arguments?.getString("result")
                    DeviceInfo_Page(navController, context, wifiPerm, locationPerm, result)
                }
            }
        }
        }
    }
}