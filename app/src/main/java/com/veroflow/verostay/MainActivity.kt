package com.veroflow.verostay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.veroflow.verostay.navigation.VeroStayNavGraph
import com.veroflow.verostay.ui.theme.VeroStayTheme
import com.veroflow.verostay.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VeroStayTheme(darkTheme = appViewModel.darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    VeroStayNavGraph(navController = navController, appViewModel = appViewModel)
                }
            }
        }
    }
}
