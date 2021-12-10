package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import timber.log.Timber
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

//https://stackoverflow.com/questions/56195791/android-navigation-component-how-save-fragment-state
//https://github.com/android/architecture-components-samples/blob/master/NavigationAdvancedSample/app/src/main/java/com/example/android/navigationadvancedsample/MainActivity.kt
//https://medium.com/@oluwabukunmi.aluko/bottom-navigation-view-with-fragments-a074bfd08711

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController

    private val sourcesViewFragment : Fragment = SourceView()
    private val liveViewFragment    : Fragment = LiveView()
    private var activeFragment      : Fragment = sourcesViewFragment

    private fun switchToFragmentSourceView() : Boolean {
        supportFragmentManager.beginTransaction().hide(activeFragment).show(sourcesViewFragment).commit();
        return true
    }
    private fun switchToFragmentLiveView() : Boolean {
        supportFragmentManager.beginTransaction().hide(activeFragment).show(liveViewFragment).commit();
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Setup the bottom navigation view with navController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.setupWithNavController(navController)

        //Setup fragment navigation
        bottomNavigationView.setOnItemSelectedListener { item ->
            when(item.itemId) {
                R.id.sourceViewItem -> switchToFragmentSourceView()
                R.id.liveViewItem -> switchToFragmentLiveView()
                else -> false
            }
        }

        //Workaround since BottomNavigationView does not support fragment states
        //https://medium.com/@oluwabukunmi.aluko/bottom-navigation-view-with-fragments-a074bfd08711
        //Programmatically create the two fragments and commit them. Keep the "Sources View" visible as it is the starting fragment
        supportFragmentManager.beginTransaction().add(R.id.nav_fragment, liveViewFragment   , "2").hide(liveViewFragment).commit();
        supportFragmentManager.beginTransaction().add(R.id.nav_fragment, sourcesViewFragment, "1").commit();
        supportFragmentManager.beginTransaction().replace(R.id.nav_fragment, activeFragment).commit()
    }
}
