package nl.vu.cs.s2group.batterybomber

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.findNavController

/**
 * A simple [Fragment] subclass.
 * Use the [StressRunning.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressRunning : Fragment(R.layout.fragment_stress_running) {
    private lateinit var sensorManager: SensorManager
    private val cpuStressThreads = arrayListOf<Thread>()
    private var stressedSensors : List<Sensor>? = null
    private val sensorsListener = SensorsListener()

    private fun startStressTest() {
        val args = StressRunningArgs.fromBundle(requireArguments())
        if(args.cpuStress) {
            val cores = Runtime.getRuntime().availableProcessors()
            Log.d(javaClass.name, "Cores available: $cores. Spawning CPU stresser threads")

            (1..cores).forEach{ _ ->
                val stresserCPU = StresserCPU()
                cpuStressThreads.add(stresserCPU)
                stresserCPU.start()
            }
        }
        if(args.cameraStress) {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CameraFragment>(R.id.camera_preview_frag_container_view)
            }
        }

        if(args.sensorsStress) {
            stressedSensors = sensorManager.getSensorList(Sensor.TYPE_ALL)
            stressedSensors!!.forEach { sensor ->
                Log.d(javaClass.name, "Stressing sensor: ${sensor.name}")
                sensorManager.registerListener(sensorsListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
            }
        }

    }
    private fun stopStressTest() {
        cpuStressThreads.forEach {
            it.interrupt()
        }
        cpuStressThreads.clear()
        sensorManager.unregisterListener(sensorsListener)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

private class SensorsListener() : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent) {
        val pi_50 : Double = 3.1415926535897932384626433832795028841971
        if(event.values[0].toDouble() == pi_50) {
            Log.d(javaClass.name, "${event.sensor.name} congrats! You found the first 50 digits of PI: ${event.values[0]}")
        }
        //Log.d(javaClass.name, "onSensorChanged: ${event.sensor.name} ${event.values[0]}")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(javaClass.name, "onAccuracyChanged: ${sensor.name}. New accuracy: $accuracy")
    }
}
