package nl.vu.cs.s2group.batterybomber

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import timber.log.Timber
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var navController: NavController

    private val sourcesViewFragment : Fragment = SourceView()
    private val liveViewFragment    : Fragment = LiveView()
    private var activeFragment      : Fragment = sourcesViewFragment

    private fun switchToFragmentSourceView() : Boolean {
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)

            activeFragment = sourcesViewFragment
            show(activeFragment)
            // addToBackStack("sourcesViewFragment") //FIXME: the bottom_navigation_view doesn't get updated
            commit()
        }
        return true
    }
    private fun switchToFragmentLiveView() : Boolean {
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment)

            activeFragment = liveViewFragment
            show(activeFragment)
            // addToBackStack("liveViewFragment") //FIXME: the bottom_navigation_view doesn't get updated
            commit()
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            //StrictMode.enableDefaults();
        }

        // Setup top navigation
        val topToolBar = findViewById<MaterialToolbar>(R.id.topToolBar)
        topToolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    // Handle About icon press
                    startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                    true
                }
                else -> false
            }
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
        //Programmatically create the two fragments and commit them. Keep the "Sources View" visible as it is the starting fragment
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.nav_fragment, sourcesViewFragment, "sourcesViewFragment") // In the first case, we replace the fragment that is taken from the nav graph
            hide(sourcesViewFragment)
            add(R.id.nav_fragment, liveViewFragment, "liveViewFragment")
            hide(liveViewFragment)

            activeFragment = sourcesViewFragment
            show(activeFragment)
            commit()
        }
    }
}
