package net.nutrishare.app.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import net.nutrishare.app.database.ProductDao
import net.nutrishare.app.model.Product
import net.nutrishare.app.retrofit.OpenFoodFactsAPI

class ProductRepository(private val productDao: ProductDao, private val apiService: OpenFoodFactsAPI) {

    private val _localProducts = MutableLiveData<List<Product>>()
    val localProducts: LiveData<List<Product>> = _localProducts
    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> get() = _products

    init {
        _localProducts.postValue(productDao.getAllProducts())
    }

    suspend fun fetchAndSaveProducts(query: String) {
        try {

            if (query.isEmpty()){
                _localProducts.postValue(productDao.getAllProducts())
                return
            }

            val localProducts = productDao.searchProducts(query)
            if (localProducts.isNotEmpty()) {
                _localProducts.postValue(localProducts)
                return
            }

            val response = apiService.searchProducts(query)
            productDao.clearProducts()
            productDao.insertProducts(response.products)
            _localProducts.postValue(response.products)
        } catch (e: Exception) {
            Log.e("API_EXCEPTION", "Exception: ${e.message}")
        }
    }

    fun searchProductsByCategory(category: String) {
        val filteredList = localProducts.value?.filter {
            it.categories?.contains(category, ignoreCase = true) == true
        } ?: localProducts.value!!

        _products.postValue(filteredList)
    }
}
