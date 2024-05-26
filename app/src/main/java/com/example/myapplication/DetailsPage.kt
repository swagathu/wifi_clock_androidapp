package com.example.myapplication

import android.content.Context
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun DetailsScreen(navController: NavController, context: Context, wifiPerm: Boolean, locationPerm: Boolean) {
    val colors = when {
        isSystemInDarkTheme() -> darkColors()
        else -> lightColors()
    }
    var backFlag by remember { mutableStateOf(false) }
    BackHandler(
        enabled = true,
        onBack = {
            backFlag = true
        }
    )
    if (backFlag) {
        Log.d("DetailsScreen", "BackHandler")
        navController.navigateUp()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .height(100.dp)
            .background(
                shape = RectangleShape,
                color = MaterialTheme.colorScheme.background
            )
    ){
        Column {
            Text(text = "This app was developed by Swagath Unnithan",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}