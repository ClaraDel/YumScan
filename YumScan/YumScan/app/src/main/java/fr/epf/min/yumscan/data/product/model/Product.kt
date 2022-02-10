package fr.epf.min.yumscan.data.product.model

import android.net.Uri
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "products")
@Parcelize
data class Product(
    @PrimaryKey val code: String,
    val name: String,
    val thumbUri: String,
    val favorite: Boolean,
    val lastScan: Date
) : Parcelable