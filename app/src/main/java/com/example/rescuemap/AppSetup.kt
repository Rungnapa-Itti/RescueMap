package com.example.rescuemap

import androidx.appcompat.app.AppCompatActivity

class AppSetup {
    public fun finishApp(appCompatActivity:AppCompatActivity){
        appCompatActivity.finish()

    }
    public fun refreshApp(appCompatActivity: AppCompatActivity){
        appCompatActivity.recreate()
    }

}