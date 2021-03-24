package com.example.rescuemap

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.rescuemap.DataServer.DataItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_add_place.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.lang.reflect.Type
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*


class AddPlaceActivity : AppCompatActivity(),AdapterView.OnItemSelectedListener {

    lateinit var topicName: String
    lateinit var editPlaceDetail: EditText
    private var MyLongitude: Double? = null
    private var Mylatitude: Double? = null
    var listLoc :MutableList<String> = ArrayList()
    private var selectedLat: Double? = null
    private var selectedLng: Double? = null
    var currLoc2: String? = null
    private var currLat: Double?= null
    private var currLng: Double?= null
    var tempGoogleUsername: String? = null
    var timeAmount:Int = 60


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)


        var listPlaceName = arrayListOf<String>("จราจรติดขัด", "ทะเลาะวิวาท", "ไฟไหม้", "น้ำท่วม", "อื่นๆ")
//        val currLoc=intent.getStringExtra("list")
        val myLat: String
        val myLng: String
        val newLat=intent.getStringExtra("newLat")
        val newLng=intent.getStringExtra("newLng")
        val selectedLoc=intent.getStringExtra("selectedLoc")
        currLoc2 = intent.getStringExtra("list")

        if (newLat == null){
            myLat= intent.getStringExtra("myLat").toString()
            myLng= intent.getStringExtra("myLng").toString()
            val currLoc=intent.getStringExtra("list")
            listLoc.add(currLoc.toString())
            Log.e("currentLocAdd", currLoc.toString())
        }
        else{
            myLat= newLat
            myLng= newLng.toString()
            listLoc.add(selectedLoc.toString())
            Log.e("currentLocAdd", selectedLoc.toString())
        }
//        if (newLat == null){
//            val currLoc=intent.getStringExtra("list")
//            listLoc.add(currLoc.toString())
//            Log.e("currentLocAdd", currLoc.toString())
//        }
//        else{
//            listLoc.add(selectedLoc.toString())
//        }

        setLatitudeAndLongitude(myLat,myLng)

