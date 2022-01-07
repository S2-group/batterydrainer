package nl.vu.cs.s2group.batterybomber

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import nl.vu.cs.s2group.batterybomber.stressers.CPUStresser
import nl.vu.cs.s2group.batterybomber.stressers.CameraStresser
import nl.vu.cs.s2group.batterybomber.stressers.GPUStresser
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

        val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission is granted. Continue the action or workflow in your app.
                Timber.d("CAMERA permission is granted")
                cameraStresser.start()
            } else {
                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision.
                cameraCard.isChecked = false
                Toast.makeText(requireContext(), "CAMERA denied by the user.", Toast.LENGTH_SHORT).show()
            }
        }


        cpuCard.setOnClickListener {
            cpuCard.toggle()

            when(cpuCard.isChecked) {
                true  -> cpuStresser.start()
                false -> cpuStresser.stop()
            }
        }
        gpuCard.setOnClickListener {
            gpuCard.toggle()

            when(gpuCard.isChecked) {
                true  -> gpuStresser.start()
                false -> gpuStresser.stop()
            }
        }
        cameraCard.setOnClickListener {
            cameraCard.toggle()

            if(cameraCard.isChecked && !cameraStresser.permissionsGranted()) {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                return@setOnClickListener
            }

            assert(cameraStresser.permissionsGranted())
            when(cameraCard.isChecked) {
                true  -> cameraStresser.start()
                false -> cameraStresser.stop()
            }
        }
        sensorsCard.setOnClickListener {
            sensorsCard.toggle()
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
        if(cpuStresser.isRunning)
            cpuStresser.stop()

        if(gpuStresser.isRunning)
            gpuStresser.stop()

        if(cameraStresser.isRunning)
            cameraStresser.stop()
    }
}
