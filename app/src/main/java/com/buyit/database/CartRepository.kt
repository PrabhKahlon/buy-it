package com.buyit.database


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CartRepository(private val cartDao: CartDao) {

    val allOrders: Flow<List<Cart>> = cartDao.getAllOrders()

    fun insert(cart: Cart){
        CoroutineScope(IO).launch{
            cartDao.insertOrders(cart)
        }
    }

    fun update(cart: Cart) {
        CoroutineScope(IO).launch{
            cartDao.updateOrders(cart)
        }
    }

    fun delete(id: Long){
        CoroutineScope(IO).launch {
            cartDao.deleteOrder(id)
        }
    }

    fun deleteAll(){
        CoroutineScope(IO).launch {
            cartDao.deleteAll()
        }
    }
}