//        listLoc.add(currLoc.toString())
//        Log.e("currentLocAdd", currLoc.toString())


        val adapterLoc :ArrayAdapter<String> = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,listLoc)
        spinnerLocation.adapter = adapterLoc
        spinnerLocation.onItemSelectedListener = this

        val adapterPlaceName :ArrayAdapter<String> = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,listPlaceName)
        spinnerPlaceName.adapter = adapterPlaceName
        spinnerPlaceName.onItemSelectedListener = this

        editPlaceDetail = findViewById(R.id.editPlaceDetail)

        getRequest()

        //Start of dynamic title code---------------------
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
//            val cal: Calendar = Calendar.getInstance()
            val dynamicTitle: String = "RescueMap"
            val colorDrawable = ColorDrawable(Color.parseColor("#db5a6b"))
            //Setting a dynamic title at runtime. Here, it displays the current time.
            actionBar.setTitle(dynamicTitle)
            actionBar.setBackgroundDrawable(colorDrawable)
            actionBar.setDisplayHomeAsUpEnabled(true)

        }
        //End of dynamic title code----------------------

        val googleUserName= intent.getStringExtra("googleUsername2").toString()
        Log.w("userAddPage",googleUserName)

        buttonNewLocation.setOnClickListener {
            var nextTime = calculateNextTime(getTimeAmountPlace())
            Log.d("NextTime",nextTime)

            postRequest(topicName, editPlaceDetail.text.toString(),nextTime)
            val addBut = Intent(this@AddPlaceActivity, MapsActivity::class.java)
            addBut.putExtra("googleUsername1",tempGoogleUsername)
            startActivity(addBut)
        }

        selectNewLocation.setOnClickListener {
            val selectBut = Intent(this@AddPlaceActivity,MapSelectActivity::class.java)
            selectBut.putExtra("googleUsername3",googleUserName)
            Log.w("sendUserToSelectPage", googleUserName)
            startActivity(selectBut)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//        val text: String = parent?.getItemAtPosition(position).toString()
        val text: String = spinnerPlaceName.selectedItem.toString()
        Log.d("spinnerPlaceName",text)
        if(text.equals("จราจรติดขัด") || text.equals("ทะเลาะวิวาท") ){
            setTimeAmountPlace(60) // 1 hour
        }else if (text.equals("ไฟไหม้") || text.equals("น้ำท่วม")){
            setTimeAmountPlace(180) // 3 hour
        }else{
            setTimeAmountPlace(2) // 1 hour (อื่นๆ)
        }

        val locSelect = spinnerLocation.selectedItem.toString()
        topicName = text
        Log.e("LocSelect",locSelect)
//        Log.e("testSelect",topicName)
        Log.d("splitItem",splitSelectedItem(locSelect))
        getLocationFromAddress(splitSelectedItem(locSelect))
        Log.d("LatSelect",selectedLat.toString())
        Log.d("LngSelect",selectedLng.toString())
//        var currLat= intent.getStringExtra("currLat")
//        Log.d("currLat",currLat.toString())

        getLocationFromAddressForCurrent(currLoc2.toString())
        Log.d("currLoc2",currLoc2.toString())
        Log.d("currLat",currLat.toString())
        Log.d("currLng",currLng.toString())
        val selectedLocAc = Intent(this@AddPlaceActivity,MapsActivity::class.java)
        selectedLocAc.putExtra("selectedLat",selectedLat.toString())
        selectedLocAc.putExtra("selectedLng",selectedLng.toString())
        if((selectedLng != currLng) && (selectedLng != currLng) && currLoc2 != null){
//            selectedLocAc.putExtra("stateSelectDropdown",stateSelectDropdown)
            startActivity(selectedLocAc)
        }
        setLatitudeAndLongitude(selectedLat.toString(),selectedLng.toString())
    }

    private fun splitSelectedItem(item:String): String {
        val delim = "("
        val split = item.split(delim)
        var strSelected = ""
        var strTemp = ""
        for (str in split){
            if (str.contains(")") == true){
                strTemp = "${str.substringAfter('(')}\n"
                strSelected = "${strTemp.substringBefore(')')}\n"
            }
            else{
                strSelected = item
            }

        }

        return strSelected

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

    private fun getLocationFromAddress(addr: String){
        val geocoder = Geocoder(this)
        var addresses = geocoder.getFromLocationName(addr,1)
        if(addresses.size > 0) {
            selectedLat= addresses.get(0).getLatitude()
            selectedLng= addresses.get(0).getLongitude()
        }
    }
    private fun getLocationFromAddressForCurrent(addr: String){
        val geocoder = Geocoder(this)
        var addresses = geocoder.getFromLocationName(addr,1)
        if(addresses.size > 0) {
            currLat= addresses.get(0).getLatitude()
            currLng= addresses.get(0).getLongitude()
        }
    }

    private fun getAddress(lat: LatLng): String? {

        val geocoder = Geocoder(this)
        val list = geocoder.getFromLocation(lat.latitude, lat.longitude, 1)
        // Log.e("lat", lat.latitude.toString())

        //for search places
        return list[0].getAddressLine(0)
    }
    private fun setLatitudeAndLongitude(lat: String, lng: String) {
        Mylatitude = lat.toDouble()
        MyLongitude = lng.toDouble()

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

            val collectionType: Type = object : TypeToken<List<DataItem?>?>() {}.getType()
            val data: List<DataItem> = Gson()
                    .fromJson(response, collectionType) as List<DataItem>
            checkItemJson(data)
            Log.d("json",data.toString())


        }, Response.ErrorListener { error -> println("GET error $error") })
        queue.add(request)

    }

    fun checkItemJson(data:List<DataItem>?){
        val geocoder = Geocoder(this)


        for (item in data!!){
            Log.d("TEST","${item.latitude} ${item.longitude}")
            var content = "${item.latitude} ${item.longitude} ${item.topic}"


            try {
                val kmInDec = calculate(item.latitude.toDouble(),item.longitude.toDouble())
                if (kmInDec <= 1 ) {
                    var locSame = LatLng(item.latitude.toDouble(), item.longitude.toDouble())
                    listLoc.add(item.topic+" ("+getAddress(locSame).toString()+")")
//                    topicName = getAddress(locSame).toString()
                    //alert isn't open and Latitude Longitude not null and this location never open alert
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

    private fun postRequest(topic: String, comment: String,nextTime: String) {
        val postUrl = "http://10.0.2.2:8081/messages"
        val queue = Volley.newRequestQueue(this)
        val postData = JSONObject()
        val currentDate = LocalDateTime.now().toString().substring(0, 16)
        val googleUserName= intent.getStringExtra("googleUsername2").toString()
        Log.w("userAddPage",googleUserName)
        tempGoogleUsername = googleUserName

        try {
            postData.put("id", currentDate+"-${nextTime}")
            postData.put("userName", googleUserName)
            postData.put("topic", topic)
            postData.put("comment", comment)
            postData.put("rating", "")
            postData.put("latitude", getLatitude().toString())
            postData.put("longitude", getLongitude().toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val postRequest: JsonObjectRequest =
                object : JsonObjectRequest(
                        Request.Method.POST, postUrl, postData,
                        Response.Listener { response ->
                            // response
                            Log.d("POST Http response: ", "$response")
                        },
                        Response.ErrorListener { error ->
                            // error
                            Log.i("POST error: ", "$error")
                        }
                ) {

                }
        queue.add(postRequest)
    }

    private fun calculateNextTime(timeAmount:Int):String{
        val df = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val now = Calendar.getInstance()
        now.add(Calendar.MINUTE, timeAmount)
        val teenMinutesFromNow = df.format(now.time)
        val nextTime = teenMinutesFromNow

        return  add12Hour(nextTime)
    }

    private fun setTimeAmountPlace(time:Int){
        timeAmount = time
    }

    private fun getTimeAmountPlace():Int{
        return timeAmount
    }

    private fun add12Hour(nextTime:String) :String{
        var hour = nextTime.substring(0,2).toInt()
        var minute = nextTime.substring(3,5)
        hour = hour+12
        return "${hour}:${minute}"

    }

}