package com.buyit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ProductAdapter(private val context: Context, private var productList: List<Product>) : BaseAdapter() {
    override fun getCount(): Int {
        return productList.size
    }

    override fun getItem(position: Int): Any {
        return productList[position]

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val listItem = View.inflate(context, R.layout.cart_item, null)
        val cart = productList[position]
        listItem.findViewById<TextView>(R.id.productName).text = "Brand: ${cart.brand}\nProduct Name: ${cart.product}"

        when(cart.product) {
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

        val price = Util.currencyConversion(cart.price!!, context)
        val currencySymbol =  Util.getCurrencySymbol(context)
        val priceString = currencySymbol + "%.2f".format(price)
        listItem.findViewById<TextView>(R.id.productPrice).text = priceString

        val quantityString = ""
        listItem.findViewById<TextView>(R.id.ProductQty).text = quantityString
        return listItem
    }

    fun replace(newCartList: List<Product>){
        productList = newCartList
    }
}