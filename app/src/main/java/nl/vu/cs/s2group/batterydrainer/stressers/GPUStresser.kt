package nl.vu.cs.s2group.batterydrainer.stressers

import android.content.Context
import android.view.View
import androidx.core.view.isVisible

class GPUStresser(context: Context, private val gpuCanvas: View) : Stresser(context) {

    override fun start() {
        super.start()
        handlerUI.post { gpuCanvas.isVisible = true }
    }

    override fun stop() {
        super.stop()
        handlerUI.post { gpuCanvas.isVisible = false }
    }
}
