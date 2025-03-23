package net.nutrishare.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.nutrishare.app.model.Product

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("SELECT * FROM products")
    fun getAllProducts(): List<Product>

    @Query("DELETE FROM products")
    suspend fun clearProducts()

    @Query("SELECT * FROM products WHERE LOWER(productName) LIKE '%' || LOWER(:query) || '%' OR LOWER(brands) LIKE '%' || LOWER(:query) || '%'")
    suspend fun searchProducts(query: String): List<Product>


}
