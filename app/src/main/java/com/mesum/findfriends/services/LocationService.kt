package com.mesum.findfriends.services


import android.Manifest.*
import com.google.firebase.auth.FirebaseAuth
import android.app.Notification
import com.google.firebase.firestore.FirebaseFirestore
import com.mesum.findfriends.UserLocation
import android.os.Looper
import com.google.firebase.firestore.GeoPoint
import com.mesum.findfriends.UserClient
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.Service
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.mesum.findfriends.User
import java.lang.NullPointerException
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import androidx.annotation.NonNull
import androidx.core.app.NotificationCompat


import com.google.android.gms.location.LocationServices

import com.google.android.gms.location.FusedLocationProviderClient


class LocationService : Service() {
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        //gets the location of the user
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (Build.VERSION.SDK_INT >= 26) {
            //starts background notification for api level 26 above
            val CHANNEL_ID = "my_channel_01"
            val channel = NotificationChannel(
                CHANNEL_ID,
                "My Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(
                channel
            )
            val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("")
                .setContentText("").build()
            startForeground(1, notification)
        }

    }
    //Runs when the startservice command is called from the Locationservice class
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: called.")
        getLocation()
        return START_NOT_STICKY
    }

    // ---------------------------------- LocationRequest ------------------------------------
    // Create the location request to start receiving updates

        private fun getLocation() {

            // ---------------------------------- LocationRequest ------------------------------------
            // Create the location request to start receiving updates
            val mLocationRequestHighAccuracy = LocationRequest()
            mLocationRequestHighAccuracy.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequestHighAccuracy.interval = UPDATE_INTERVAL
            mLocationRequestHighAccuracy.fastestInterval = FASTEST_INTERVAL


            // new Google API SDK v11 uses getFusedLocationProviderClient(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d(TAG, "getLocation: stopping the location service.")
                stopSelf()
                return
            }
            Log.d(TAG, "getLocation: getting location information.")
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequestHighAccuracy, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        Log.d(TAG, "onLocationResult: got location result.")
                        val location = locationResult.lastLocation
                        if (location != null) {
                            val user = (applicationContext as UserClient).user
                            val geoPoint = GeoPoint(location.latitude, location.longitude)
                            val userLocation = UserLocation(user, geoPoint, null)
                            saveUserLocation(userLocation)
                        }
                    }
                },
                Looper.myLooper()!!
            ) // Looper.myLooper tells this to repeat forever until thread is destroyed
        }

    private fun saveUserLocation(userLocation: UserLocation) {
        try {
            val locationRef = FirebaseFirestore.getInstance()
                .collection("usersLocation")
                .document(FirebaseAuth.getInstance().uid!!)
            locationRef.set(userLocation).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        TAG,
                        """onComplete: 
                            inserted user location into database.
                            latitude: ${userLocation.geo_point!!.latitude}
                            longitude: ${userLocation.geo_point!!.longitude}"""
                    )
                }
            }
        } catch (e: NullPointerException) {
            Log.e(TAG, "saveUserLocation: User instance is null, stopping location service.")
            Log.e(TAG, "saveUserLocation: NullPointerException: " + e.message)
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "LocationService"
        private const val UPDATE_INTERVAL = (4 * 1000 /* 4 secs */).toLong()
        private const val FASTEST_INTERVAL: Long = 2000 /* 2 sec */
    }
}