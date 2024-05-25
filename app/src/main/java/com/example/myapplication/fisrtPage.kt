package com.example.myapplication

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


@Composable
fun AnimatedTextandHeading(context: Context, text: String) {
    // Create an Animatable object to store the animated value
    val animatedValue = remember { Animatable(0f) }
    val navigateToNextScreen = remember { mutableStateOf(false) }
    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan),
        verticalArrangement = Arrangement.SpaceEvenly,
    ){
        Text(
            text = "Wifi Clock",
            style = TextStyle(
                fontSize = 45.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Magenta,
            ),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            text = text,
            modifier = Modifier.alpha(animatedValue.value)
                .align(Alignment.CenterHorizontally)
                .clickable{
                navigateToNextScreen.value = true
                },
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Red
            )

        )
    }

    // Start an animation to change the alpha value from 0 to 1
    LaunchedEffect(Unit) {
        animatedValue.animateTo(1f, animationSpec = tween(durationMillis = 2000))
    }
    if (navigateToNextScreen.value) {
        // Navigate to the next screen, on pressing start now.
        CheckPermissionAndStart(context)
    }
}