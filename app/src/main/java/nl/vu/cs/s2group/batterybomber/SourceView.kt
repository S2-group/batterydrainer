package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import nl.vu.cs.s2group.batterybomber.graphics.MyGLSurfaceView
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [SourceView.newInstance] factory method to
 * create an instance of this fragment.
 */
class SourceView : Fragment(R.layout.fragment_source_view) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cpuCard      : MaterialCardView = view.findViewById(R.id.cpuCard)
        val gpuCard      : MaterialCardView = view.findViewById(R.id.gpuCard)
        val cameraCard   : MaterialCardView = view.findViewById(R.id.cameraCard)
        val sensorsCard  : MaterialCardView = view.findViewById(R.id.sensorsCard)
        val networkCard  : MaterialCardView = view.findViewById(R.id.networkCard)
        val locationCard : MaterialCardView = view.findViewById(R.id.locationCard)

        val gpuCanvas: MyGLSurfaceView = requireView().findViewById(R.id.myGLSurfaceView)

        cpuCard.setOnClickListener {
            cpuCard.toggle()
        }
        gpuCard.setOnClickListener {
            gpuCard.toggle()

            //Start/Stop GPU stressing
            Timber.d("Stressing GPU: ${gpuCard.isChecked}")
            gpuCanvas.isVisible = gpuCard.isChecked
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
}
