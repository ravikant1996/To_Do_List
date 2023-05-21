package com.rcomhubs.todolist.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.rcomhubs.todolist.databinding.ActivityLoginBinding
import com.rcomhubs.todolist.db.FirebaseDatabaseHelper
import com.rcomhubs.todolist.model.UserModel
import com.rcomhubs.todolist.utils.toMap
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        auth = FirebaseAuth.getInstance()

        binding.btnSendVerificationCode.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            if (phoneNumber.isNullOrEmpty() || phoneNumber.length < 10) {
                Toast.makeText(this, "Enter valid mobile number", Toast.LENGTH_SHORT).show()
            } else {
                sendVerificationCode(phoneNumber)
            }
        }

        binding.btnVerifyCode.setOnClickListener {
            val code = binding.etVerificationCode.text.toString().trim()
            if (!code.isNullOrEmpty()) {
                verifyVerificationCode(code)
            } else {
                Toast.makeText(this, "Enter otp", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if (auth.currentUser != null) {
            // User is authenticated, navigate to the next activity
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Remove all activities from the stack
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        // Configure the PhoneAuthOptions
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+1$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Verification completed successfully
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(exception: FirebaseException) {
                    // Verification failed
                    Log.e(TAG, "Verification failed: $exception")
                    Toast.makeText(applicationContext, "Verification failed", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    // Code sent to the user's phone
                    this@LoginActivity.verificationId = verificationId
                    binding.verifyLayout.visibility = View.VISIBLE
                    Toast.makeText(applicationContext, "Verification code sent", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .build()

// Start the phone number verification
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyVerificationCode(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId ?: "", code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this, OnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Phone authentication successful
                    val user = task.result?.user
                    Toast.makeText(applicationContext,
                        "Authentication successful",
                        Toast.LENGTH_SHORT).show()
                    if (task.result.additionalUserInfo?.isNewUser == true) {
                        Toast.makeText(applicationContext,
                            "New User",
                            Toast.LENGTH_SHORT).show()
                        storeData(task)
                    } else {
                        Toast.makeText(applicationContext,
                            "Old User",
                            Toast.LENGTH_SHORT).show()
                        goToUI()
                    }
                } else {
                    // Phone authentication failed
                    Toast.makeText(applicationContext, "Authentication failed", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun storeData(task: Task<AuthResult>) {
        val mAuth = FirebaseAuth.getInstance()
        val uid = mAuth.currentUser?.uid.toString()

        val user = UserModel()
        user.uid = uid
        val ppp = mAuth.currentUser?.phoneNumber.toString()
        if (!ppp.equals("null")) {
            user.phone = ppp
        }
        user.providerId = task.result.additionalUserInfo?.providerId.toString()
        user.flag = true
        user.timestamp = System.currentTimeMillis()

        // Convert the JSON string to a HashMap
        val hashMap: HashMap<String, Any?> = user.toMap()

        val firebaseDatabaseHelper = FirebaseDatabaseHelper.FirebaseDatabaseHelper()

        firebaseDatabaseHelper.sendData("users", hashMap, object :
            FirebaseDatabaseHelper.FirebaseDatabaseHelper.FirebaseCallback {
            override fun onSuccess() {
                // Data sent successfully
                // Handle the success case here
                goToUI()
            }

            override fun onError(exception: Exception) {
                // An error occurred while sending data
                // Handle the error case here
                Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun goToUI() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finishAffinity() // Remove all activities from the stack
    }


    companion object {
        private const val TAG = "LoginActivity"
    }
}