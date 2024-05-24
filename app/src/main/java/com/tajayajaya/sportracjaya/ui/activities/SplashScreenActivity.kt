package com.tajayajaya.sportracjaya.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.tajayajaya.sportracjaya.R
import kotlinx.coroutines.delay

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        gotoMainActivity()
    }

    private fun gotoMainActivity() {
        lifecycleScope.launchWhenCreated {
            delay(2000)
            val intent = Intent(this@SplashScreenActivity,MainActivity::class.java)
            startActivity(intent)
        }
    }
}