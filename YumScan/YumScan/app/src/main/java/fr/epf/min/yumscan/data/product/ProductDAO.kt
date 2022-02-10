package fr.epf.min.yumscan.data.product

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.epf.min.yumscan.data.product.model.Product

@Dao
interface ProductDAO {
    @Query("select * from products order by lastScan")
    suspend fun getAllProducts(): List<Product>

    @Query("select * from products where favorite is 1")
    suspend fun getFavProducts(): List<Product>

    @Query("update products set favorite = :favorite where code = :code")
    suspend fun favProduct(code: String, favorite: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addProduct(product: Product)

    @Query("select count(*)!=0 from products where code = :code and favorite = 1 ")
    suspend fun isFavorite(code: String): Boolean
}