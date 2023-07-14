package com.buyit

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.buyit.database.Cart

class CartAdapter(private val context: Context, private var cartList: List<Cart>) : BaseAdapter() {
    override fun getCount(): Int {
        return cartList.size
    }

    override fun getItem(position: Int): Any {
        return cartList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val listItem = View.inflate(context, R.layout.cart_item, null)
        val cart = cartList[position]
        listItem.findViewById<TextView>(R.id.productName).text = cart.itemName

        when(cart.itemName) {
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

        val price = Util.currencyConversion(cart.itemPrice!!, context)
        val currencySymbol =  Util.getCurrencySymbol(context)
        val priceString = currencySymbol + "%.2f".format(price)
        listItem.findViewById<TextView>(R.id.productPrice).text = priceString

        val quantityString = "${cart.itemQuantity}x"
        listItem.findViewById<TextView>(R.id.ProductQty).text = quantityString
        return listItem
    }

    fun replace(newCartList: List<Cart>){
        cartList = newCartList
    }
}