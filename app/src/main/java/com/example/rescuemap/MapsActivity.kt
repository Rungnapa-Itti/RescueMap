package com.example.rescuemap

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Paint
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
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
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rescuemap.Common.Common
import com.example.rescuemap.DataServer.DataItem
import com.example.rescuemap.Model.MyPlaces
import com.example.rescuemap.Remote.IGoogleAPIService
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.HttpMethod
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.showalert.view.*
import okhttp3.OkHttpClient
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.fixedRateTimer


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
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
    private var markerLat: Double? = null
    private var markerLng: Double? = null
    var stateClick = false
    var stateSelectDropdown = false
    var personName: String? = null
    val personGivenName: String? = null
    val personFamilyName: String? = null
    val personEmail: String? = null
    val personId: String? = null
    val personPhoto: Uri? = null

    var userName = ""
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

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
        personName = intent.getStringExtra("googleUsername1").toString()
        Log.w("userMainPage",personName.toString())

        setUsername(personName.toString())

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<View>(R.id.navAccountName) as TextView
        navUsername.text = personName

        buttonAdd.setOnClickListener {
            val addBut = Intent(this@MapsActivity,AddPlaceActivity::class.java)
            addBut.putExtra("list",currentLoc)
            addBut.putExtra("myLat",getLatitude().toString())
            addBut.putExtra("myLng",getLongitude().toString())
            addBut.putExtra("googleUsername2",personName)
            startActivity(addBut)
        }

//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestIdToken(getString(R.string.default_web_client_id))
//            .requestEmail()
//            .build()
//        mGoogleSignInClient= GoogleSignIn.getClient(this,gso)
//// pass the same server client ID used while implementing the LogIn feature earlier.
//        logout.setOnClickListener {
//            mGoogleSignInClient.signOut().addOnCompleteListener {
//                val intent= Intent(this, GoogleLogInLogOut::class.java)
//                startActivity(intent)
//                finish()
//            }
//        }


    }

    // for toggle menu slide
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

    //get data for show path by location
    fun getDirectionURL(origin:LatLng,dest:LatLng) : String{
        Log.d("Path GetDirection","https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyCFV5FI2cHCpCrOAtjYXC_X72kS7T_8nSQ")
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyCFV5FI2cHCpCrOAtjYXC_X72kS7T_8nSQ"
    }

    //get data for show path by location
    private inner class GetDirection(val url : String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap" , " data : $data")
            val result =  ArrayList<List<LatLng>>()
            try{
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path =  ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }
        //get data for show path by location
    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }


        //get data for show path by location
        override fun onPostExecute(result: List<List<LatLng>>?) {
            val lineOption = PolylineOptions()
            for (i in result!!.indices){
                lineOption.addAll(result[i])
                lineOption.width(10f)
                lineOption.color(Color.RED)
                lineOption.geodesic(true)
            }
            map.addPolyline(lineOption)
        }


    }



    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var id = item.itemId
        if (id == R.id.nav_map){
            fragment = MapFragment()

        }else if (id == R.id.nav_call){
            //fragment = SearchPlacesFragment()
            val buttonCall = Intent(this@MapsActivity,PhoneNumberActivity::class.java)
            startActivity(buttonCall)

        }else if (id == R.id.logout) {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
// pass the same server client ID used while implementing the LogIn feature earlier.
            mGoogleSignInClient.signOut().addOnCompleteListener {
                val intent = Intent(this, GoogleLogInLogOut::class.java)
                startActivity(intent)
                finish()
            }
            if (AccessToken.getCurrentAccessToken() != null) {
                GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/permissions/",
                    null,
                    HttpMethod.DELETE,
                    GraphRequest.Callback {
                        AccessToken.setCurrentAccessToken(null)
                        LoginManager.getInstance().logOut()
                        finish()
                    }).executeAsync()
            }
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

        state = false
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
            val titleStr = getAddress(location)
            markerOptions.title(titleStr)

            map.addMarker(markerOptions)
            setLatitudeAndLongitude(location)
//        currLat = location.latitude
//        currLng = location.longitude
//        val sendCurrLatLng = Intent(this@MapsActivity,AddPlaceActivity::class.java)
//        sendCurrLatLng.putExtra("currLat",currLat.toString())
//        sendCurrLatLng.putExtra("currLng",currLng.toString())
            //getAddress(location)



        //14-01-64
       // getRequest()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d("test click marker", marker.title)
