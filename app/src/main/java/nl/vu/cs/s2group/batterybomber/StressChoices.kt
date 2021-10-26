package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import kotlinx.android.synthetic.main.fragment_stress_choices.*

/**
 * A simple [Fragment] subclass.
 * Use the [StressChoices.newInstance] factory method to
 * create an instance of this fragment.
 */
class StressChoices : Fragment(R.layout.fragment_stress_choices) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val startStressButton: Button = view.findViewById(R.id.start_stress_button)
        val cpuStressCheckBox: CheckBox = view.findViewById(R.id.cpu_stress_checkbox)
        val cameraStressCheckBox: CheckBox = view.findViewById(R.id.camera_stress_checkbox)
        val sensorsStressCheckBox: CheckBox = view.findViewById(R.id.sensors_stress_checkbox)
        val locationStressCheckBox: CheckBox = view.findViewById(R.id.location_stress_checkbox)

        val requestCameraPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Log.d(this.javaClass.name, "CAMERA permission is granted")
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
                cameraStressCheckBox.isChecked = false
                Toast.makeText(requireContext(), "CAMERA Permissions denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }
        val requestHSRSensorsLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(this.javaClass.name, "HIGH_SAMPLING_RATE_SENSORS permission is granted")
            } else {
                sensors_stress_checkbox.isChecked = false
                Toast.makeText(requireContext(), "HIGH_SAMPLING_RATE_SENSORS denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }

        val requestLocationLauncher = registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Log.d(this.javaClass.name, "ACCESS_FINE_LOCATION permission is granted")
            } else {
                locationStressCheckBox.isChecked = false
                Toast.makeText(requireContext(), "ACCESS_FINE_LOCATION denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }

        cameraStressCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked && !cameraPermissionsGranted()) {
                // Directly ask for permission
                // The registered ActivityResultCallback gets the result of this request.
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
        sensorsStressCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !HSRSensorsPermissionsGranted()) {
                requestHSRSensorsLauncher.launch(Manifest.permission.HIGH_SAMPLING_RATE_SENSORS)
            }
        }
        locationStressCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked && !locationPermissionsGranted()) {
                requestLocationLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        startStressButton.setOnClickListener {
            val cpuStress: Boolean = cpuStressCheckBox.isChecked
            val cameraStress: Boolean = cameraStressCheckBox.isChecked
            val sensorsStress: Boolean = sensorsStressCheckBox.isChecked
            val locationStress : Boolean = locationStressCheckBox.isChecked
            val hasSelected: Boolean = arrayOf(
                cpuStress, cameraStress, sensorsStress, locationStress
            ).any { it }

            if(!hasSelected) {
                Toast.makeText(view.context, "Select at least one component for testing", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            view.findNavController().navigate(StressChoicesDirections.actionStressChoicesToStressRunning(cpuStress, cameraStress, sensorsStress, locationStress))
        }
    }

    private fun cameraPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun HSRSensorsPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED)
    }

    private fun locationPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }
}
