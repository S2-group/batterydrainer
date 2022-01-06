package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import nl.vu.cs.s2group.batterybomber.stressers.CPUStresser
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
    }
}
