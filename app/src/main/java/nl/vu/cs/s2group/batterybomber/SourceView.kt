package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.content.Context
import android.content.IntentSender
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.card.MaterialCardView
import nl.vu.cs.s2group.batterybomber.stressers.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [SourceView.newInstance] factory method to
 * create an instance of this fragment.
 */
class SourceView : Fragment(R.layout.fragment_source_view) {
    protected val REQUEST_CHECK_SETTINGS = 0x1

    private lateinit var cpuStresser        : CPUStresser
    private lateinit var gpuStresser        : GPUStresser
    private lateinit var cameraStresser     : CameraStresser
    private lateinit var sensorsStresser    : SensorsStresser
    private lateinit var networkStresser    : NetworkStresser
    private lateinit var locationStresser   : LocationStresser
    private lateinit var stressersList      : ArrayList<Stresser>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cpuCard      : MaterialCardView = view.findViewById(R.id.cpuCard)
        val gpuCard      : MaterialCardView = view.findViewById(R.id.gpuCard)
        val cameraCard   : MaterialCardView = view.findViewById(R.id.cameraCard)
        val sensorsCard  : MaterialCardView = view.findViewById(R.id.sensorsCard)
        val networkCard  : MaterialCardView = view.findViewById(R.id.networkCard)
        val locationCard : MaterialCardView = view.findViewById(R.id.locationCard)
        val context = requireContext()

        cpuStresser         = CPUStresser     (context)
        gpuStresser         = GPUStresser     (context, view.findViewById(R.id.myGLSurfaceView))
        cameraStresser      = CameraStresser  (context, childFragmentManager)
        sensorsStresser     = SensorsStresser (context)
        networkStresser     = NetworkStresser (context)
        locationStresser    = LocationStresser(context)
        stressersList       = arrayListOf(cpuStresser, gpuStresser, cameraStresser, sensorsStresser, networkStresser, locationStresser)

        fun stresserPermissionLauncher(isGranted: Boolean, permissionName: String, stresser: Stresser, cardView: MaterialCardView) {
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Timber.d("$permissionName permission is granted")
                stresser.start()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
                cardView.isChecked = false
                Toast.makeText(requireContext(), "$permissionName denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }

        val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            stresserPermissionLauncher(isGranted, "CAMERA", cameraStresser, cameraCard)
        }
        val requestHSRSensorsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            stresserPermissionLauncher(isGranted, "HIGH_SAMPLING_RATE_SENSORS", sensorsStresser, sensorsCard)
        }
        val requestLocationLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted)
                requestHighAccuracyLocation()
            stresserPermissionLauncher(isGranted, "ACCESS_FINE_LOCATION", locationStresser, locationCard)
        }

        fun stresserOnClick(stresser: Stresser, cardView: MaterialCardView) {
            assert(stresser.permissionsGranted())
            when(cardView.isChecked) {
                true  -> stresser.start()
                false -> stresser.stop()
            }
        }
        cpuCard.setOnClickListener {
            cpuCard.toggle()
            stresserOnClick(cpuStresser, cpuCard)
        }
        gpuCard.setOnClickListener {
            gpuCard.toggle()
            stresserOnClick(gpuStresser, gpuCard)
        }
        cameraCard.setOnClickListener {
            cameraCard.toggle()

            if(cameraCard.isChecked && !cameraStresser.permissionsGranted()) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                return@setOnClickListener
            }

            stresserOnClick(cameraStresser, cameraCard)
        }
        sensorsCard.setOnClickListener {
            sensorsCard.toggle()

            if(sensorsCard.isChecked && !sensorsStresser.permissionsGranted()) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    requestHSRSensorsLauncher.launch(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
                }
                return@setOnClickListener
            }

            stresserOnClick(sensorsStresser, sensorsCard)
        }
        networkCard.setOnClickListener {
            networkCard.toggle()
            stresserOnClick(networkStresser, networkCard)
        }
        locationCard.setOnClickListener {
            locationCard.toggle()

            if(locationCard.isChecked) {
                if(!locationStresser.permissionsGranted()) {
                    requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) //First requests the location permission and then requests the high accuracy
                    return@setOnClickListener
                }
                requestHighAccuracyLocation()
            }

            stresserOnClick(locationStresser, locationCard)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopStressTest()
        Timber.d("SourceView destroyed!")
    }

    private fun stopStressTest() {
        stressersList.forEach{ stresser ->
            if(stresser.isRunning)
                stresser.stop()
        }
    }

    /** Request user to change "Location Mode" in settings to "High Accuracy" */
    private fun requestHighAccuracyLocation() {
        //Request user to change "Location Mode" in settings to "High Accuracy"
        val mLocationRequest = LocationRequest.create().apply {
            interval = 1
            fastestInterval = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)
            .setNeedBle(true)
        val client: SettingsClient = LocationServices.getSettingsClient(requireContext())
        val task : Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            Timber.i("Location mode HIGH_ACCURACY is enabled")
            Timber.i(locationManager.getProviders(true).joinToString(prefix="Enabled Location Providers: "))
        }
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    startIntentSenderForResult(exception.status.resolution.intentSender, REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) { /* Ignore the error */ }
            } }
    }
}
