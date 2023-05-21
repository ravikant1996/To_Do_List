package com.rcomhubs.todolist.model

data class UserModel(
    var uid: String = "",
    var name: String = "",
    var phone: String = "",
    var providerId: String = "",
    var timestamp: Long = 0L,
    var flag: Boolean = true,
)
