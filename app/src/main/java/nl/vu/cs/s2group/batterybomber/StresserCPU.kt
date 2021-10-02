package nl.vu.cs.s2group.batterybomber

import android.util.Log
import android.widget.Toast
import java.lang.StringBuilder
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.random.Random

class StresserCPU: Thread() {
    //Declare as public volatile to prevent JVM optimizations
    @Volatile
    var digest : ByteArray? = null

    fun ByteArray.toHex(): String = joinToString(separator = "") { b -> "%02x".format(b) }

    override fun run() {
        val prefix_string = "Hello World: "
        val random_seed =  BigInteger( (1..20)
            .map { _ -> Random.nextInt(0, 10) }
            .map { i -> i.toString() }
            .joinToString("")
        )

        val algorithm = "SHA-512"
        val md = MessageDigest.getInstance(algorithm)
        val lucky_suffix = (1..20).map { "0" }.joinToString("")
        var random_num = random_seed

        /* TODO: should we do some memory recycling so that the GC doesn't kick in that often
         * TODO: and take away from us precious juicy cycles?
         */
        while(!Thread.interrupted()) {
            val input_str = prefix_string + random_num.toString()
            val digest = md.digest(input_str.toByteArray())

            //if case to ensure that code doesn't get optimized out
            if(digest.toHex().endsWith(lucky_suffix)) {
                //TODO: use toast instead of logcat
                //Toast.makeText(view.context, "Lucky $algorithm hit! Input: $input_str. Output: " + digest.toHex(), Toast.LENGTH_LONG).show()
                Log.i(javaClass.name, "Lucky $algorithm hit! Input: $input_str. Output: " + digest.toHex())
            }
            md.reset()
            random_num++
        }
    }
}