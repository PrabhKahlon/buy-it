package com.buyit.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.buyit.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signinText.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        binding.buttonLogin.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()
            val confirmPassword = binding.passwordConfirmText.text.toString()
            var accountType = intent.getStringExtra("accountType")
            if(accountType == null) {
                accountType = "user"
            }

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                val db = Firebase.firestore
                                val extraUserInfo = hashMapOf(
                                    "email" to email,
                                    "accountType" to accountType,
                                    "productIDs" to arrayOf<String>()
                                )
                                if(firebaseAuth.currentUser != null) {
                                    db.collection("users").document(firebaseAuth.currentUser!!.uid).set(extraUserInfo, SetOptions.merge())
                                        .addOnSuccessListener { documentReference ->
                                            Log.d("REGISTER", "Added extra user info to ${firebaseAuth.currentUser!!.email}")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.w("REGISTER", "Error adding extra user info", e)
                                        }
                                }
                                Toast.makeText(this, "Signup was successful!", Toast.LENGTH_SHORT)
                                    .show()
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            } else {
                                Toast.makeText(this, "An error has occurred.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    binding.passwordText.error = "Password does not match."
                }
            } else {
                if (email.isEmpty()) {
                    binding.emailText.error = "Email cannot be empty."
                }
                if (password.isEmpty()) {
                    binding.passwordText.error = "Password cannot be empty."
                }
                if (confirmPassword.isEmpty()) {
                    binding.passwordConfirmText.error = "Confirm your password."
                }
            }
        }
    }
}