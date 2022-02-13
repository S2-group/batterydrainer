package nl.vu.cs.s2group.batterybomber.stressers

import android.app.ActivityManager
import android.content.Context
import timber.log.Timber
import java.io.InterruptedIOException
import java.lang.StrictMath.min
import java.net.HttpURLConnection
import java.net.ProtocolException
import java.net.URL
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

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
                } catch(ex: InterruptedIOException) {
                    break
                } catch (ex: ProtocolException) {
                    //Can be thrown sometimes due to the large repetitive download. Simply re-download
                    break
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
