package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.fragment_stress_choices.*
import timber.log.Timber
import java.lang.StringBuilder
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [StressChoices.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressChoices : Fragment(R.layout.fragment_stress_choices) {
    private lateinit var locationManager: LocationManager
    protected val REQUEST_CHECK_SETTINGS = 0x1

    private fun updateOnSelection(selectedComponentsTextView : TextView, stressNames: Map<CheckBox, String>) {
        val sj = StringJoiner(", ", getString(R.string.selected_components) + ": ", "")
        stressNames.filter { it.key.isChecked }.values.forEach { sj.add(it) }
        selectedComponentsTextView.text = sj.toString()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        locationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val startStressButton: Button = view.findViewById(R.id.start_stress_button)
        val cpuStressCheckBox: CheckBox = view.findViewById(R.id.cpu_stress_checkbox)
        val cameraStressCheckBox: CheckBox = view.findViewById(R.id.camera_stress_checkbox)
        val sensorsStressCheckBox: CheckBox = view.findViewById(R.id.sensors_stress_checkbox)
        val locationStressCheckBox: CheckBox = view.findViewById(R.id.location_stress_checkbox)
        val networkStressCheckBox: CheckBox = view.findViewById(R.id.network_stress_checkbox)
        val gpuStressCheckBox: CheckBox = view.findViewById(R.id.gpu_stress_checkbox)
        val selectedComponentsTextView : TextView = view.findViewById(R.id.selectedComponentsTextView)

        val stressNames: Map<CheckBox, String> = mapOf(
            cpuStressCheckBox to "CPU",
            gpuStressCheckBox to "GPU",
            cameraStressCheckBox to "Camera",
            sensorsStressCheckBox to "Sensors",
            networkStressCheckBox to "Network",
            locationStressCheckBox to "Location",
        )

        Timber.i(locationManager.getProviders(false).joinToString(prefix="Found Location Providers: "))

        val requestCameraPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Timber.d("CAMERA permission is granted")
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
                cameraStressCheckBox.isChecked = false
                Toast.makeText(requireContext(), "CAMERA denied by the user.", Toast.LENGTH_SHORT).show()
            }
            updateOnSelection(selectedComponentsTextView, stressNames)
        }
        val requestHSRSensorsLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Timber.d("HIGH_SAMPLING_RATE_SENSORS permission is granted")
            } else {
                sensors_stress_checkbox.isChecked = false
                Toast.makeText(requireContext(), "HIGH_SAMPLING_RATE_SENSORS denied by the user.", Toast.LENGTH_SHORT).show()
            }
            updateOnSelection(selectedComponentsTextView, stressNames)
        }

        val requestLocationLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Timber.d("ACCESS_FINE_LOCATION permission is granted")
                requestHighAccuracyLocation()
            } else {
                locationStressCheckBox.isChecked = false
                Toast.makeText(requireContext(), "ACCESS_FINE_LOCATION denied by the user.", Toast.LENGTH_SHORT).show()
            }
            updateOnSelection(selectedComponentsTextView, stressNames)
        }

        cameraStressCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            updateOnSelection(selectedComponentsTextView, stressNames)
            if(isChecked && !cameraPermissionsGranted()) {
                // Directly ask for permission
                // The registered ActivityResultCallback gets the result of this request.
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        sensorsStressCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateOnSelection(selectedComponentsTextView, stressNames)
            if(isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !HSRSensorsPermissionsGranted()) {
                requestHSRSensorsLauncher.launch(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
            }
        }
        locationStressCheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateOnSelection(selectedComponentsTextView, stressNames)
            if(isChecked) {
                //Request location permission
                if(!locationPermissionsGranted()) {
                    requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) //First requests the location permission and then requests the high accuracy
                } else {
                    requestHighAccuracyLocation()
                }
            }
        }

        listOf(cpuStressCheckBox, gpuStressCheckBox, networkStressCheckBox).forEach{
            it.setOnCheckedChangeListener { _, _ ->  updateOnSelection(selectedComponentsTextView, stressNames) }
        }

        /* TODO: https://developer.android.com/reference/java/net/HttpURLConnection#handling-network-sign-on
         * When the user enables network stresser but the WiFI network requires that they go through a
         * sign-in page first
         */

        startStressButton.setOnClickListener {
            val cpuStress: Boolean = cpuStressCheckBox.isChecked
            val cameraStress: Boolean = cameraStressCheckBox.isChecked
            val sensorsStress: Boolean = sensorsStressCheckBox.isChecked
            val locationStress : Boolean = locationStressCheckBox.isChecked
            val networkStress : Boolean = networkStressCheckBox.isChecked
            val gpuStress : Boolean = gpuStressCheckBox.isChecked
            val hasSelected: Boolean = arrayOf(
                cpuStress, cameraStress, sensorsStress, locationStress, networkStress, gpuStress
            ).any { it }

            if(!hasSelected) {
                Toast.makeText(view.context, "Select at least one component for testing", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            view.findNavController().navigate(StressChoicesDirections.actionStressChoicesToStressRunning(cpuStress, cameraStress, sensorsStress, locationStress, networkStress, gpuStress))
        }
    }

    @Override
    override fun onResume() {
        super.onResume()

        //i.e. the user might have navigated to the settings and change the High Precision
        val locationStressCheckBox: CheckBox = requireView().findViewById(R.id.location_stress_checkbox)
        if(locationStressCheckBox.isChecked)
            requestHighAccuracyLocation()
    }

    @Override
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //val states : LocationSettingsStates = LocationSettingsStates.fromIntent(data)
        if(requestCode == REQUEST_CHECK_SETTINGS) {
            if(resultCode == Activity.RESULT_OK) {
                Timber.i("Location mode HIGH_ACCURACY granted")
            } else if(resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(requireContext(), "Location mode HIGH_ACCURACY denied by the user.", Toast.LENGTH_SHORT).show()
                val locationStressCheckBox: CheckBox = requireView().findViewById(R.id.location_stress_checkbox)
                locationStressCheckBox.isChecked = false
            }
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
                // Location settings are not satisfied, but this can be fixed  by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result in onActivityResult().
                    startIntentSenderForResult(exception.status.resolution.intentSender, REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null)
                } catch (sendEx: IntentSender.SendIntentException) { /* Ignore the error */ }
            } }
    }

    private fun cameraPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun HSRSensorsPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun locationPermissionsGranted(): Boolean {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        else
            TODO("implement me") // On Android 12 (API level 31) or higher we must request both FINE and COARSE grained location
    }
}
