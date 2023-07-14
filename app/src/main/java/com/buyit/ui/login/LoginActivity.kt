package com.buyit.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.buyit.MainActivity
import com.buyit.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding:ActivityLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.signupText.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("accountType", "user")
            startActivity(intent)
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.emailText.text.toString()
            val password = binding.passwordText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful){
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }else{
                            binding.emailText.error = "Incorrect username and/or password."
                        }
                    }
                }
            else {
                if (email.isEmpty()) {
                    binding.emailText.error = "Email cannot be empty."
                }
                if (password.isEmpty()){
                    binding.passwordText.error = "Password cannot be empty."
                }
            }
        }
        }
    
    override fun onStart() {
        super.onStart()
        
        if(firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    }