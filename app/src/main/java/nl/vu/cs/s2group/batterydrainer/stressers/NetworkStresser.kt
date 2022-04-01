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

import android.app.ActivityManager
import android.content.Context
import timber.log.Timber
import java.io.InterruptedIOException
import java.lang.StrictMath.min
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class NetworkStresser(context: Context) : Stresser(context) {
    private lateinit var networkExecutorService : ExecutorService
    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val runnable = object : Runnable {
        private val SERVER_URL = URL("https://www.ivanomalavolta.com/files/garbage.blob")
        //private val SERVER_URL = URL("https://garbage-traffic.netlify.app/garbage.blob")
        //private val SERVER_URL = URL("http://192.168.0.107:8080/garbage.blob")

        override fun run() {
            val memoryClass = activityManager.memoryClass
            val bufferSz = min(memoryClass, 32)*1024*1024// in bytes
            Timber.d("Memory class: $memoryClass MB, Buffer Size: $bufferSz Bytes")

            val dataChunk = ByteArray(bufferSz/2)
            while(!Thread.interrupted()) {
                val con: HttpsURLConnection = SERVER_URL.openConnection() as HttpsURLConnection
                //val con: HttpURLConnection = SERVER_URL.openConnection() as HttpURLConnection

                /*
                //Temporary workaround for proxy
                con.sslSocketFactory = SSLContext.getInstance("TLSv1.2").apply {
                    val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                        override fun checkClientTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                        override fun checkServerTrusted(certs: Array<X509Certificate>, authType: String) = Unit
                    })
                    init(null, trustAllCerts, SecureRandom())
                }.socketFactory
                con.hostnameVerifier = HostnameVerifier { _, _ -> true }
                 */

                con.requestMethod = "GET"
                con.setRequestProperty("cache-control", "no-cache,must-revalidate");
                con.setRequestProperty("accept-encoding", "identity"); //prevent compression on server-side
                con.setRequestProperty("connection", "close")

                try {
                    val status = con.responseCode //execute the request
                    Timber.d("Status: $status")

                    if(status != HttpURLConnection.HTTP_OK) {
                        Timber.e("Unexpected status code in network request. Aborting.")
                        break
                    }

                    val inputStream = con.inputStream.buffered(bufferSz/2)
                    var readsz : Int
                    while(true) { //read the response
                        readsz = inputStream.read(dataChunk, 0, dataChunk.size)
                        if(readsz == -1)
                            break;
                        //Timber.d("Status: $status, Data Chunk[0]: ${dataChunk[0].toInt().toChar()} Data Chunk[${readsz-1}]: ${dataChunk[readsz-1].toInt().toChar()}")

                        /* This if condition is impossible to occur but we keep it to prevent the JVM from
                         * optimizing out the entire loop
                         */
                        impossibleUIUpdateOnMain(dataChunk[0].toInt() xor dataChunk[readsz-1].toInt() == 300)
                    }
                    inputStream.close()
                } catch(ex: InterruptedIOException) {   //expected
                    Timber.i(ex.message)
                    break
                } catch (ex: javax.net.ssl.SSLHandshakeException) {
                    //may happen if the phone has wrong date/time or invalid certificate is presented
                    //may also happen if the user has WiFi connection, but they must sign-in into the network first (e.g. airport)
                    Timber.w(ex)
                    break
                } catch(ex: java.net.ConnectException) {
                    //TODO: notify user about failure while attempting to connect a socket to a remote address and port
                    Timber.w(ex)
                    break
                } catch (ex: java.net.UnknownHostException) {
                    //TODO: notify user about failure: IP address of host could not be determined
                    Timber.w(ex)
                    break
                } catch (ex: ProtocolException) {
                    //Can be thrown sometimes due to the large repetitive download. Simply re-download
                    Timber.w(ex)
                    continue
                }
                con.disconnect()
            }
            Timber.i("Network thread stopped")
        }
    }

    override fun permissionsGranted(): Boolean {
        /* TODO: https://developer.android.com/reference/java/net/HttpURLConnection#handling-network-sign-on
         * When the user enables network stresser but the WiFI network requires that they go through a
         * sign-in page first
         */
        return super.permissionsGranted()
    }

    override fun start() {
        super.start()
        networkExecutorService = Executors.newSingleThreadExecutor()
        networkExecutorService.execute(runnable)
    }

    override fun stop() {
        super.stop()
        networkExecutorService.shutdownNow()
        while(!networkExecutorService.awaitTermination(5, TimeUnit.SECONDS)) {
            /* Wait for termination */
        }
    }
}
