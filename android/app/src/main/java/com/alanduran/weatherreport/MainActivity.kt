package com.alanduran.weatherreport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alanduran.weatherreport.ui.WeatherApp
import com.alanduran.weatherreport.ui.theme.WeatherReportTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherReportTheme {
                WeatherApp()
            }
        }
    }
}
