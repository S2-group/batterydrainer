package nl.vu.cs.s2group.batterydrainer

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            super.onCreate(savedInstanceState)
        } else {
            installSplashScreen()
            super.onCreate(savedInstanceState)
        }
        Thread.sleep(500) //artificial delay before we switch from the splash screen
        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        finish()
    }
}