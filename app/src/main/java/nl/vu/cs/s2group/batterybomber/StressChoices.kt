package nl.vu.cs.s2group.batterybomber

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.navigation.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [StressChoices.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressChoices : Fragment(R.layout.fragment_stress_choices) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val startStressButton: Button = view.findViewById(R.id.start_stress_button)
        val cpuStressCheckBox: CheckBox = view.findViewById(R.id.cpu_stress_checkbox)

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

            if(cpuStress) {
                val cores = Runtime.getRuntime().availableProcessors()
                Log.d(javaClass.name, "Cores available: $cores. Spawning CPU stresser threads")

                for(i in 1..Runtime.getRuntime().availableProcessors()) {
                    val stresserCPU = StresserCPU()
                    stressThreads.add(stresserCPU)
                    stresserCPU.start()
                }
            }
            Toast.makeText(view.context, "Stress test started!", Toast.LENGTH_LONG).show()
            view.findNavController().navigate(R.id.action_stressChoices_to_stressRunning)
        }
    }
}
