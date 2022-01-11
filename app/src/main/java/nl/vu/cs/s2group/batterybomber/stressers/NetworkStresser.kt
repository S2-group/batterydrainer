package nl.vu.cs.s2group.batterybomber.stressers

import android.content.Context
import timber.log.Timber
import java.io.BufferedInputStream
import java.io.InterruptedIOException
import java.net.ProtocolException
import java.net.URL
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class NetworkStresser(context: Context) : Stresser(context) {
    private lateinit var networkExecutorService : ExecutorService

    private val runnable = object : Runnable {
        private val SERVER_URL = URL("https://garbage-traffic.netlify.app/garbage.blob")
        private val dataChunk = ByteArray(32 * 1024 * 1024) //32 MB buffer
        //private val SERVER_URL = URL("http://192.168.0.107:8080/garbage.blob")

        override fun run() {
            while(!Thread.interrupted()) {
                val con: HttpsURLConnection = SERVER_URL.openConnection() as HttpsURLConnection
                //val con: HttpURLConnection = SERVER_URL.openConnection() as HttpURLConnection

                con.requestMethod = "GET"
                con.setRequestProperty("cache-control", "no-cache,must-revalidate");
                con.setRequestProperty("accept-encoding", "identity"); //prevent compression on server-side
                con.setRequestProperty("connection", "close")

                try {
                    val status = con.responseCode //execute the request
                    val inputStream = BufferedInputStream(con.inputStream)
                    Timber.d("Status: $status")

                    if(status != 200) {
                        Timber.e("Unexpected status code in network request. Aborting.")
                        break
                    }
                    while(inputStream.read(dataChunk) != -1) { //read the response
                        //Timber.d("Status: $status, Data Chunk[0]: ${dataChunk[0].toInt().toChar()}")

                        /* This if condition is impossible to occur but we keep it to prevent the JVM from
                         * optimizing out the entire loop
                         */
                        impossibleUIUpdateOnMain(dataChunk[0].toInt() xor dataChunk.last().toInt() == 300)
                    }

                    inputStream.close()
                } catch(ex: InterruptedIOException) {
                    break
                } catch (ex: ProtocolException) {
                    //Can be thrown sometimes due to the large repetitive download. Simply re-download
                    continue
                } finally {
                    con.disconnect()
                }
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
