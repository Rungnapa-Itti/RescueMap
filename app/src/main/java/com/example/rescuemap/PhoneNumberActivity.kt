package com.example.rescuemap

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.fragment_call.*
import kotlinx.android.synthetic.main.fragment_call.view.*

class PhoneNumberActivity :  AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_call)

        val actionBar: ActionBar? = supportActionBar
        if (actionBar != null) {
//            val cal: Calendar = Calendar.getInstance()
            val dynamicTitle: String = "Map"
            val colorDrawable = ColorDrawable(Color.parseColor("#db5a6b"))
            //Setting a dynamic title at runtime. Here, it displays the current time.
            actionBar.setTitle(dynamicTitle)
            actionBar.setBackgroundDrawable(colorDrawable)
            actionBar.setDisplayHomeAsUpEnabled(true)



        }
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.fragment_call,null)
        mDialogView.call.setPaintFlags(Paint.FAKE_BOLD_TEXT_FLAG);

        val numberArray = mutableListOf<String>()
        numberArray.add("เหตุด่วนเหตุร้าย                                             191")
        numberArray.add("เหตุไฟไหม้                                                      199")
        numberArray.add("ตำรวจท่องเที่ยว                                            1155")
        numberArray.add("หน่วยแพทย์วชิรฯ                                          1154")
        numberArray.add("จส.100                                                          1137")
        numberArray.add("สายด่วนทางหลวง                                          1193")
        numberArray.add("เจ็บป่วยฉุกเฉิน                                               1669")
        numberArray.add("สายด่วนจราจร                                               1197")
       // var numberArray = resources.getStringArray(R.array.PhoneNumber)
        var arrayAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,numberArray)

        listCall.adapter = arrayAdapter



        listCall.setOnItemClickListener{ parent: AdapterView<*>?, view: View?, position: Int, id: Long ->

           // Toast.makeText(this,numberArray[position],Toast.LENGTH_SHORT).show()

            val intent = Intent(Intent.ACTION_DIAL)

            if (numberArray[position].contains("จส.100") == true){
                intent.setData(Uri.parse("tel:1137"))
            }else {
                intent.setData(Uri.parse("tel:${numberArray[position]}"))
            }
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}