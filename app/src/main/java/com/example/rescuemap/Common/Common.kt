package com.example.rescuemap.Common

import com.example.rescuemap.Remote.IGoogleAPIService
import com.example.rescuemap.Remote.RetrofitClient

object Common {
    private val GOOGLE_API_URL="https://maps.googleapis.com/"

    val googleApiService:IGoogleAPIService
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
}