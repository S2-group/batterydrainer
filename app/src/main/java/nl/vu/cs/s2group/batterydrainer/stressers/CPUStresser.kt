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
import timber.log.Timber
import java.nio.ByteBuffer
import java.security.MessageDigest
import kotlin.random.Random

class CPUStresser(context: Context) : Stresser(context) {
    private var cpuStressThreads : List<Thread> = listOf()

    private val threadRunnable = Runnable {
        val algorithm = "SHA-512"
        val md = MessageDigest.getInstance(algorithm)
        val luckyDigest : ByteArray = ByteBuffer.allocate(64).also { bb -> (1..32).forEach{ bb.put(0x1A); bb.put(0x2B) } }.array() //1A2B1A2B..2B - 64 bytes = 512 bits

        val buffer: ByteBuffer = ByteBuffer.allocate(Long.SIZE_BYTES)
        var randomNum = Random.nextLong()
        var digest: ByteArray

        while(!Thread.interrupted()) {
            buffer.putLong(0, randomNum++)
            digest = md.digest(buffer.array())
            impossibleUIUpdateOnMain(digest.contentEquals(luckyDigest))
        }
    }

    override fun start() {
        super.start()
        val cores = Runtime.getRuntime().availableProcessors()
        Timber.d("Cores available: $cores. Spawning $cores CPU stresser threads")

        cpuStressThreads = (1..cores).map { Thread(threadRunnable) }.toList()
        cpuStressThreads.forEach{ it.start() }
    }

    override fun stop() {
        super.stop()
        cpuStressThreads.forEach {  it.interrupt() }
    }
}
