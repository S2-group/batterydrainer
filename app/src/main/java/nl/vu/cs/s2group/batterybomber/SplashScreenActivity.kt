package nl.vu.cs.s2group.batterybomber

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Thread.sleep(500) //artificial delay before we switch from the splash screen
        startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
        finish()
    }
}