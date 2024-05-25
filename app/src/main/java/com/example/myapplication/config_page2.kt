package com.example.myapplication

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch


@Composable
fun isSystemInDarkTheme(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
}

@Composable
fun MainScreen(navController: NavController, context: Context, wifiPerm: Boolean, locationPerm: Boolean) {
    val colors = when {
        isSystemInDarkTheme() -> darkColors()
        else -> lightColors()
    }
    val coroutineScope = rememberCoroutineScope()
    var showContent by remember { mutableStateOf(false) }
    MaterialTheme(
        colorScheme = colors
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Text("Drawer title", modifier = Modifier.padding(16.dp))
                    Divider()
                    NavigationDrawerItem(label = { Text(text = "About") }, selected = false, onClick = {
                        showContent = true
                        coroutineScope.launch { drawerState.close() }
                    })
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .height(100.dp)
                    .background(
                        shape = RectangleShape,
                        color = MaterialTheme.colorScheme.background
                    )
            ) {
                if (showContent) {
                    navController.navigate("details")
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .background(
                                    shape = RectangleShape,
                                    color = MaterialTheme.colorScheme.inverseSurface
                                )
                        ) {
                            Button(
                                onClick = { coroutineScope.launch { drawerState.open() } },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = ButtonDefaults.shape,
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
