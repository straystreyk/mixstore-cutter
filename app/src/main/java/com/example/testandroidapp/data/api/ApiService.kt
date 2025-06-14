package com.example.testandroidapp.data.api


import com.example.testandroidapp.data.model.UserDto
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("users")
    suspend fun getUsers(): List<UserDto>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserDto
}