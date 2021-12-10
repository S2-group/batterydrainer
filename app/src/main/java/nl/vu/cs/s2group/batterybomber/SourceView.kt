package nl.vu.cs.s2group.batterybomber

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
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

        cpuCard.setOnClickListener {
            cpuCard.toggle()
        }
        gpuCard.setOnClickListener {
            gpuCard.toggle()
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
