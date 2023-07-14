package com.buyit.ui.gallery

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.buyit.R
import com.buyit.Util
import java.text.SimpleDateFormat
import java.util.*

class OrderHistoryAdapter(private val context: Context, private var historyList: List<History>) : BaseAdapter()  {
    override fun getCount(): Int {
        return historyList.size
    }

    override fun getItem(position: Int): Any {
        return historyList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val listItem = View.inflate(context, R.layout.history_item, null)
        val orderHistory = historyList[position]
        listItem.findViewById<TextView>(R.id.productName).text = orderHistory.itemName

        when(orderHistory.itemName) {
            "Khaki Pants" -> {
                listItem.findViewById<ImageView>(R.id.productImage).setImageResource(R.drawable.khahi_pants_temp)
            }
            "Pants" -> {
                listItem.findViewById<ImageView>(R.id.productImage).setImageResource(R.drawable.pants_temp)
            }
            "Hat" -> {
                listItem.findViewById<ImageView>(R.id.productImage).setImageResource(R.drawable.hat_temp)
            }
            "T-Shirt" -> {
                listItem.findViewById<ImageView>(R.id.productImage).setImageResource(R.drawable.shirt_temp)
            }
            "Jacket" -> {
                listItem.findViewById<ImageView>(R.id.productImage).setImageResource(R.drawable.jacket_temp)
            }
        }

        listItem.findViewById<TextView>(R.id.orderId).text = orderHistory.orderId

        val formatter = SimpleDateFormat("MM/dd/yyyy")
        val dateString = formatter.format(Date(orderHistory.date.toLong()))
        listItem.findViewById<TextView>(R.id.orderDate).text = dateString

        val price = Util.currencyConversion(orderHistory.itemPrice!!, context)
        val currencySymbol =  Util.getCurrencySymbol(context)
        val priceString = currencySymbol + "%.2f".format(price)
        listItem.findViewById<TextView>(R.id.productPrice).text = priceString

        val quantityString = "${orderHistory.itemQuantity}x"
        listItem.findViewById<TextView>(R.id.ProductQty).text = quantityString
        return listItem
    }
}