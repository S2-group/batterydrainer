package nl.vu.cs.s2group.batterybomber

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.appbar.MaterialToolbar
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import timber.log.Timber

class AboutActivity : AppCompatActivity(R.layout.activity_about) {
    private lateinit var aboutPage: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<MaterialToolbar>(R.id.aboutActivityTopToolBar)
        val layout = findViewById<LinearLayout>(R.id.aboutScrollView)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val versionElement = Element()
        versionElement.title = "Version 0.1"

        //.addPlayStore("com.ideashower.readitlater.pro")
        //.addGroup("Connect with us")
        aboutPage = AboutPage(this)
            .isRTL(false)
            .setDescription("" +
                "BatteryBomber is an android application aiming to drain the phone's battery as fast as possible. " +
                "The idea is that every individual hardware component in a phone consumes an amount of power " +
                "in order to function and consumes maximum power under maximum utilization. " +
                "With this application we demonstrate the need for energy-efficient software and sustainable software engineering " +
                "practices in battery-powered devices.")
            .setImage(R.drawable.ic_logo)
            .addItem(versionElement)
            .addWebsite("https://s2group.cs.vu.nl", "s2group.cs.vu.nl")
            .addTwitter("s2_group", "twitter.com/s2_group")
            .addGitHub("S2-group", "github.com/S2-group")
            .create()

        //align the text
        aboutPage.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        aboutPage.findViewById<TextView>(mehdi.sakout.aboutpage.R.id.description).textAlignment = TEXT_ALIGNMENT_VIEW_START

        //remove excessive margins from the logo
        val logoImageView = aboutPage.findViewById<ImageView>(mehdi.sakout.aboutpage.R.id.image)
        val logoLayout = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        logoLayout.setMargins(0,0,0,0)
        logoImageView.layoutParams = logoLayout

        //show the "About" view
        val replaceableView = findViewById<View>(R.id.customAboutView)
        val index = layout.indexOfChild(replaceableView);
        layout.removeView(replaceableView)
        layout.addView(aboutPage, index)
    }
}
