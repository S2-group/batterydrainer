/*
 * MIT License
 *
 * Copyright (c) 2022 Software and Sustainability Group - VU
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package nl.vu.cs.s2group.batterydrainer.stressers

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import timber.log.Timber

abstract class Stresser (protected val context: Context) {
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

    open fun permissionsGranted(): Boolean {
        return true
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
