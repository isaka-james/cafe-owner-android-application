package com.udomcive.cafe


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import android.content.SharedPreferences


class SplashScreen : ComponentActivity() {
    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

        // Initialize SharedPreferences
        val sharedPreferences: SharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)


        // Check if a specific key exists in SharedPreferences
        val keyToCheck = "userid" // Replace with the key you want to check
        val userExists = sharedPreferences.contains(keyToCheck)



        if (userExists) {
            // The key exists in SharedPreferences
            //val savedValue = sharedPreferences.getInt(keyToCheck, 0) // Use getInt for integers
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            finish()

        } else {

            // The key doesn't exist in SharedPreferences
            Handler().postDelayed({
                // Start the main activity after the splash screen delay
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                finish()
            }, SPLASH_DELAY)
        }




    }






}
