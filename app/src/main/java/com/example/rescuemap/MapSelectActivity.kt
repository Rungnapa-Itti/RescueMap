package com.example.rescuemap

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_map_select.*
import kotlinx.android.synthetic.main.activity_maps.*

import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


class MapSelectActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener {

    var currentMarker:Marker? = null
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var MyLongitude: Double? = null
    private var Mylatitude: Double? = null
    private var km: Double? = null

    var currentLoc: String? = null
    var selectedLoc: String? = null



    override  fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // new 27-1-64 change language on map
        val languageToLoad = "th_TH"
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config,
            baseContext.resources.displayMetrics)


        setContentView(R.layout.activity_map_select)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //Start of dynamic title code---------------------
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
//            val cal: Calendar = Calendar.getInstance()
            val dynamicTitle: String = "Map"
            val colorDrawable = ColorDrawable(Color.parseColor("#db5a6b"))
            //Setting a dynamic title at runtime. Here, it displays the current time.
            actionBar.setTitle(dynamicTitle)
            actionBar.setBackgroundDrawable(colorDrawable);
            actionBar.setDisplayHomeAsUpEnabled(true)

        }
        //End of dynamic title code----------------------

        buttonConfirm.setOnClickListener {
            val confirmBut = Intent(this@MapSelectActivity,AddPlaceActivity::class.java)
            confirmBut.putExtra("newLat",getLatitude().toString())
            confirmBut.putExtra("newLng",getLongitude().toString())
            confirmBut.putExtra("selectedLoc",selectedLoc)
            startActivity(confirmBut)
        }

    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {


        map = googleMap
        // Add a marker in Sydney and move the camera
//        val myPlace = LatLng(40.73, -73.99)
//        map.addMarker(MarkerOptions().position(myPlace).title("My place"))
//        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15.0f)

        map.uiSettings.isZoomControlsEnabled = true
        map.setOnMarkerClickListener(this)
        map.setOnMarkerDragListener(this)

        setUpMap()
        //action of search bar

    }



    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        markerOptions.title(titleStr).draggable(true)

        currentMarker = map.addMarker(markerOptions)
        currentMarker?.showInfoWindow()
    }

    override fun onMarkerClick(p0: Marker?) = false

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1

    }

    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        // 1
        map.isMyLocationEnabled = true

        // 2
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            // Got last known location. In some rare situations this can be null.
            // 3
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                placeMarkerOnMap(currentLatLng)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f))
            }
        }


    }



    private fun getAddress(lat: LatLng): String? {


        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat.latitude, lat.longitude, 1)
        // Log.e("lat", lat.latitude.toString())

        //for search places
        // setLatitudeAndLongitude(lat.latitude.toString().toDouble(), lat.longitude.toString().toDouble())
        currentLoc = list[0].getAddressLine(0)
        return currentLoc
    }


    private fun setLatitudeAndLongitude(lat: LatLng) {
        Mylatitude = lat.latitude.toDouble()
        MyLongitude = lat.longitude.toDouble()

    }

    private fun getLatitude(): Double? {

        return Mylatitude
    }

    //new 14-01-64
    private fun getLongitude(): Double? {

        return MyLongitude
    }


    override fun onMarkerDrag(marker: Marker) {
//        tvLocInfo.setText("Marker " + marker.id.toString() + " Drag@" + marker.position)
//        Log.e("marker","Marker " + marker.id.toString() + " Drag@" + marker.position)
    }

    override fun onMarkerDragEnd(marker: Marker) {
//        tvLocInfo.setText("Marker " + marker.id.toString() + " DragEnd")
//        Log.e("markerEnd","Marker " + marker.id.toString() + " DragEnd")
        if(currentMarker != null){
            currentMarker?.remove()
        }

        val markerOptions = MarkerOptions().position(marker.position)
        val titleStr = getAddress(marker.position)
        markerOptions.title(titleStr)
        placeMarkerOnMap(marker.position)
        setLatitudeAndLongitude(marker.position)
        Log.d("markerEnd","Marker " + marker.id.toString() + " Drag@" + marker.position)
        Log.d("address",titleStr.toString())
        selectedLoc = titleStr.toString()
    }

    override fun onMarkerDragStart(marker: Marker) {
//        tvLocInfo.setText("Marker " + marker.id.toString() + " DragStart")
        Log.d("markerStart","Marker DragStart")
    }


}