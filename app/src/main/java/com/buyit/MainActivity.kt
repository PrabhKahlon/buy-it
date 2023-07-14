package com.buyit

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.buyit.database.*
import com.buyit.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.android.synthetic.main.nav_header_main.view.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    //database variables
    private lateinit var database: CartDatabase
    private lateinit var databaseDao: CartDao
    private lateinit var repository: CartRepository
    private lateinit var cartViewModelFactory: CartViewModelFactory
    private lateinit var cartViewModel: CartViewModel

    // text recognition variables
    private lateinit var tempImageFileName : String
    private lateinit var tempImage : File
    private lateinit var tempImageUri : Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val root = binding.root

        val sharedPref = this.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);

        when(sharedPref.getInt("themeUnit", -1)){
            0 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                delegate.applyDayNight()
            }
            1 -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                delegate.applyDayNight()
            }
        }

        setContentView(root)

        Util.checkPermissions(this)


        tempImageFileName = "image_tmp.jpg"
        tempImage = File(getExternalFilesDir(null), tempImageFileName)
        tempImageUri = FileProvider.getUriForFile(this, "com.buyit", tempImage)

        setSupportActionBar(binding.appBarMain.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        //get items from database
        database = CartDatabase.getInstance(this)
        databaseDao = database.cartDao
        repository = CartRepository(databaseDao)
        cartViewModelFactory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, cartViewModelFactory)[CartViewModel::class.java]

        binding.appBarMain.fab.setOnClickListener { view ->
            //This launches the activity to take an image and process it to buy the product
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempImageUri)
            cameraResult.launch(cameraIntent)
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null) {
            Firebase.firestore.collection("users").document(currentUser.uid).get().addOnSuccessListener {result ->
                if(result.data?.get("accountType") == "business") {
                    binding.appBarMain.addProduct.visibility = View.VISIBLE
                }
            }
        }
        binding.appBarMain.addProduct.setOnClickListener { view ->
            //This should launch the activity to take add a product to the database
            Toast.makeText(this, "Add product button clicked", Toast.LENGTH_SHORT).show()
            val addProductIntent = Intent(this, ProductActivity::class.java)
            startActivity(addProductIntent)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_logout
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        val firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser != null) {
            navView.getHeaderView(0).emailView.text = firebaseAuth.currentUser!!.email
        }
    }

    private val cameraResult : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { result ->
        if(result.resultCode == Activity.RESULT_OK) {
            //Add item to list
            val ocrImage = Util.getBitmap(this, tempImageUri, tempImage)
            var scanFlag = false
            Log.d("OCR image", ocrImage.toString())

            val db = Firebase.firestore

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
                        products.add(product)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
                }

            if(ocrImage != null){
                val imageBitmap = ocrImage
                val imageOCR = InputImage.fromBitmap(imageBitmap, 0)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                recognizer.process(imageOCR)
                    .addOnSuccessListener {
                        Log.d("OCR", it.text)
                        for((index, block) in it.textBlocks.withIndex()) {
                            val itemCode = block.text
                            for(item in products) {
                                if(itemCode == item.productCode) {
                                    Toast.makeText(this, R.string.item_scanned, Toast.LENGTH_SHORT).show()
                                    try {
                                        addItemToCart(item.product, item.price)
                                        scanFlag = true
                                    } catch (e: NumberFormatException) {
                                        Log.d("OCR","no price")
                                    }
                                }
                                //Adding break to not waste time processing junk text. Probably a better way by using while.
                                if(scanFlag) {
                                    break
                                }
                            }
                            Log.d("OCR", itemCode)
                            //Adding break to not waste time processing junk text. Probably a better way by using while.
                            if(scanFlag) {
                                break
                            }
                        }
                        if(!scanFlag) {
                            Toast.makeText(this, R.string.invalid_tag_item, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Log.d("OCR", "Failed to Detect Text")
                        Toast.makeText(this, R.string.detect_text_fail, Toast.LENGTH_LONG).show()
                    }
            }
        } else {
            Toast.makeText(this, R.string.scan_fail, Toast.LENGTH_SHORT).show()
        }
    }

    fun addItemToCart(itemName : String, itemPrice : Double) {
        //insert into database
        database = CartDatabase.getInstance(this)
        databaseDao = database.cartDao
        repository = CartRepository(databaseDao)
        cartViewModelFactory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, cartViewModelFactory)[CartViewModel::class.java]

        Log.d("ADD", cartViewModel.allOrdersLiveData.value.toString())
        val currentItems = cartViewModel.allOrdersLiveData.value

        //Update Quantity if already in cart
        if (currentItems != null) {
            for(item in currentItems) {
                Log.d("ADD", item.itemName.toString())
                if(item.itemName == itemName) {
                    item.itemQuantity = item.itemQuantity?.plus(1)
                    cartViewModel.update(item)
                    return
                }
            }
        }

        //insert entry into database
        val entry = Cart()

        entry.itemName = itemName
        entry.itemPrice = itemPrice
        entry.itemQuantity = 1

        entry.date = Calendar.getInstance().timeInMillis

        val formatter = SimpleDateFormat("MM/dd/yyyy")
        val dateString = formatter.format(Date(entry.date!!))

        cartViewModel.insert(entry)

        Log.d("entry", "name: ${entry.itemName}")
        Log.d("entry", "price: ${entry.itemPrice}")
        Log.d("entry", "quantity: ${entry.itemQuantity}")
        Log.d("entry", "date: $dateString")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun attachBaseContext(newBase: Context?) {
        val sharedPrefs = newBase?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE);
        val lang = sharedPrefs?.getString("languageLocale", "en")
        val locale = Locale(lang!!)
        val config = Configuration(newBase!!.resources.configuration)
        Locale.setDefault(locale)
        config.setLocale(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val context = newBase!!.createConfigurationContext(config)
            super.attachBaseContext(context)
        } else {
            newBase!!.resources.updateConfiguration(config, newBase!!.resources.displayMetrics)
            super.attachBaseContext(newBase);
        }
    }
}