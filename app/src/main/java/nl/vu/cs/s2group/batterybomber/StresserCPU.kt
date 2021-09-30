package nl.vu.cs.s2group.batterybomber

import android.util.Log
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

        val md = MessageDigest.getInstance("SHA-512")
        var random_num = random_seed
        while(!Thread.interrupted()) {
            digest = md.digest((prefix_string + random_num.toString()).toByteArray())
            //Log.d(javaClass.name, "Hashed Value: " + digest.toHex())
            md.reset()
            random_num++
        }
    }
}