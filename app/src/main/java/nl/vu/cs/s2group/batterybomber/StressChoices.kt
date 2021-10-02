package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
        val cameraStressCheckBox: CheckBox = view.findViewById(R.id.camera_stress_checkbox)

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Log.d(this.javaClass.name, "Permission is granted")
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
                cameraStressCheckBox.isChecked = false
                Toast.makeText(requireContext(), "Permissions denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }

        cameraStressCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked && !cameraPermissionsGranted()) {
                // Directly ask for permission
                // The registered ActivityResultCallback gets the result of this request.
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        startStressButton.setOnClickListener {
            val cpuStress: Boolean = cpuStressCheckBox.isChecked
            val cameraStress: Boolean = cameraStressCheckBox.isChecked
            val hasSelected: Boolean = arrayOf(
                cpuStress, cameraStress
            ).any { it }

            if(!hasSelected) {
                Toast.makeText(view.context, "Select at least one component for testing", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            view.findNavController().navigate(StressChoicesDirections.actionStressChoicesToStressRunning(cpuStress, cameraStress))
        }
    }

    private fun cameraPermissionsGranted(): Boolean {
        return (ContextCompat.checkSelfPermission(requireActivity().baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
}
