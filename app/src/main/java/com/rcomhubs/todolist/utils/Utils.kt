package com.rcomhubs.todolist.utils

import com.rcomhubs.todolist.model.TodoModel
import com.rcomhubs.todolist.model.UserModel

fun UserModel.toMap(): HashMap<String, Any?> {
    return hashMapOf(
        "name" to name,
        "uid" to uid,
        "providerId" to providerId,
        "timestamp" to timestamp,
        "flag" to flag,
        "phone" to phone
    )
}

fun TodoModel.toMap(): HashMap<String, Any?> {
    return hashMapOf(
        "uid" to uid,
        "title" to title,
        "description" to description,
        "category" to category,
        "date" to date,
        "time" to time,
        "isFinished" to isFinished
    )
}