package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.navigation.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [StressRunning.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressRunning : Fragment(R.layout.fragment_stress_running) {
    val stressThreads = arrayListOf<Thread>()

    private fun startStressTest() {
        val args = StressRunningArgs.fromBundle(requireArguments())
        if(args.cpuStress) {
            val cores = Runtime.getRuntime().availableProcessors()
            Log.d(javaClass.name, "Cores available: $cores. Spawning CPU stresser threads")

            (1..cores).forEach{ _ ->
                val stresserCPU = StresserCPU()
                stressThreads.add(stresserCPU)
                stresserCPU.start()
            }
        }
    }
    private fun stopStressTest() {
        stressThreads.forEach {
            it.interrupt()
        }
        stressThreads.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val stopButton : Button = view.findViewById(R.id.stop_stress_button)
        stopButton.setOnClickListener {
            stopStressTest()
            view.findNavController().navigate(StressRunningDirections.actionStressRunningToStressChoices())
        }

        /*TODO: figure out how to acquire a wake lock and release it on stop
        val powerManager: PowerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.WAK_LOCK)
         */
        startStressTest()
        Toast.makeText(view.context, "Stress test started!", Toast.LENGTH_LONG).show()
    }
}
