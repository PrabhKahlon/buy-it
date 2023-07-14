package com.buyit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProductActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_product)


        val db = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener {result ->

                result.data?.get("productIDs")?.let { displayProducts(it) }

            }
        }


        setupAddButton()
    }

    override fun onResume() {
        super.onResume()

        val db = Firebase.firestore
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) {
            db.collection("users").document(currentUser.uid).get().addOnSuccessListener {result ->
                result.data?.get("productIDs")?.let { displayProducts(it) }
            }
        }
    }


    private fun displayProducts(dataAny: Any) {
//        println("PULLED FROM DB:" + data)
        //array of ids is in data
        val data = dataAny as List<String>
        val db = Firebase.firestore
        //products are listed in products
        val products = ArrayList<Product>()
        db.collection("products")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val product = Product()
                    product.productCode = document.data.getValue("productCode").toString()
                    product.product = document.data.getValue("product").toString()
                    product.price = document.data.getValue("price").toString().toDouble()
                    product.brand = document.data.getValue("brand").toString()
//                    println("ADDING: " + product)
                    products.add(product)
                }

                // productsToDisplay will only have the products associated with that acc
                var productsToDisplay = ArrayList<Product>()
//                println("TESTING: " + products)

                for(item in products){
//                    println("looping: " + item.productCode)
                }

                for(item in products){
                    //("looping: " + item.productCode)
                    for(savedItems in data){
                        if(item.productCode == savedItems){
                            productsToDisplay.add(item)
                            //println("PULLED FROM DB: line 67 " + savedItems)
                        }
                    }
                }

                for(item in productsToDisplay){
                    //u can see that the data is right here
//                    println("ACCOUNT CONTAINS: " + item)
                }


                //TODO - PRODUCTS TO DISPLAY HAS THE PRODUCT OBJECTS  ---- JUST SHOW IT IN A LISTVIEW ---- VARIABLE = productsToDisplay

                //display products
                val productListView = findViewById<ListView>(R.id.listViewOfProducts)
                val productListAdapter = ProductAdapter(this, productsToDisplay)
                productListView.adapter = productListAdapter




    }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
            }

    }

    private fun setupAddButton() {
        var button = findViewById<Button>(R.id.addProductButton);

        button.setOnClickListener {
            var intent = Intent(this, AddNewProductActivity::class.java)
            startActivity(intent);
        }
    }

}