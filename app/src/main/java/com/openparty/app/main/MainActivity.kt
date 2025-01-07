package com.openparty.app.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.openparty.app.navigation.AppNavigation
import com.openparty.app.ui.theme.OpenPartyTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenPartyTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController)
                }
            }
        }
        mainViewModel.trackAppOpenedAndIdentifyUser()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.trackAppOpenedAndIdentifyUser()
    }
}
