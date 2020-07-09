package com.rasyidabdulhalim.aquaza.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import com.rasyidabdulhalim.aquaza.R
import com.rasyidabdulhalim.aquaza.commoners.K
import com.rasyidabdulhalim.aquaza.helpers.FirebaseEventListenerHelper
import com.rasyidabdulhalim.aquaza.helpers.GoogleMapHelper
import com.rasyidabdulhalim.aquaza.helpers.MarkerAnimationHelper
import com.rasyidabdulhalim.aquaza.helpers.UiHelper
import com.rasyidabdulhalim.aquaza.interfaces.FirebaseDriverListener
import com.rasyidabdulhalim.aquaza.interfaces.IPositiveNegativeListener
import com.rasyidabdulhalim.aquaza.interfaces.LatLngInterpolator
import com.rasyidabdulhalim.aquaza.models.Driver
import com.rasyidabdulhalim.aquaza.models.Order
import com.rasyidabdulhalim.aquaza.utils.MarkerCollection
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper
import com.rasyidabdulhalim.aquaza.utils.PreferenceHelper.get
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity(), FirebaseDriverListener {

    companion object {
        private const val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 6161
        private const val ONLINE_DRIVERS = "online_drivers"
    }

    private lateinit var googleMap: GoogleMap
    private lateinit var locationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var locationFlag = true
    private lateinit var valueEventListener: FirebaseEventListenerHelper
    private val uiHelper = UiHelper()
    private val googleMapHelper = GoogleMapHelper()
    private val markerAnimationHelper = MarkerAnimationHelper()
    private lateinit var order: Order
    private lateinit var prefs: SharedPreferences

    private val databaseReference = FirebaseDatabase.getInstance().reference.child(
        ONLINE_DRIVERS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceHelper.defaultPrefs(this)
        order = intent.getSerializableExtra(K.ORDER) as Order
        setContentView(R.layout.activity_map)
        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(
            R.id.supportMap
        ) as SupportMapFragment
        mapFragment.getMapAsync { googleMap = it }
        createLocationCallback()
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = uiHelper.getLocationRequest()
        if (!uiHelper.isPlayServicesAvailable(this)) {
            Toast.makeText(this, "Play Services did not installed!", Toast.LENGTH_SHORT).show()
            finish()
        } else requestLocationUpdate()
        valueEventListener = FirebaseEventListenerHelper(this)
        databaseReference.addChildEventListener(valueEventListener)

        if (prefs[K.STATUS,""]==K.USER){
            totalOnlineDrivers.setVisibility(View.GONE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        if (!uiHelper.isHaveLocationPermission(this)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            return
        }
        if (uiHelper.isLocationProviderEnabled(this))
            uiHelper.showPositiveDialogWithListener(this, resources.getString(R.string.need_location), resources.getString(
                R.string.location_content
            ), object : IPositiveNegativeListener {
                override fun onPositive() {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            }, "Turn On", false)
        locationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult!!.lastLocation == null) return
                val latLng = LatLng(locationResult.lastLocation.latitude, locationResult.lastLocation.longitude)
                Log.e("Location", latLng.latitude.toString() + " , " + latLng.longitude)
                if (locationFlag) {
                    locationFlag = false
                }
            }
        }
    }

    private fun animateCamera(latLng: LatLng) {
        val cameraUpdate = googleMapHelper.buildCameraUpdate(latLng)
        googleMap.animateCamera(cameraUpdate, 10, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            val value = grantResults[0]
            if (value == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show()
                finish()
            } else if (value == PackageManager.PERMISSION_GRANTED) requestLocationUpdate()
        }
    }

    override fun onDriverAdded(driver: Driver) {
        if (prefs[K.STATUS,""]==K.OWNER){
                addMarker(driver)
        }else if (prefs[K.STATUS,""]==K.DRIVER){

        }else{//for user
            if (driver.driverId==order.driverId){
                addMarker(driver)
            }else{
                Toast.makeText(this, "No Driver", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDriverRemoved(driver: Driver) {
        if (prefs[K.STATUS,""]==K.OWNER){
             removeMarker(driver)
        }else if (prefs[K.STATUS,""]=="Driver"){

        }else {//for user
            if (driver.driverId == order.driverId) {
             removeMarker(driver)
            }
        }
    }

    override fun onDriverUpdated(driver: Driver) {
        if (prefs[K.STATUS,""]==K.OWNER){
              updateMarker(driver)
        }else if (prefs[K.STATUS,""]=="Driver"){

        }else {//for user
            if (driver.driverId == order.driverId) {
                updateMarker(driver)
            }
        }
    }
    private fun addMarker(driver: Driver){
        val markerOptions = googleMapHelper.getDriverMarkerOptions(LatLng(driver.lat!!, driver.lng!!))
        val marker = googleMap.addMarker(markerOptions)
        marker.tag = driver.driverId
        MarkerCollection.insertMarker(marker)
        totalOnlineDrivers.text = resources.getString(R.string.total_online_drivers).plus(" ").plus(MarkerCollection.allMarkers().size)
        val latLng = LatLng(driver.lat!!, driver.lng!!)
        animateCamera(latLng)
    }
    private fun updateMarker(driver: Driver){
        val marker = MarkerCollection.getMarker(driverId = driver.driverId!!)
        markerAnimationHelper.animateMarkerToGB(
            marker!!,
            LatLng(driver.lat!!, driver.lng!!),
            LatLngInterpolator.Spherical()
        )
    }
    private fun removeMarker(driver: Driver){
        MarkerCollection.removeMarker(driver.driverId!!)
        totalOnlineDrivers.text =
            resources.getString(R.string.total_online_drivers).plus(" ").plus(
                MarkerCollection.allMarkers().size
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        databaseReference.removeEventListener(valueEventListener)
        locationProviderClient.removeLocationUpdates(locationCallback)
        MarkerCollection.clearMarkers()
    }
}
