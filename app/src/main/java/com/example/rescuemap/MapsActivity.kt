package com.example.rescuemap

import android.Manifest
import android.app.PendingIntent.getActivity
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import java.io.IOException
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.ArrayList


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
GoogleMap.OnMarkerClickListener , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var MyLongitude: Double? = null
    private var Mylatitude: Double? = null
    private lateinit var drawer:DrawerLayout
    private lateinit var toggle:ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    var appSetup = AppSetup()
    lateinit var bundle : Bundle
    var fragment:Fragment? = null
    lateinit var fragmentManager:FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    var mSearchText: EditText? = null





    //new 25-1-64
//    lateinit var mService: IGoogleAPIService
//    internal lateinit var currentPlace: MyPlaces


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

        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        mSearchText = findViewById(R.id.input_search)
        init()

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //new 25-1-64  --- Init service
//        mService = Common.googleApiService

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //menu slide
        val toolbar =
            findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)
        //change page
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        toggle = ActionBarDrawerToggle(this,drawer,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()





    }
    // for toggle menu slide
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var id = item.itemId
        if (id == R.id.nav_map){
            fragment = MapFragment()

        }else if (id == R.id.nav_place){
            fragment = SearchPlacesFragment()
        }

        if (fragment != null){
            fragmentManager = supportFragmentManager
            fragmentTransaction = fragmentManager.beginTransaction()

            fragmentTransaction.replace(R.id.fragment_container , fragment!!)

            fragmentTransaction.commit()
        }
//        when(item.itemId) {
//
//            R.id.nav_map -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, MapFragment()).commit()
//            R.id.nav_place -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, SearchPlacesFragment()).commit()
//       }
      drawer.closeDrawer(GravityCompat.START)
      return true;
        //return super.onOptionsItemSelected(item)



    }
    // for search
    private fun init() {

        mSearchText!!.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH || event.action == KeyEvent.ACTION_DOWN || event.action == KeyEvent.KEYCODE_ENTER ){
                //geoLocate()
                Log.d("TAG","Text : "+mSearchText!!.text.toString())
            }
            return@setOnEditorActionListener false
        }
    }
//    private fun geoLocate(){
//        var geocoder : Geocoder
//        var searchString = mSearchText!!.text.toString()
//         geocoder = Geocoder(MapsActivity.this)
//        var list = ArrayList<Address>()
//        try{
//            list = geocoder.getFromLocationName(searchString,1) as ArrayList<Address>
//        }catch (e:IOException){
//            Log.e("TAG","geoLacate: "+e.message)
//        }
//        if(list.size > 0){
//            var address:Address
//            address = list.get(0)
//
//            Log.d("TAG","geoLocate: found a location "+address.toString())
//           // Toast.makeText(this,address.toString(),Toast.LENGTH_SHORT).show()
//        }
//
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
        init()



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

        //new 14-01-64
       // setLatitudeAndLongitude(lat.latitude.toString().toDouble(), lat.longitude.toString().toDouble())



        return list[0].getAddressLine(0)
    }

    fun recreateAcitivity(item: MenuItem) {
        appSetup.refreshApp(this);

    }


}