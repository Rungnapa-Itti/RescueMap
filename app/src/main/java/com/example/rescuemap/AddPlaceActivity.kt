package com.example.rescuemap

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_place.*
import java.util.*


class AddPlaceActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_place)

        var listLoc :MutableList<String> = ArrayList()
        var listPlaceName = arrayListOf<String>("จราจรติดขัด", "ทะเลาะวิวาท", "ไฟไหม้")
        val currLoc=intent.getStringExtra("list")

        listLoc.add(currLoc.toString())
        Log.e("currentLocAdd", currLoc.toString())


        val adapterLoc :ArrayAdapter<String> = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,listLoc)
        spinnerLocation.adapter = adapterLoc

        val adapterPlaceName :ArrayAdapter<String> = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,listPlaceName)
        spinnerPlaceName.adapter = adapterPlaceName

        buttonNewLocation.setOnClickListener {

        }

        //Start of dynamic title code---------------------
        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
//            val cal: Calendar = Calendar.getInstance()
            val dynamicTitle: String = "Map"
            val colorDrawable = ColorDrawable(Color.parseColor("#db5a6b"))
            //Setting a dynamic title at runtime. Here, it displays the current time.
            actionBar.setTitle(dynamicTitle)
            actionBar.setBackgroundDrawable(colorDrawable);

        }
        //End of dynamic title code----------------------
    }
}