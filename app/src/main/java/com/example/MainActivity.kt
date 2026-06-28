package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TradingViewModel
import com.example.ui.TradingViewModelFactory
import com.example.ui.screens.MainDashboard
import com.example.ui.screens.OnboardingScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val viewModel: TradingViewModel = viewModel(
          factory = TradingViewModelFactory(application)
        )
        val userProfile by viewModel.userProfile.collectAsState()

        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val modifier = Modifier.padding(innerPadding)
          if (userProfile == null || !userProfile!!.isOnboarded) {
            OnboardingScreen(viewModel = viewModel, modifier = modifier)
          } else {
            MainDashboard(viewModel = viewModel, modifier = modifier)
          }
        }
      }
    }
  }
}
