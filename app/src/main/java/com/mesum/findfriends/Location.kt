
package com.mesum.findfriends

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.mesum.findfriends.RegisterFragment.Companion.TAG
import com.mesum.findfriends.databinding.FragmentLocationBinding
import com.google.android.gms.tasks.OnCompleteListener
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import android.app.ActivityManager
import android.content.Context

import android.content.Context.ACTIVITY_SERVICE

import androidx.core.content.ContextCompat.getSystemService

import com.mesum.findfriends.services.LocationService

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.core.Bound
import com.google.firebase.firestore.ktx.toObjects
import java.lang.IllegalStateException
import java.lang.NullPointerException
import java.sql.Timestamp


class Location : Fragment() {
    private lateinit var mMap: GoogleMap
    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var mUserLocation : UserLocation
    private lateinit var mDB : FirebaseFirestore
    private lateinit var mUserLocationClint : FusedLocationProviderClient
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private val userlocationlist = mutableListOf<User>()
    private val viewModel by activityViewModels<LocationViewModel>()
    private val locationUpdateInterval = 3000;
    private val mHandler = Handler()
    private  var mRunnable = Runnable {  }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         // Inflate the layout for this fragment
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Current user location client
        mUserLocationClint = FusedLocationProviderClient(requireContext())
        //If permission are not granted request permission
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.FOREGROUND_SERVICE), 10 )
        }
        //Location client provided to this class
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( activity as AppCompatActivity);
        //Current user data class
        mUserLocation = UserLocation()
        //Initialize fireStore database
        mDB = Firebase.firestore
        val firebaseAuth = FirebaseAuth.getInstance()
        binding.currentuser.text = firebaseAuth.currentUser?.email.toString()
        getUserDetails()
        // getallUser()

        //Observe live data with users location
        viewModel.getResponseUserLocationFlow().observe(this, {
        })

        getallUser()
    }


    private fun startUserLocationsRunnable() {
        Log.d(
            TAG,
            "startUserLocationsRunnable: starting runnable for retrieving updated locations."
        )
         mRunnable = object : Runnable{
             override fun run() {
                // retrieveUserLocations()
                 mHandler.postDelayed(mRunnable,locationUpdateInterval.toLong() )
             }
         }
        mHandler.postDelayed( mRunnable,  locationUpdateInterval.toLong())

    }




    //step 1
    fun getUserDetails() {
        mUserLocation = UserLocation()
        val userRef: DocumentReference = mDB.collection("user")
            .document(FirebaseAuth.getInstance().uid!!)
        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "onComplete: successfully set the user client.")
                val user: User = task.result.toObject(User::class.java)!!
                mUserLocation.user = user
                (activity?.application as UserClient).user = user
                getLAstKnownLocation()
            }
        }
    }
    //Step 2
    private fun getLAstKnownLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10 )
        }
        mFusedLocationClient?.lastLocation?.addOnCompleteListener(OnCompleteListener<Location?> { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                val geoPoint = GeoPoint(location!!.latitude, location.longitude)
                mUserLocation.geo_point = geoPoint
                mUserLocation.timestamp = null
                savedUserLocation()
                startLocationService()
            }
        })
    }

    //step 3
    private fun savedUserLocation(){
        if (mUserLocation != null){
            val locationReference = mDB
                .collection("usersLocation")
                //Gets current signed in users id
                .document(FirebaseAuth.getInstance().uid.toString())
            locationReference.set(mUserLocation).addOnCompleteListener{
                if (it.isSuccessful){
                    Log.d(TAG, "${mUserLocation.geo_point!!.latitude}")
                    Log.d(TAG, "${mUserLocation.geo_point!!.longitude}")
                }
            }
        }
    }


    //Method without corotines and uses callback
    private fun getallUser() {
        val userReference = mDB.collection("usersLocation")
        userReference
            .addSnapshotListener(object : EventListener<QuerySnapshot?> {
                override fun onEvent(
                    queryDocumentSnapshots: QuerySnapshot?,
                    e: FirebaseFirestoreException?
                ) {
                    if (e != null) {
                        Log.e(TAG, "onEvent: Listen failed.", e)
                        return
                    }
                    if (queryDocumentSnapshots != null) {

                        // Clear the list and add all the users again
                        for (doc in queryDocumentSnapshots) {
                            val user = doc.toObject(
                                UserLocation::class.java
                            )


                        }
                        val currentUser = FirebaseAuth.getInstance().currentUser?.email
                            //gets the updated time for the current user
                            queryDocumentSnapshots.toObjects(UserLocation::class.java).filter { it.user?.email.equals(currentUser) }.forEach { binding.usertime.text = it.timestamp.toString() }

                        val mapFragment = childFragmentManager
                            .findFragmentById(R.id.map) as SupportMapFragment
                        mapFragment.getMapAsync(object  : OnMapReadyCallback{
                            override fun onMapReady(p0: GoogleMap) {
                                mMap = p0
                                mMap.clear()

                                for (i in queryDocumentSnapshots.toObjects(UserLocation::class.java)){
                                    val userlc = LatLng(i.geo_point?.latitude!!, i.geo_point?.longitude!!)
                                    val zoomLevel = 15f
                                    mMap.addMarker(MarkerOptions().position(userlc).title(i.user?.username))
                                        ?.setIcon(getCustomMarkerFromBitmap(R.drawable.peronsimage))
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlc, zoomLevel))

                                }
                            }
                        })

                        Log.d(TAG, "onEvent: user list size: " + userlocationlist.size)
                    }
                }
            })

    }


    private fun startLocationService() {
        if (!isLocationServiceRunning()) {
            val serviceIntent = Intent(activity as AppCompatActivity, LocationService::class.java)
            //        this.startService(serviceIntent);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context?.startForegroundService(serviceIntent)
            } else {

                activity?.startService(serviceIntent)
            }
        }
    }

    private fun isLocationServiceRunning(): Boolean {
        val manager = activity?.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
            if ("com.codingwithmitch.googledirectionstest.services.LocationService" == service.service.className) {
                Log.d(TAG, "isLocationServiceRunning: location service is already running.")
                return true
            }
        }
        Log.d(TAG, "isLocationServiceRunning: location service is not running.")
        return false
    }

    private fun getCustomMarkerFromBitmap(vectorId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context!!, vectorId)
        vectorDrawable!!.setBounds(0, 0 , vectorDrawable!!.intrinsicWidth, vectorDrawable.intrinsicWidth)
        val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)


    }


}


