package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.add
import androidx.fragment.app.commit
import androidx.navigation.findNavController
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import nl.vu.cs.s2group.batterybomber.graphics.MyGLSurfaceView
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection


/**
 * A simple [Fragment] subclass.
 * Use the [StressRunning.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressRunning : Fragment(R.layout.fragment_stress_running) {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private val cpuStressThreads = arrayListOf<Thread>()
    private var stressedSensors : List<Sensor>? = null
    private val sensorsListener = SensorsListener()
    private val locationListener = LocationListener()
    private val networkExecutorService = Executors.newSingleThreadExecutor()

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

        if(args.locationStress) {
            //These location providers were enabled during the choices phase
            assert(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            //Permissions were set during choices phase
            assert(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION  ) == PackageManager.PERMISSION_GRANTED)
            assert(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            /*
             * framework location API (https://developer.android.com/reference/android/location/package-summary) drains more battery than the fused provider
             *   * https://developer.android.com/guide/topics/location/battery
             * https://stuff.mit.edu/afs/sipb/project/android/docs/training/basics/location/locationmanager.html
             * https://stackoverflow.com/questions/6775257/android-location-providers-gps-or-network-provider
             *   * https://developerlife.com/2010/10/20/gps/
             */
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER    , 0, 0.0f, locationListener)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, locationListener)
        }

        if(args.networkStress) {
            /* https://developer.android.com/training/efficient-downloads
             *   - Data transfers over broadband consume more battery than WiFi
             *   - It's also more efficient to keep the radio active for longer periods during each transfer session to reduce the frequency of updates.
            */
            networkExecutorService.execute(NetworkStresser())
        }

        if(args.gpuStress) {
            val gpuCanvas: MyGLSurfaceView = requireView().findViewById(R.id.myGLSurfaceView)
            gpuCanvas.isVisible = true
        }

    }
    private fun stopStressTest() {
        cpuStressThreads.forEach {
            it.interrupt()
        }
        cpuStressThreads.clear()
        sensorManager.unregisterListener(sensorsListener)
        locationManager.removeUpdates(locationListener)

        networkExecutorService.shutdownNow()
        while(!networkExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            /* Wait for termination */
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager   = requireContext().getSystemService(Context.SENSOR_SERVICE  ) as SensorManager
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

private object MyConstants {
    val PI_50 : Double = 3.1415926535897932384626433832795028841971
}

private class SensorsListener() : SensorEventListener {

    override fun onSensorChanged(event: SensorEvent) {
        if(event.values[0].toDouble() == MyConstants.PI_50) {
            Log.d(javaClass.name, "${event.sensor.name} congrats! You found the first 50 digits of PI: ${event.values[0]}")
            //TODO: make this a toast instead
        }
        //Log.d(javaClass.name, "onSensorChanged: ${event.sensor.name} ${event.values[0]}")
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        Log.d(javaClass.name, "onAccuracyChanged: ${sensor.name}. New accuracy: $accuracy")
    }
}

private class LocationListener() : LocationListener {
    override fun onLocationChanged(location: Location) {
        if(location.latitude == MyConstants.PI_50) {
            Log.d(javaClass.name, "${location.provider} congrats! You found the first 50 digits of PI: ${location.latitude}")
            //TODO: make this a toast instead
        }
        //Log.d(javaClass.name, location.toString())
    }
    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}

private class NetworkStresser : Runnable {
    private val SERVER_URL = URL("https://garbage-traffic.netlify.app/garbage.blob")
    //private val SERVER_URL = URL("http://192.168.0.107:8080/garbage.blob")

    override fun run() {
        while(!Thread.interrupted()) {
            val con: HttpsURLConnection = SERVER_URL.openConnection() as HttpsURLConnection
            //val con: HttpURLConnection = SERVER_URL.openConnection() as HttpURLConnection

            con.requestMethod = "GET"
            con.setRequestProperty("cache-control", "no-cache,must-revalidate");
            con.setRequestProperty("accept-encoding", "identity"); //prevent compression on server-side

            try {
                val status = con.responseCode //execute the request
                val inputStream = BufferedInputStream(con.inputStream)
                Log.d(javaClass.name, "Status: $status")

                if(status != 200) {
                    Log.e(javaClass.name, "Unexpected status code in network request. Stopping.", )
                    break
                }
                val dataChunk = ByteArray(32 * 1024 * 1024) //32 MB buffer
                while(inputStream.read(dataChunk) != -1) { //read the response
                    /* This if condition is impossible to occur but we keep it to prevent the JVM from
                     * optimizing out the entire loop
                     */
                    if (dataChunk[0].toInt() == 300)
                        Log.d(javaClass.name, "Impossible")
                    //Log.d(javaClass.name, "Status: $status, Data Chunk[0]: ${dataChunk[0].toInt().toChar()}")
                }

                inputStream.close()
            } catch(ex: InterruptedIOException) {
                break
            } finally {
                con.disconnect()
            }
        }
        Log.i(javaClass.name, "Network thread stopped")
    }
}
