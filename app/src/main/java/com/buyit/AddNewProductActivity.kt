package com.buyit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class AddNewProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val db = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener {result ->
            }
        }

        setupAddButton()


    }

    private fun getNameEditText(): String {
        var edit = findViewById<EditText>(R.id.pNameEditText)
        return edit?.text.toString()
    }

    private fun getPriceEditText(): Double {
        var edit = findViewById<EditText>(R.id.priceEditText)
        if(edit?.text.toString().isEmpty()){
            return Double.NaN
        }
        return edit?.text.toString().toDouble()
    }

    private fun getBrandEditText(): String {
        var edit = findViewById<EditText>(R.id.brandEditText)
        return edit?.text.toString()
    }

    private fun setupAddButton() {
        var button = findViewById<Button>(R.id.addNewProductButton);
        val db = Firebase.firestore

        button.setOnClickListener {
            //add to firebase here

            val product = Product()
            var productCode = genRandomCode()

            product.productCode = productCode
            product.product =  getNameEditText()
            product.price = getPriceEditText().toDouble()
            product.brand = getBrandEditText()

            if(getBrandEditText().isEmpty() || getNameEditText().isEmpty() || getPriceEditText().isNaN()){
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()

            }
            else{
                //TODO -- add the new product to the database  -- done

                //db.collection("products").doc().set(product)

                val addedDocRef = db.collection("products").add(product).addOnSuccessListener {
                    println("ADDING: LINE 75" + it.id)

                    Toast.makeText(this, "New product added", Toast.LENGTH_SHORT).show()

                    //TODO -- add product code to the account product code array -- done
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if(currentUser != null) {
                        db.collection("users").document(currentUser.uid).get().addOnSuccessListener {result ->
                            if(result.data?.get("productIDs") != null) {
                                var data = result.data?.get("productIDs") as List<String>
                                data.plus(productCode)
                                var data2 = arrayOf(data)


                                var firebaseAuth = FirebaseAuth.getInstance()
                                val map = HashMap<String, Any>()
                                //var finalARR = arrayOf();
                                map["productIDs"] = data2
                                db.collection("users").document(firebaseAuth.currentUser!!.uid).update("productIDs", FieldValue.arrayUnion(productCode)).addOnSuccessListener {
                                    Toast.makeText(this, "New product added to account too", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            }

                        }
                    }

                }

                //System.out.println("Added document with ID: " + addedDocRef.get().getId())



                //done close activity
                //Toast.makeText(this, "New product added", Toast.LENGTH_SHORT).show()
                //finish()
            }

        }
    }

    private fun genRandomCode(): String {
        val random = Random()
        var num = random.nextInt(9999999 - 1111111) + 1111111
        return num.toString()
    }

}