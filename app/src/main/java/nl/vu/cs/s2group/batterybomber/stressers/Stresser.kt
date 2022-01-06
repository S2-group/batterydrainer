package nl.vu.cs.s2group.batterybomber.stressers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import timber.log.Timber

abstract class Stresser (private val context: Context) {
    protected val handlerUI = Handler(Looper.getMainLooper())
    var isRunning = false

    /**
     * This function exists so that each Stresser does not get optimized out
     */
    protected fun impossibleUIUpdateOnMain(impossibleCondition : Boolean) {
        if(impossibleCondition) {
            val s = "Impossible result from ${javaClass.name}"
            Timber.d(s)
            handlerUI.post { Toast.makeText(context, s, Toast.LENGTH_LONG).show() }
        }
    }

    open fun start() {
        assert(!isRunning)
        Timber.d("${this.javaClass.name} started")
        isRunning = true
    }
    open fun stop() {
        assert(isRunning)
        Timber.d("${this.javaClass.name} stopped")
        isRunning = false
    }
}
