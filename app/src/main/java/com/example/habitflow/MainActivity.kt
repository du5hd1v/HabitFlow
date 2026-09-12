package com.example.habitflow

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null) {
            val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)
            navView.setupWithNavController(navController)

            navView.setOnItemSelectedListener { item ->
                val handled = NavigationUI.onNavDestinationSelected(item, navController)
                if (handled) {
                    true
                } else {
                    when (item.itemId) {
                        R.id.navigation_home -> {
                            navController.navigate(R.id.navigation_home)
                            true
                        }
                        R.id.navigation_study -> {
                            navController.navigate(R.id.navigation_study)
                            true
                        }
                        R.id.navigation_ai_planner -> {
                            navController.navigate(R.id.navigation_ai_planner)
                            true
                        }
                        R.id.navigation_stats -> {
                            navController.navigate(R.id.navigation_stats)
                            true
                        }
                        else -> false
                    }
                }
            }
        }
    }
}
