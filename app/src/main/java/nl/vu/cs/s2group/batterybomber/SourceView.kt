package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import nl.vu.cs.s2group.batterybomber.stressers.*
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [SourceView.newInstance] factory method to
 * create an instance of this fragment.
 */
class SourceView : Fragment(R.layout.fragment_source_view) {
    private lateinit var cpuStresser: CPUStresser
    private lateinit var gpuStresser: GPUStresser
    private lateinit var cameraStresser: CameraStresser
    private lateinit var sensorsStresser: SensorsStresser

    private lateinit var stresserList : ArrayList<Stresser>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cpuCard      : MaterialCardView = view.findViewById(R.id.cpuCard)
        val gpuCard      : MaterialCardView = view.findViewById(R.id.gpuCard)
        val cameraCard   : MaterialCardView = view.findViewById(R.id.cameraCard)
        val sensorsCard  : MaterialCardView = view.findViewById(R.id.sensorsCard)
        val networkCard  : MaterialCardView = view.findViewById(R.id.networkCard)
        val locationCard : MaterialCardView = view.findViewById(R.id.locationCard)

        cpuStresser = CPUStresser(requireContext())
        gpuStresser = GPUStresser(requireContext(), requireView().findViewById(R.id.myGLSurfaceView))
        cameraStresser = CameraStresser(requireContext(), childFragmentManager)
        sensorsStresser = SensorsStresser(requireContext())
        stresserList =  arrayListOf<Stresser>(cpuStresser, gpuStresser, cameraStresser, sensorsStresser)

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
        }
        locationCard.setOnClickListener {
            locationCard.toggle()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopStressTest()
        Timber.d("SourceView destroyed!")
    }

    private fun stopStressTest() {
        stresserList.forEach{stresser ->
            if(stresser.isRunning)
                stresser.stop()
        }
    }
}