//        getLocationFromAddress(marker.title)
        stateClick = true
        Log.d("test state marker", stateClick.toString())
        markerLat = marker.position.latitude
        markerLng = marker.position.longitude
//        getAlert(item.topic,item.comment,list[0].getAddressLine(0).toString(),item.latitude,item.longitude,item.id,item.userName,item.rating)
        getRequest()
        marker.position.latitude

        if (mSearchText!!.text.isNotEmpty()){
            try {
                val sourceLocation = LatLng(getLatitude()!!.toDouble(), getLongitude()!!.toDouble())
                val destLocation = LatLng(marker.position.latitude,marker.position.longitude)
                val URL = getDirectionURL(sourceLocation,destLocation)
                GetDirection(URL).execute()
            }catch (e:Exception){
                Log.d("Err GetAlert LatLng()",e.message.toString())
            }
        }
        return false
    }

//    private fun getLocationFromAddress(addr: String){
//        val geocoder = Geocoder(this)
//        var addresses = geocoder.getFromLocationName(addr,1)
//        if(addresses.size > 0) {
//            markerLat= addresses.get(0).getLatitude()
//            markerLng= addresses.get(0).getLongitude()
//        }
//    }

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

//

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

    private fun putRequest(id:String,userName:String,topic:String,comment: String,rating:String,latitude: String,longitude: String) {
        val putUrl = "http://10.0.2.2:8081/messages/update/${id}"
        val queue = Volley.newRequestQueue(this)
        val putData = JSONObject()

        try {
            putData.put("id", id)
            putData.put("userName", userName)
            putData.put("topic", topic)
            putData.put("comment", comment)
            putData.put("rating", rating)
            putData.put("latitude", latitude)
            putData.put("longitude", longitude)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val putRequest: JsonObjectRequest =
                object : JsonObjectRequest(
                        Request.Method.PUT, putUrl, putData,
                        Response.Listener { response ->
                            // response
                            Log.d("PUT Http response: ", "$response")
                        },
                        Response.ErrorListener { error ->
                            // error
                            Log.i("PUT error: ", "$error")
                        }
                ) {

                }
        queue.add(putRequest)
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
                val selectedLat= intent.getStringExtra("selectedLat").toString()
                val selectedLng= intent.getStringExtra("selectedLng").toString()

                // Alert from select dropdown in Add place page
                if(item.latitude == selectedLat.toString() && stateSelectDropdown == false){
                    val list = geocoder.getFromLocation(item.latitude.toDouble(), item.longitude.toDouble(), 1)
                    stateSelectDropdown = true
                    getAlert(item.topic,item.comment,list[0].getAddressLine(0).toString(),item.latitude,item.longitude,item.id,item.userName,item.rating)
                }

                // Alert from select marker
                if(item.latitude == markerLat.toString() && stateClick == true){
                    val list = geocoder.getFromLocation(item.latitude.toDouble(), item.longitude.toDouble(), 1)
                    getAlert(item.topic,item.comment,list[0].getAddressLine(0).toString(),item.latitude,item.longitude,item.id,item.userName,item.rating)
                    stateClick = false
                    Log.d("test state marker", stateClick.toString())
                }

                // Default alert
                if (kmInDec <= 1 ) {
                        //alert isn't open and Latitude Longitude not null and this location never open alert
                    if (state == false && getLatitude() != null && getLongitude() != null && listAlerted.contains(content) == false) {
                        Log.d("You stay around radius","${state} ${getLatitude()} ${getLongitude()}")
                        //  do alert Success
                        listAlerted.add(content)
                        Log.d("ListAlerted", listAlerted.toString())
                        val list = geocoder.getFromLocation(item.latitude.toDouble(), item.longitude.toDouble(), 1)
                        getAlert(item.topic,item.comment,list[0].getAddressLine(0).toString(),item.latitude,item.longitude,item.id,item.userName,item.rating)
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

    private fun getAlert(topic: String, comment: String, address: String, latitude: String, longitude: String, id: String, userName: String, rating: String) {
        Log.d("GET Alert","ok")
        state = true

        val mDialogView = LayoutInflater.from(this).inflate(R.layout.showalert,null)
        val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)

        mDialogView.topic.setText(topic)
        mDialogView.topic.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);
        mDialogView.TextComment.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);

      //  Log.d("IndexOf", "${comment.indexOf(',').toString()} ")
        if (comment.indexOf(',') != -1){
            //Log.d("Test indexof" , "${comment.substring(0,comment.indexOf(','))}")
            mDialogView.TextAlertDetail.setText("${comment.substring(0,comment.indexOf(','))}\n${address} ")
        }else if (comment.indexOf(',') == -1){
            mDialogView.TextAlertDetail.setText("${comment}\n${address}\n\n${latitude} ${longitude} ")
        }


        mDialogView.TextAlertComment.setText(splitComment(comment))
        var countRating = findUserInRating(rating)
        mDialogView.rating.setText("มีผู้ยืนยันเหตุการณ์ทั้งหมด : ${countRating} คน")
        mDialogView.rating.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);



        mDialogView.TextAlertComment.setMovementMethod(ScrollingMovementMethod());

        val mAlertDialog = mBuilder.show()
        val currentTime = LocalDateTime.now().toString().substring(11,16)
        mDialogView.button1.setOnClickListener{
           // state = false
            map.addMarker(MarkerOptions().position(LatLng(latitude.toDouble(),longitude.toDouble())).title(address)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            //mAlertDialog.dismiss()


            if (mDialogView.Comment.text.isNotEmpty()){
              //  Log.d("Comment","not Null")
                if (rating.contains(getUsername()) == true){
                    mDialogView.errMessage.setText("ไม่สามารถยืนยันเหตุการณ์ได้เนื่องจากผู้ใช้ได้ทำการยืนยันไปแล้ว")
                    //putRequest(id,userName,topic,comment+",${currentTime};${mDialogView.Comment.text}",rating,latitude,longitude)
                }else {

                   // map.addMarker(MarkerOptions().position(LatLng(latitude.toDouble(),longitude.toDouble())).title(address)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    putRequest(
                        id,
                        userName,
                        topic,
                        comment + ",${currentTime};${mDialogView.Comment.text}",
                        rating + ";${getUsername()}",
                        latitude,
                        longitude
                    )
                    mAlertDialog.dismiss()
                    state = false
                }
            }else{
                //Log.d("Comment","Null")
                if (rating.contains(getUsername()) == true){
                    mDialogView.errMessage.setText("ไม่สามารถยืนยันเหตุการณ์ได้เนื่องจากผู้ใช้ได้ทำการยืนยันเหตุการณ์นี้ไปแล้ว")
                   // putRequest(id,userName,topic,comment+",${currentTime};${mDialogView.Comment.text}",rating,latitude,longitude)
                } else {

                    putRequest(
                        id,
                        userName,
                        topic,
                        comment,
                        rating + ";${getUsername()}",
                        latitude,
                        longitude
                    )
                    mAlertDialog.dismiss()
                    state = false
                }
            }
        }
        mDialogView.button2.setOnClickListener{

            try {
                val sourceLocation = LatLng(getLatitude()!!.toDouble(), getLongitude()!!.toDouble())
                val destLocation = LatLng(latitude.toDouble(),longitude.toDouble())
                val URL = getDirectionURL(sourceLocation,destLocation)
                GetDirection(URL).execute()
            }catch (e:Exception){
                Log.d("Err GetAlert LatLng()",e.message.toString())
            }
            map.addMarker(MarkerOptions().position(LatLng(latitude.toDouble(),longitude.toDouble())).title(address)).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            if (mDialogView.Comment.text.isNotEmpty()){
                //Log.d("Comment","not Null")
                putRequest(id,userName,topic,comment+",${currentTime};${mDialogView.Comment.text}",rating,latitude,longitude)
            }
            mAlertDialog.dismiss()
            state = false
        }
        mDialogView.close.setOnClickListener{

            mAlertDialog.dismiss()
            state = false
        }



    }
    private fun splitComment(comment:String): String {
        val delim = ","
        val split = comment.split(delim)
        var strComment = ""
        for (str in split){

            if (str.contains(";") == true){

                strComment+="${str.substring(0,5)} ${str.substring(6,str.length)}\n"
            }

        }

        return strComment

    }

    private fun setUsername(paramsName : String){
        userName = paramsName
    }

    private fun getUsername():String{
        return userName
    }

    private fun findUserInRating(rating: String) :Int{
        val split = rating.split(";")
        var count = 0
        for (str in split) {
            if (str != "") {
                count += 1
                Log.d("Test rating", str + " ${count}")
            }
        }
        return count
    }











}