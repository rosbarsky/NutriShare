package net.nutrishare.app.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.nutrishare.app.data.ProductRepository
import net.nutrishare.app.viewmodel.ProductViewModel

class ProductViewModelFactory(private val repository: ProductRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
