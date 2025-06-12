package com.example.testandroidapp.data.api


import com.example.testandroidapp.data.model.UserDto
import retrofit2.http.GET

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserDto>
}