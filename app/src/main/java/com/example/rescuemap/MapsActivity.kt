package com.example.rescuemap

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Paint
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rescuemap.Common.Common
import com.example.rescuemap.DataServer.DataItem
import com.example.rescuemap.Model.MyPlaces
import com.example.rescuemap.Remote.IGoogleAPIService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.showalert.*
import kotlinx.android.synthetic.main.showalert.view.*
import retrofit2.Call
import retrofit2.Callback
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.fixedRateTimer


open class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
GoogleMap.OnMarkerClickListener , NavigationView.OnNavigationItemSelectedListener {

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    private var MyLongitude: Double? = null
    private var Mylatitude: Double? = null
    private var km: Double? = null
    private lateinit var drawer:DrawerLayout
    private lateinit var toggle:ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    var appSetup = AppSetup()
    lateinit var bundle : Bundle
    var fragment:Fragment? = null
    lateinit var fragmentManager:FragmentManager
    lateinit var fragmentTransaction: FragmentTransaction
    //for search place
    var mSearchText: EditText? = null
    lateinit var mService: IGoogleAPIService
    internal lateinit var currentPlace: MyPlaces

    var state = false
    //var listAlerted : List<String> = listOf()
    var listAlerted = mutableListOf<String>()
    var queue = mutableListOf<String>()
    var currentLoc: String? = null



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

            // --- Init service
            mService = Common.googleApiService

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            //menu slide
            val toolbar =
                    findViewById(R.id.toolbar) as Toolbar?
            setSupportActionBar(toolbar)

            drawer = findViewById(R.id.drawer_layout)
            //change page
            navigationView = findViewById(R.id.nav_view)
            navigationView.setNavigationItemSelectedListener(this)
            toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
            drawer.addDrawerListener(toggle)
            toggle.syncState()


            //get http request
            fixedRateTimer("default", false, 0L, 30000) {
                println("Hello2!")
                getRequest()

            }


        buttonAdd.setOnClickListener {
            val addBut = Intent(this, AddPlaceActivity::class.java)
            startActivity(addBut)

            val currentLoc2 = Intent(this@MapsActivity,AddPlaceActivity::class.java)
            currentLoc2.putExtra("list",currentLoc)
            startActivity(currentLoc2)
        }

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
                var places = mSearchText!!.text.toString()
                nearByPlace(places)
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
        //action of search bar
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

        //return Radius * c
        return kmInDec
    }


    private fun placeMarkerOnMap(location: LatLng) {

        val markerOptions = MarkerOptions().position(location)
        val titleStr = "Test"//getAddress(location)
        markerOptions.title(titleStr)

        map.addMarker(markerOptions)
        setLatitudeAndLongitude(location)
        getAddress(location)

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

        //for search places
      // setLatitudeAndLongitude(lat.latitude.toString().toDouble(), lat.longitude.toString().toDouble())
        currentLoc = list[0].getAddressLine(0)
        return currentLoc
    }
    private fun nearByPlace(typePlace: String) {
        //Clear all marker on Map
        map.clear()
        //build URL request base on lacation
       val url = getUrl(getLatitude()!!, getLongitude()!!, typePlace)
        //val url = getUrl(13.9047,100.6572,"hospital")

        Log.d("URL",url)

        mService.getNearbyPlaces(url)
                .enqueue(object : Callback<MyPlaces> {
                    override fun onFailure(call: Call<MyPlaces>, t: Throwable) {
                        Log.d("PLACE","failure")
                        Toast.makeText(baseContext, "" + t!!.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onResponse(call: Call<MyPlaces>, response: retrofit2.Response<MyPlaces>) {
                        currentPlace = response!!.body()!!
                        Log.d("PLACE","Response")
                        if (response!!.isSuccessful) {

                            Log.d("PLACE","isSuccessful")
                            for (i in 0 until response!!.body()!!.results!!.size) {
                                val markerOptions = MarkerOptions()
                                val googlePlaces = response.body()!!.results!![i]
                                val lat = googlePlaces.geometry!!.location!!.lat
                                val lng = googlePlaces.geometry!!.location!!.lng
                                val placeName = googlePlaces.name
                                val latLng = LatLng(lat, lng)

                                markerOptions.position(latLng)
                                markerOptions.title(placeName)
//                                if (typePlace.equals("hospital"))
//                                  //  markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_hospital))

                                //34.35 เพิ่มเติม

                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

                                markerOptions.snippet(i.toString()) //Assign index for Market

                                //Add marker to map
                                map!!.addMarker(markerOptions)

                            }
                            //move camera
                            map!!.moveCamera(CameraUpdateFactory.newLatLng(LatLng(Mylatitude!!,MyLongitude!!)))
                            map!!.animateCamera(CameraUpdateFactory.zoomTo(11f))

                        }
                    }


                })


    }
    private fun getUrl(latitude: Double,longitude: Double,typePlace: String):String{


        val googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
        googlePlaceUrl.append("?location=${latitude},${longitude}")
        googlePlaceUrl.append("&radius=10000")//10km -> 10000
        googlePlaceUrl.append("&type=${typePlace}&key=AIzaSyCFV5FI2cHCpCrOAtjYXC_X72kS7T_8nSQ")
        //googlePlaceUrl.append("&key=AIzaSyCFV5FI2cHCpCrOAtjYXC_X72kS7T_8nSQ")

        Log.d("URL_DEBUG",googlePlaceUrl.toString())
        return googlePlaceUrl.toString()


    }


    private fun setLatitudeAndLongitude(lat: LatLng) {
        Mylatitude = lat.latitude.toDouble()
        MyLongitude = lat.longitude.toDouble()

    }

    private fun getKm():Double?{
        return km
    }
    private fun getLatitude(): Double? {

        return Mylatitude
    }

    //new 14-01-64
    private fun getLongitude(): Double? {

        return MyLongitude
    }


    private fun getRequest() {
        val queue = Volley.newRequestQueue(this)
        val url = "http://10.0.2.2:8081/messages"


        val request = StringRequest(Request.Method.GET, url, Response.Listener { response ->
            print("Http request GET: "+response)

                val collectionType:Type = object : TypeToken<List<DataItem?>?>() {}.getType()
                val data: List<DataItem> = Gson()
                        .fromJson(response, collectionType) as List<DataItem>
            checkItemJson(data)
            Log.d("json",data.toString())







        }, Response.ErrorListener { error -> println("GET error $error") })
        queue.add(request)

    }



    fun recreateAcitivity(item: MenuItem) {
        mSearchText!!.setText("")
        appSetup.refreshApp(this);

    }
    fun checkItemJson(data:List<DataItem>?){
        val geocoder = Geocoder(this)


        for (item in data!!){
            Log.d("TEST","${item.latitude} ${item.longitude}")
            var content = "${item.latitude} ${item.longitude} ${item.topic}"


            try {
                val kmInDec = calculate(item.latitude.toDouble(),item.longitude.toDouble())

                if (kmInDec <= 1 ) {
                        //alert isn't open and Latitude Longitude not null and this location never open alert
                    if (state == false && getLatitude() != null && getLongitude() != null && listAlerted.contains(content) == false) {
                        Log.d("You stay around radius","${state} ${getLatitude()} ${getLongitude()}")
                        //  do alert Success
                        listAlerted.add(content)
                        Log.d("ListAlerted", listAlerted.toString())
                        val list = geocoder.getFromLocation(item.latitude.toDouble(), item.longitude.toDouble(), 1)
                        getAlert(item.topic,item.comment,list[0].getAddressLine(0).toString(),item.latitude,item.longitude)
                    }
                    // alert is open and Latitude Longitude not null and this location do alert Success and this location isn't in queue
//                   if (state == true && getLatitude() != null && getLongitude() != null && listAlerted.contains(content) == false && queue.contains(item.toString()) == false){
//
//                        queue.add(item.toString())
//                        Log.d("Queue","${state} ${queue.toString()}")
//
//                    }
                }

            }
            catch (e:java.lang.Exception){
                Log.e("Err CheckItemJson",e.message.toString())
            }


        }

    }

    private fun calculate(latitude: Double , longitude: Double) : Double{

     try {
        val myCurrent = LatLng(getLatitude()!!.toDouble(), getLongitude()!!.toDouble())
         val locationItem = LatLng(latitude, longitude)
         return CalculationByDistance(myCurrent,locationItem)
     }catch (e:Exception){
         Log.d("ERR",e.message.toString())
     }
       return 0.0
    }

    private fun getAlert(topic: String, comment: String, address: String, latitude: String, longitude: String) {
        Log.d("GET Alert","ok")
        state = true

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.showalert,null)
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle(topic)

        mDialogView.Comment.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);

        mDialogView.TextAlertDetail.setText("${comment}\n${address}\n\n${latitude} ${longitude} ")
        mDialogView.TextAlertComment.setText("\"Mohandas Karamchand Gandhi (/ˈɡɑːndi, ˈɡændi/;[2] Hindustani: [ˈmoːɦəndaːs ˈkərəmtʃənd ˈɡaːndʱi] (About this soundlisten);\n" +
                "         2 October 1869 – 30 January 1948) was an Indian activist who was the leader of the Indian independence movement against British colonial rule.[3]\n" +
                "         Employing nonviolent civil disobedience, Gandhi led India to independence and inspired movements for civil rights and freedom across the world.\n" +
                "         The honorific Mahātmā (Sanskrit: \\\"high-souled\\\", \\\"venerable\\\")[4] was applied to him first in 1914 in South Africa[5] and is now used worldwide.\n" +
                "         In India, he was also called Bapu, a term that he preferred[6] (Gujarati: endearment for father,[7] papa[7][8]), and Gandhi ji, and is known as the Father of the\n" +
                "         Nation.[9][10]\\n\" + \"\\n\" + \"Born and raised in a Hindu family in coastal Gujarat, western India, and trained in law at the Inner Temple, London, Gandhi first\n" +
                "         employed nonviolent civil disobedience as an expatriate lawyer in South Africa, in the resident Indian community's struggle for civil rights. After his return\n" +
                "         to India in 1915, he set about organising peasants,  farmers, and urban labourers to protest against excessive land-tax and discrimination.\n" +
                "         Assuming leadership of the Indian National Congress in 1921, Gandhi led nationwide campaigns for various social causes and for achieving Swaraj or\n" +
                "         self-rule.[11]\\n\" + \"\\n\" + \"Gandhi led Indians in challenging the British-imposed salt tax with the 400 km (250 mi) Dandi Salt March in 1930, and\n" +
                "         later in calling for the British to Quit India in 1942. He was imprisoned for many years, upon many occasions, in both South Africa and India.\n" +
                "         He lived modestly in a self-sufficient residential community and wore the traditional Indian dhoti\n" +
                "o assassinated Gandhi on 30 January 1948 by firing three bullets into his chest.[15\""

        )

        mDialogView.TextAlertComment.setMovementMethod(ScrollingMovementMethod());


        val mAlertDialog = mBuilder.show()

        mDialogView.button1.setOnClickListener{
            state = false
            map.addMarker(MarkerOptions().position(LatLng(latitude.toDouble(),longitude.toDouble())).title(address)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            mAlertDialog.dismiss()
        }






    }







}