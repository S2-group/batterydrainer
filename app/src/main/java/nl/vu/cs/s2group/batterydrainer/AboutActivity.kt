package nl.vu.cs.s2group.batterydrainer

import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import android.view.View.TEXT_ALIGNMENT_VIEW_START
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import mehdi.sakout.aboutpage.AboutPage
import mehdi.sakout.aboutpage.Element
import nl.vu.cs.s2group.batterydrainer.Utils.getAllChildrenFlat


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

        val contributionsHeader = Element()
        contributionsHeader.title = "Project Contributions"
        contributionsHeader.gravity = Gravity.CENTER_HORIZONTAL

        //.addPlayStore("com.ideashower.readitlater.pro")
        //.addGroup("Connect with us")
        aboutPage = AboutPage(this)
            .isRTL(false)
            .setImage(R.drawable.ic_logo)
            .setDescription(Html.fromHtml("" +
                "BatteryDrainer is an android application aiming to drain the phone's battery as fast as possible. " +
                "The idea is that every individual hardware component in a phone consumes an amount of power " +
                "in order to function and consumes maximum power under maximum utilization. " +
                "With this application we demonstrate the need for energy-efficient software and sustainable software engineering " +
                "practices in battery-powered devices. <br/><br/>" +
                "As of 2022, IT systems alone already consume 10% of global electricity and by 2030 it is estimated that, " +
                "the Internet, data centers, telecommunication, and embedded devices will consume one third of the global energy demand. " +
                "Renewable energy is only a half solution as to address the root causes we need green IT, "+
                "to sustainably reduce the energy need of data centers and cloud services worldwide [1].<br/><br/>" +
                """<a href='https://ieeexplore.ieee.org/abstract/document/9585139'>[1]</a> <span style="color:${"#" + Integer.toHexString(ContextCompat.getColor(this, mehdi.sakout.aboutpage.R.color.about_item_text_color) and 0x00ffffff)}">R. Verdecchia, P. Lago, C. Ebert and C. de Vries, "Green IT and Green Software," in IEEE Software, vol. 38, no. 6, pp. 7-15, Nov.-Dec. 2021, doi: 10.1109/MS.2021.3102254.</span>"""
                , Html.FROM_HTML_MODE_COMPACT)
            )
            .addWebsite("https://s2group.cs.vu.nl", "s2group.cs.vu.nl")
            .addTwitter("s2_group", "twitter.com/s2_group")
            .addGitHub("S2-group", "github.com/S2-group")
            .addItem(versionElement)
            .addItem(contributionsHeader)
            .addWebsite("https://www.linkedin.com/in/chalkn", "Nikolaos Chalkiadakis")
            .addWebsite("https://www.ivanomalavolta.com", "Ivano Malavolta")
            .create()
        aboutPage.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        //Make the header "Project Contributions" black
        aboutPage.findViewById<LinearLayout>(mehdi.sakout.aboutpage.R.id.about_providers)
            .getAllChildrenFlat()
            .find { view -> view is TextView && view.text.startsWith("Project Contributions") }
            .apply { with(this as TextView) {
                //TextViewCompat.setTextAppearance(this, mehdi.sakout.aboutpage.R.style.about_groupTextAppearance);
                this.setTextColor(ContextCompat.getColor(context, R.color.defaultTextColor))
        }}

        //align the text and set up the view so that the links are clickable
        val descriptionTextView = aboutPage.findViewById(mehdi.sakout.aboutpage.R.id.description) as TextView
        descriptionTextView.textAlignment = TEXT_ALIGNMENT_VIEW_START
        descriptionTextView.isClickable = true
        descriptionTextView.movementMethod = LinkMovementMethod.getInstance()

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
