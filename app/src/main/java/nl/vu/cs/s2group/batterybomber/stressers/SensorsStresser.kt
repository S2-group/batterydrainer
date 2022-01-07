package nl.vu.cs.s2group.batterybomber.stressers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.add
import androidx.fragment.app.commit
import nl.vu.cs.s2group.batterybomber.*
import timber.log.Timber

class SensorsStresser(context: Context) : Stresser(context) {
    private val sensorManager : SensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val sensorsListener = object: SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            impossibleUIUpdateOnMain(event.values[0].toDouble() == Utils.Constants.PI_50)
            //Timber.d("onSensorChanged: ${event.sensor.name} ${event.values[0]}")
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            Timber.d("onAccuracyChanged: ${sensor.name}. New accuracy: $accuracy")
        }
    }

    override fun permissionsGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (ContextCompat.checkSelfPermission(context, Manifest.permission.HIGH_SAMPLING_RATE_SENSORS) == PackageManager.PERMISSION_GRANTED)
        } else {
            super.permissionsGranted()
        }
    }

    override fun start() {
        super.start()
        sensorManager.getSensorList(Sensor.TYPE_ALL).forEach { sensor ->
            Timber.d("Stressing sensor: ${sensor.name}")
            sensorManager.registerListener(sensorsListener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun stop() {
        super.stop()
        sensorManager.unregisterListener(sensorsListener)
    }

}