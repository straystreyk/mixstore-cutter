package com.pokrikinc.mixpokrikcutter.data

import com.pokrikinc.mixpokrikcutter.data.remote.IPlotterApi
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    private lateinit var retrofit: Retrofit
    private val client = OkHttpClient.Builder().build()

    fun init() {
        val baseUrl = PreferenceManager.getBaseUrl()
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun updateBaseUrl(newBaseUrl: String) {
        if (newBaseUrl.isNotBlank()) {
            retrofit = Retrofit.Builder()
                .baseUrl(newBaseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
    }

    fun getPlotterApi(): IPlotterApi = retrofit.create(IPlotterApi::class.java)
}

