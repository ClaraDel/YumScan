package fr.epf.min.yumscan.data.preferences.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "allergens")
@Parcelize
data class Allergen(
    @PrimaryKey val name: String
) : Parcelable

@Entity(tableName = "diets")
@Parcelize
data class Diet(
    @PrimaryKey val name: String
) : Parcelable