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
import android.os.Handler
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
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InterruptedIOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import android.os.Looper
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 * Use the [StressRunning.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressRunning : Fragment(R.layout.fragment_stress_running) {
    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager

    private var cpuStressThreads : List<Thread>? = null
    private val sensorsListener = SensorsListener()
    private val locationListener = LocationListener()
    private val networkExecutorService = Executors.newSingleThreadExecutor()
    private val handlerUI: Handler = Handler(Looper.getMainLooper())

    private fun startStressTest() {
        val args = StressRunningArgs.fromBundle(requireArguments())
        if(args.cpuStress) {
            val cores = Runtime.getRuntime().availableProcessors()
            Timber.d("Cores available: $cores. Spawning CPU stresser threads")

            cpuStressThreads = (1..cores).map {
                Thread(StresserCPU())
            }.toList()
            cpuStressThreads!!.forEach{ it.start() }
        }
        if(args.cameraStress) {
            requireActivity().supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<CameraFragment>(R.id.camera_preview_frag_container_view)
            }
        }

        if(args.sensorsStress) {
            sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
                Timber.d("Stressing sensor: ${sensor.name}")
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
        Timber.d("Stopping stress tests")
        cpuStressThreads?.forEach {
            it.interrupt()
        }
        sensorManager.unregisterListener(sensorsListener)
        locationManager.removeUpdates(locationListener)

        networkExecutorService.shutdownNow()
        while(!networkExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            /* Wait for termination */
        }
        Timber.d("Stress tests stopped")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager   = requireContext().getSystemService(Context.SENSOR_SERVICE  ) as SensorManager
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val stopButton : Button = view.findViewById(R.id.stop_stress_button)
        stopButton.setOnClickListener {
            view.findNavController().navigate(StressRunningDirections.actionStressRunningToStressChoices())
        }

        /*TODO: figure out how to acquire a wake lock and release it on stop
        val powerManager: PowerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        powerManager.newWakeLock(PowerManager.WAK_LOCK)
         */
        startStressTest()
        Toast.makeText(view.context, "Stress test started!", Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopStressTest()
    }

    private abstract inner class Stresser {
        /**
         * This function exists so that each Stresser does not get optimized out
         */
        protected fun impossibleUIUpdateOnMain(impossibleCondition : Boolean) {
            if(impossibleCondition) {
                val s = "Impossible result from ${javaClass.name}"
                Timber.d(s)
                handlerUI.post { Toast.makeText(requireContext(), s, Toast.LENGTH_LONG).show() }
            }
        }
    }

    private inner class SensorsListener() : Stresser(), SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            impossibleUIUpdateOnMain(event.values[0].toDouble() == MyConstants.PI_50)
            //Timber.d("onSensorChanged: ${event.sensor.name} ${event.values[0]}")
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Timber.d("onAccuracyChanged: ${sensor.name}. New accuracy: $accuracy")
        }
    }

    private inner class LocationListener() : Stresser(), android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            impossibleUIUpdateOnMain(location.latitude == MyConstants.PI_50)
            //Timber.d(location.toString())
        }

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    private inner class NetworkStresser : Stresser(), Runnable {
        private val SERVER_URL = URL("https://garbage-traffic.netlify.app/garbage.blob")
        private val dataChunk = ByteArray(32 * 1024 * 1024) //32 MB buffer
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
                    Timber.d("Status: $status")

                    if(status != 200) {
                        Timber.e("Unexpected status code in network request. Stopping.")
                        break
                    }
                    while(inputStream.read(dataChunk) != -1) { //read the response
                        //Timber.d("Status: $status, Data Chunk[0]: ${dataChunk[0].toInt().toChar()}")

                        /* This if condition is impossible to occur but we keep it to prevent the JVM from
                         * optimizing out the entire loop
                         */
                        impossibleUIUpdateOnMain(dataChunk[0].toInt() xor dataChunk.last().toInt() == 300)
                    }

                    inputStream.close()
                } catch(ex: InterruptedIOException) {
                    break
                } finally {
                    con.disconnect()
                }
            }
            Timber.i("Network thread stopped")
        }
    }

    private inner class StresserCPU: Stresser(), Runnable {
        fun ByteArray.toHex(): String = joinToString(separator = "") { b -> "%02x".format(b) }

        override fun run() {
            val randomSeed =  BigInteger( (1..20)
                .map { _ -> Random.nextInt(0, 10) }
                .map { i -> i.toString() }
                .joinToString("")
            ) //20 digit random number

            val algorithm = "SHA-512"
            val md = MessageDigest.getInstance(algorithm)
            val luckySuffix = (1..20).map { "0" }.joinToString("")
            var randomNum = randomSeed

            /* TODO: maybe reuse memory here to prevent the Garbage Collector from kicking in */
            while(!Thread.interrupted()) {
                val digest = md.digest(randomNum.toByteArray())

                impossibleUIUpdateOnMain(digest.toHex().endsWith(luckySuffix))

                md.reset()
                randomNum++
            }
        }
    }
}

private object MyConstants {
    val PI_50 : Double = 3.1415926535897932384626433832795028841971
}

