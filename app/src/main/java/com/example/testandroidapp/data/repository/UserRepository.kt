package com.example.testandroidapp.data.repository

import com.example.testandroidapp.data.api.RetrofitInstance
import com.example.testandroidapp.data.model.UserDto

class UserRepository {
    suspend fun getUsers(): List<UserDto> {
        return RetrofitInstance.api.getUsers()
    }
}