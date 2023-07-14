package com.buyit.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData


class CartViewModel(private val repository: CartRepository) : ViewModel() {
    val allOrdersLiveData: LiveData<List<Cart>> = repository.allOrders.asLiveData()

    fun insert(cart: Cart) {
        repository.insert(cart)
    }

    fun update(cart: Cart) {
        repository.update(cart)
    }

    fun deleteById(id: Long){
        val ordersList = allOrdersLiveData.value
        Log.d("OVM", ordersList.toString())
        if (ordersList != null && ordersList.isNotEmpty()){
            //find exercise with same id
            for(cart: Cart in ordersList){
                if(cart.orderId == id){
                    repository.delete(id)
                    return
                }
            }
        }
    }

    fun deleteByItemName(name: String){
        val ordersList = allOrdersLiveData.value
        Log.d("OVM", ordersList.toString())
        if (ordersList != null && ordersList.isNotEmpty()){
            //find exercise with same id
            for(cart: Cart in ordersList){
                if(cart.itemName == name){
                    repository.delete(cart.orderId)
                    return
                }
            }
        }
    }

    fun deleteAll(){
        val ordersList = allOrdersLiveData.value
        if (ordersList != null && ordersList.isNotEmpty())
            repository.deleteAll()
    }
}

class CartViewModelFactory (private val repository: CartRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{
        if(modelClass.isAssignableFrom(CartViewModel::class.java))
            return CartViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}