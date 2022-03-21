




package com.mesum.findfriends

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationRequest
import android.media.MediaPlayer
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.ktx.Firebase
import com.mesum.findfriends.RegisterFragment.Companion.TAG
import com.mesum.findfriends.databinding.FragmentLocationBinding
import kotlin.concurrent.fixedRateTimer
import androidx.annotation.NonNull

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentSnapshot

import android.R
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Transformations.map
import com.google.android.gms.location.LocationServices
import com.google.common.io.Files.map


private const val LOCATION_PERMISSION_REQUEST = 1

class Location : Fragment() {
    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest : LocationRequest

    private var _binding: FragmentLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var mUserLocation : UserLocation
    private lateinit var mDB : FirebaseFirestore
    private lateinit var mUserLocationClint : FusedLocationProviderClient
    private var mFusedLocationClient: FusedLocationProviderClient? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
         // Inflate the layout for this fragment
        _binding = FragmentLocationBinding.inflate(inflater, container, false)
        return binding.root

    }

    //step 3
    private fun savedUserLocation(){
        if (mUserLocation != null){
            val locationReference = mDB
                .collection("usersLocation")
                .document(FirebaseAuth.getInstance().uid.toString())

            locationReference.set(mUserLocation).addOnCompleteListener{
                if (it.isSuccessful){
                    Log.d(TAG, "${mUserLocation.geo_point!!.latitude}")
                    Log.d(TAG, "${mUserLocation.geo_point!!.longitude}")
                }
            }


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mUserLocationClint = FusedLocationProviderClient(requireContext())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10 )
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { OnMapReadyCallback {
            mMap = it

            // Add a marker in Sydney and move the camera
            val sydney = LatLng(-34.0, 151.0)
            mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
*/
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient( activity as AppCompatActivity);
        mUserLocation = UserLocation()
        mDB = FirebaseFirestore.getInstance()

        getUserDetails()

        }






    //Step 2
    private fun getLAstKnownLocation(){
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            ActivityCompat.requestPermissions(activity as AppCompatActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 10 )

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        mFusedLocationClient?.lastLocation?.addOnCompleteListener(OnCompleteListener<Location?> { task ->
            if (task.isSuccessful) {
                val location: Location? = task.result
                val geoPoint = GeoPoint(location!!.latitude, location.longitude)
                mUserLocation.geo_point = geoPoint
                mUserLocation.timestamp = null
                savedUserLocation()
            }
        })
        }


    //step 1
    private fun getUserDetails() {
            val userRef: DocumentReference = mDB.collection("users").document(FirebaseAuth.getInstance().uid!!)
            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "onComplete: successfully set the user client.")
                   // val user: User? = task.result.toObject(User::class.java)
                    val user = task.result
                    mUserLocation.user = user.toObject(User::class.java)
                    getLAstKnownLocation()
                }

        }
    }


    }


