package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import timber.log.Timber
import androidx.fragment.app.FragmentManager


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private fun switchToFragmentSourceView() : Boolean {
        val manager: FragmentManager = supportFragmentManager
        manager.beginTransaction().replace(R.id.nav_fragment, SourceView()).commit()
        return true
    }
    private fun switchToFragmentLiveView() : Boolean {
        val manager: FragmentManager = supportFragmentManager
        manager.beginTransaction().replace(R.id.nav_fragment, LiveView()).commit()
        return true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.sourceViewItem -> switchToFragmentSourceView()
                R.id.liveViewItem -> switchToFragmentLiveView()
                else -> false
            }
        }
    }
}
