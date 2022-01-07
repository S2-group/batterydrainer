package nl.vu.cs.s2group.batterybomber.stressers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import nl.vu.cs.s2group.batterybomber.Utils
import timber.log.Timber

class LocationStresser(context: Context) : Stresser(context) {
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        Timber.i(locationManager.getProviders(false).joinToString(prefix="Found Location Providers: "))
    }

    private val locationListener = object : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            impossibleUIUpdateOnMain(location.latitude == Utils.Constants.PI_50)
            //Timber.d(location.toString())
        }

        override fun onProviderEnabled(provider: String) = Unit
        override fun onProviderDisabled(provider: String) = Unit
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    }

    override fun permissionsGranted(): Boolean {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            TODO("implement me") //TODO: On Android 12 (API level 31) or higher we must request both FINE and COARSE grained location
        else
            return (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    override fun start() {
        super.start()
        //assert(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
        assert(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION  ) == PackageManager.PERMISSION_GRANTED)
        assert(ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER    , 0, 0.0f, locationListener)
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0.0f, locationListener)
    }

    override fun stop() {
        super.stop()
        locationManager.removeUpdates(locationListener)
    }
}
