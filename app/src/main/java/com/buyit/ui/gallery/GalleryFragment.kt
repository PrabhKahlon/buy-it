package com.buyit.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.buyit.R
import com.buyit.databinding.FragmentGalleryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var historyListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        historyListView = root.findViewById(R.id.history_items)
        historyListView.addHeaderView(layoutInflater.inflate(R.layout.history_item_header, null))
        val firebaseAuth = FirebaseAuth.getInstance()

        val currentUser = firebaseAuth.currentUser?.email

        val db = Firebase.firestore
        val orderHistoryList = ArrayList<History>()
        db.collection("order_history").whereEqualTo("user", currentUser).get().addOnSuccessListener { result ->
            for (document in result) {
                val historyItem = History()
                historyItem.date = document.data.getValue("date").toString()
                historyItem.history = document.data.getValue("history").toString().toBoolean()
                historyItem.itemName = document.data.getValue("itemName").toString()
                historyItem.itemPrice = document.data.getValue("itemPrice").toString().toDouble()
                historyItem.itemQuantity = document.data.getValue("itemQuantity").toString()
                historyItem.orderId = document.data.getValue("orderId").toString()
                historyItem.user = document.data.getValue("user").toString()
                orderHistoryList.add(historyItem)
            }
            //display history
            val historyListAdapter = OrderHistoryAdapter(requireActivity(), orderHistoryList)
            historyListView.adapter = historyListAdapter
        }.addOnFailureListener { exception ->
                Toast.makeText(requireActivity(), "Error getting documents: $exception", Toast.LENGTH_SHORT).show()
        }


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}