package fr.epf.min.yumscan.data.product

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import fr.epf.min.yumscan.data.product.model.Product
import java.util.*

@Database(entities = [Product::class], version = 1)
@TypeConverters(FavoriteConverter::class)
abstract class ProductDatabase : RoomDatabase(){
    abstract fun getProductDAO() : ProductDAO
}

class FavoriteConverter {
    @TypeConverter
    fun toFavorite(value: Boolean) = if (value) 1 else 0
    @TypeConverter
    fun toBoolean(value: Int) = value == 1
    @TypeConverter
    fun toDate(value: Long): Date = Date(value)
    @TypeConverter
    fun fromDate(date: Date): Long = date.time
}