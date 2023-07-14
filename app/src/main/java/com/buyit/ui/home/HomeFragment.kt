package com.buyit.ui.home

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.buyit.CartAdapter
import com.buyit.R
import com.buyit.database.*
import com.buyit.databinding.FragmentHomeBinding
import com.buyit.Util
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import org.json.JSONObject

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var checkoutButton: Button

    //database variables
    private lateinit var database: CartDatabase
    private lateinit var databaseDao: CartDao
    private lateinit var repository: CartRepository
    private lateinit var cartViewModelFactory: CartViewModelFactory
    private lateinit var cartViewModel: CartViewModel

    private var firstTime = true

    private lateinit var cartListView: ListView
    private lateinit var cartList: ArrayList<Cart>
    private lateinit var cartAdapter: CartAdapter

    private lateinit var firebaseAuth: FirebaseAuth

    //Stripe
    lateinit var paymentSheet: PaymentSheet
    lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    lateinit var paymentIntentClientSecret: String
    //Moving total up here to send to stripe
    var total = 0.0

    override fun onAttach(context: Context) {
        super.onAttach(context)
        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        checkoutButton = root.findViewById(R.id.button)

        firebaseAuth = FirebaseAuth.getInstance()
        cartListView = root.findViewById(R.id.cart_items)
        cartListView.addHeaderView(layoutInflater.inflate(R.layout.cart_item_header, null))
        cartListView.addFooterView(layoutInflater.inflate(R.layout.cart_item_footer, null))
        cartList = ArrayList()
        cartAdapter = CartAdapter(requireActivity(), cartList)
        cartListView.adapter = cartAdapter

        val db = Firebase.firestore

        checkoutButton.setOnClickListener{
            //Start stripe checkout
            paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
            Log.d("HTTP", "?????")
            "https://cmpt362-stripe.azurewebsites.net/api/Stripe-Test?code=MDjjcsAh-UN5Wb9ycS4eKY7OLkNkHwWYhgnvhEZmkoAQAzFuilYWwQ==".httpPost().responseJson { _, _, result ->
                Log.d("HTTP", "Starting")
                if (result is Result.Success) {
                    val responseJson = result.get().obj()
                    paymentIntentClientSecret = responseJson.getString("paymentIntent")
                    customerConfig = PaymentSheet.CustomerConfiguration(
                        responseJson.getString("customer"),
                        responseJson.getString("ephemeralKey")
                    )
                    val publishableKey = responseJson.getString("publishableKey")
                    PaymentConfiguration.init(requireContext(), publishableKey)
                    presentPaymentSheet()
                }
                if(result is Result.Failure) {
                    Log.d("HTTP", result.getException().toString())
                }
            }


            val currentTime = System.currentTimeMillis()
            for (item in cartViewModel.allOrdersLiveData.value!!){
                val item = hashMapOf(
                    "orderId" to item.orderId.toString(),
                    "itemName" to item.itemName.toString(),
                    "itemPrice" to item.itemPrice,
                    "itemQuantity" to item.itemQuantity.toString(),
                    "date" to currentTime.toString(),
                    "history" to true,
                    "user" to firebaseAuth.currentUser?.email.toString()
                )

                db.collection("order_history")
                    .add(item)
                    .addOnSuccessListener { documentReference ->
                        Log.d("HomeFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("HomeFragment", "Error adding document", e)
                    }
            }

            //val checkoutIntent = Intent(this.context, CheckoutActivity::class.java)
            //startActivity(checkoutIntent)
        }

        //display cart
        val cartListAdapter = CartAdapter(requireActivity(), cartList)

        //get items from database
        database = CartDatabase.getInstance(requireActivity())
        databaseDao = database.cartDao
        repository = CartRepository(databaseDao)
        cartViewModelFactory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this, cartViewModelFactory)[CartViewModel::class.java]

        checkoutButton.setOnClickListener{
            //Start stripe checkout. Only works if
            if(total > 0) {
                Log.d("HTTP", "?????")
                val sharedPrefs = context?.getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
                val currency = sharedPrefs?.getString("currencyUnitString", "USD")
                val postParams =
                    JSONObject("""{"price": ${(total * 100).toInt()}, "currency": ${currency}}""")
                "https://cmpt362-stripe.azurewebsites.net/api/Stripe-Test?code=MDjjcsAh-UN5Wb9ycS4eKY7OLkNkHwWYhgnvhEZmkoAQAzFuilYWwQ==".httpPost().body(postParams.toString())
                    .header("Content-Type", "application/json").responseJson { _, _, result ->
                    Log.d("HTTP", "Starting")
                    if (result is Result.Success) {
                        val responseJson = result.get().obj()
                        paymentIntentClientSecret = responseJson.getString("paymentIntent")
                        customerConfig = PaymentSheet.CustomerConfiguration(
                            responseJson.getString("customer"),
                            responseJson.getString("ephemeralKey")
                        )
                        val publishableKey = responseJson.getString("publishableKey")
                        PaymentConfiguration.init(requireContext(), publishableKey)
                        presentPaymentSheet()
                    }
                    if (result is Result.Failure) {
                        Log.d("HTTP", result.getException().toString())
                    }
                }
            } else {
                val currentItems = cartViewModel.allOrdersLiveData.value
                if (currentItems != null) {
                    Log.d("CART", currentItems.size.toString())
                }
                if (currentItems != null) {
                    if(currentItems.isNotEmpty()) {
                        completeOrder()
                    } else {
                        Toast.makeText(requireContext(), "Please add an item", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please add an item", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cartViewModel.allOrdersLiveData.observe(requireActivity(), Observer { it ->
            if(firstTime){
                cartViewModel.deleteAll()
                firstTime = false
            }

            cartListAdapter.replace(it)
            cartListAdapter.notifyDataSetChanged()

            var itemCount = 0
            val currencySymbol =  Util.getCurrencySymbol(requireActivity())

            var subtotal = 0.0
            var tax = 0.0

            for(item in it){
                subtotal += (item.itemPrice!! * item.itemQuantity!!)
                itemCount += item.itemQuantity!!
            }

            val itemCountString = "$itemCount Items"
            cartListView.findViewById<TextView>(R.id.itemCount).text = itemCountString

            subtotal = Util.currencyConversion(subtotal, requireActivity())
            val subtotalString = "Subtotal: $currencySymbol${"%.2f".format(subtotal)}"
            cartListView.findViewById<TextView>(R.id.subtotal).text = subtotalString

            tax = subtotal * 0.12
            val taxString = "Tax: $currencySymbol${"%.2f".format(tax)}"
            cartListView.findViewById<TextView>(R.id.tax).text = taxString

            total = subtotal + tax
            val totalString = "Total: $currencySymbol${"%.2f".format(total)}"
            cartListView.findViewById<TextView>(R.id.total).text = totalString
        })

        cartListView.adapter = cartListAdapter

        cartListView.setOnItemClickListener { parent: AdapterView<*>, view: View, position: Int, id: Long ->
//            println("debug: parent: $parent | view: $view | position: $position | id: $id")

            if(position != 0){
                val itemName = view.findViewById<TextView>(R.id.productName).text as String

                val builder = AlertDialog.Builder(requireActivity())
                builder.setTitle("Delete Item")
                builder.setPositiveButton(android.R.string.yes) { _, _ ->
                    cartViewModel.deleteByItemName(itemName)
                }

                builder.setNegativeButton(android.R.string.no) { _, _ ->
                    Toast.makeText(requireContext(),
                        android.R.string.no, Toast.LENGTH_SHORT).show()
                }
                builder.show()
                builder.create()
            }
        }

        return root
    }

    fun presentPaymentSheet() {
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "Buy It",
                customer = customerConfig,
                allowsDelayedPaymentMethods = true
            )
        )
    }

    fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                Log.d("STRIPE","Payment Canceled")
                Toast.makeText(requireContext(), "Payment Canceled", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Failed -> {
                Log.e("STRIPE","Error: ${paymentSheetResult.error}")
                Toast.makeText(requireContext(), "Payment Failed", Toast.LENGTH_SHORT).show()
            }
            is PaymentSheetResult.Completed -> {
                // If payment is successful add items to order history and clear cart
                Log.d("STRIPE","Payment Success")
                completeOrder()
            }
        }
    }

    fun completeOrder() {
        Toast.makeText(requireContext(), "Order Complete", Toast.LENGTH_SHORT).show()
        val currentTime = System.currentTimeMillis()
        val db = Firebase.firestore
        for (item in cartViewModel.allOrdersLiveData.value!!){
            val itemCheckout = hashMapOf(
                "orderId" to item.orderId.toString(),
                "itemName" to item.itemName.toString(),
                "itemPrice" to item.itemPrice,
                "itemQuantity" to item.itemQuantity.toString(),
                "date" to currentTime.toString(),
                "history" to true,
                "user" to firebaseAuth.currentUser?.email.toString()
            )

            db.collection("order_history")
                .add(itemCheckout)
                .addOnSuccessListener { documentReference ->
                    Log.d("HomeFragment", "DocumentSnapshot added with ID: ${documentReference.id}")
                    //TODO Clear cart here
                    database = CartDatabase.getInstance(requireActivity())
                    databaseDao = database.cartDao
                    repository = CartRepository(databaseDao)
                    cartViewModelFactory = CartViewModelFactory(repository)
                    cartViewModel = ViewModelProvider(this, cartViewModelFactory)[CartViewModel::class.java]

                    cartViewModel.deleteAll()

                    //TODO Add successful checkout page

                    val builder = AlertDialog.Builder(requireActivity())
                    builder.setTitle("Checkout Successful")
                    builder.setMessage("Thank you for your purchase!")

                    builder.setPositiveButton(android.R.string.yes) { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                    builder.create()
                }
                .addOnFailureListener { e ->
                    Log.w("HomeFragment", "Error adding document", e)
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


