package nl.vu.cs.s2group.batterybomber

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    fun markButtonDisable(button: Button) {
        button.isEnabled = false
        button.isClickable = false
        button.setBackgroundColor(getColor(R.color.gray))
    }
    fun markButtonEnable(button: Button) {
        button.isEnabled = true
        button.isClickable = true
        button.setBackgroundResource(android.R.drawable.btn_default)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        return

        //activity for Layout Inflation
        //R stands for Resource (res/)


        /*
        val startStressButton: Button = findViewById(R.id.start_stress_button)
        val stopStressButton : Button = findViewById(R.id.stop_stress_button)
        val cpuStressCheckBox: CheckBox = findViewById(R.id.cpu_stress_checkbox)

        val stressThreads = arrayListOf<Thread>()

        /*TODO: figure out how to acquire a wake lock and release it on stop
        val powerManager: PowerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.WAK_LOCK)
         */

        startStressButton.setOnClickListener {
            val cpuStress: Boolean = cpuStressCheckBox.isChecked
            val hasSelected: Boolean = cpuStress

            if(!hasSelected) {
                //TODO: make a popup that says nothing has been selected
                return@setOnClickListener
            }
            markButtonDisable(startStressButton)



            if(cpuStress) {
                val cores = Runtime.getRuntime().availableProcessors()
                Log.d(javaClass.name, "Cores available: $cores. Spawning CPU stresser threads")

                for(i in 1..Runtime.getRuntime().availableProcessors()) {
                    val stresserCPU = StresserCPU()
                    stressThreads.add(stresserCPU)
                    stresserCPU.start()
                }
            }
            Toast.makeText(this, "Stress test started!", Toast.LENGTH_LONG).show()

            startStressButton.visibility = View.INVISIBLE
            stopStressButton.visibility  = View.VISIBLE
            markButtonEnable(startStressButton)
        }

        stopStressButton.setOnClickListener {
            markButtonDisable(stopStressButton)
            stressThreads.forEach {
                it.interrupt()
            }
            Toast.makeText(this, "Stress stopped!", Toast.LENGTH_LONG).show()

            startStressButton.visibility = View.VISIBLE
            stopStressButton.visibility  = View.INVISIBLE
            markButtonEnable(stopStressButton)
        }
    */
    }
}