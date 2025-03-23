package net.nutrishare.app.viewmodel

import androidx.lifecycle.*
import kotlinx.coroutines.launch
import net.nutrishare.app.data.ProductRepository
import net.nutrishare.app.model.Product

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {

    val products: LiveData<List<Product>> = repository.localProducts
    val categoryProducts: LiveData<List<Product>> = repository.products

    fun searchProducts(query: String) {

        viewModelScope.launch {
            repository.fetchAndSaveProducts(query)
        }
    }

    fun searchProductsByCategory(category: String) {
        viewModelScope.launch {
            repository.searchProductsByCategory(category)
        }
    }
}
