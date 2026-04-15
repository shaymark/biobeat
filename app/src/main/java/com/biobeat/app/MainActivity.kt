package com.biobeat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.biobeat.app.ui.navigation.BioBeatNavGraph
import com.biobeat.app.ui.theme.BioBeatTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BioBeatTheme {
                val navController = rememberNavController()
                BioBeatNavGraph(navController = navController)
            }
        }
    }
}
