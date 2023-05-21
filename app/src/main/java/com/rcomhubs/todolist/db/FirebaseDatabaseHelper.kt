package com.rcomhubs.todolist.db

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class FirebaseDatabaseHelper {
    class FirebaseDatabaseHelper {

        private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
        private val databaseRef: DatabaseReference = database.reference

        fun sendData(nodeName: String, data: HashMap<String, Any?>, callback: FirebaseCallback) {
//            val database = FirebaseDatabase.getInstance()
//            val ref = database.getReference(nodeName)
//            val id = ref.push().key

            val newDataRef = databaseRef.child(nodeName).push()
            newDataRef.setValue(data)
                .addOnSuccessListener {
                    callback.onSuccess()
                }
                .addOnFailureListener { exception ->
                    callback.onError(exception)
                }
        }

        interface FirebaseCallback {
            fun onSuccess()
            fun onError(exception: Exception)
        }
    }
}