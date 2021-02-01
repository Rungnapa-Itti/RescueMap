package com.example.rescuemap

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.text.DecimalFormat
import java.util.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
GoogleMap.OnMarkerClickListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var MyLongitude: Double? = null
    private var Mylatitude: Double? = null
    private lateinit var drawer:DrawerLayout
    private lateinit var toggle:ActionBarDrawerToggle



    //new 25-1-64
//    lateinit var mService: IGoogleAPIService
//    internal lateinit var currentPlace: MyPlaces


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //menu slide
        val toolbar =
            findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

//        drawer = findViewById(R.id.drawer_layout)
//        toggle = ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
//        drawer.addDrawerListener(toggle)
//        toggle.syncState()

        // new 27-1-64 change language on map
        val languageToLoad = "th_TH"
        val locale = Locale(languageToLoad)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config,
                baseContext.resources.displayMetrics)

        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.


        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //new 25-1-64  --- Init service
//        mService = Common.googleApiService

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }
    // for toggle menu slide
//    override fun onBackPressed() {
//        if (drawer.isDrawerOpen(GravityCompat.START)){
//            drawer.closeDrawer(GravityCompat.START)
//        }else{
//            super.onBackPressed()
//        }
//
//    }

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

        setUpMap()

        /* หาระยะห่าง
         map = googleMap

//         Add a marker in Sydney and move the camera
        val myPlace = LatLng(13.8394, 100.5665)
        map.addMarker(MarkerOptions().position(myPlace).title("The prize"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace, 15.0f))

        val myPlace2 = LatLng(13.8633, 100.5892)
        map.addMarker(MarkerOptions().position(myPlace2).title("Teenoi BanBangKhen"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(myPlace2, 15.0f))

        map.getUiSettings().setZoomControlsEnabled(true)
        CalculationByDistance(myPlace, myPlace2)
        */
    }


    private fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val KMnewFormat = DecimalFormat("#.###")
        val km = valueResult
        val MeternewFormat = DecimalFormat("####")
        val kmInDec = KMnewFormat.format(km).toDouble()
        val meter = valueResult * 1000
        val meterInDec = MeternewFormat.format(meter).toInt()
        Log.e("distance: ", "" + kmInDec + "   KM  " + " or " + meterInDec + "   Meter  ")
        return Radius * c
    }


    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)
        val titleStr = getAddress(location)
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)

        //14-01-64
       // getRequest()
    }

    override fun onMarkerClick(p0: Marker?) = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

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

        //new 14-01-64
       // setLatitudeAndLongitude(lat.latitude.toString().toDouble(), lat.longitude.toString().toDouble())



        return list[0].getAddressLine(0)
    }

}