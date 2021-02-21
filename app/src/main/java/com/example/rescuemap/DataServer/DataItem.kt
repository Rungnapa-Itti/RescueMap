package com.example.rescuemap.DataServer


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DataItem(
    val comment: String,
    val id: String,
    val latitude: String,
    val longitude: String,
    val rating: String,
    val topic: String,
    val userName: String
)