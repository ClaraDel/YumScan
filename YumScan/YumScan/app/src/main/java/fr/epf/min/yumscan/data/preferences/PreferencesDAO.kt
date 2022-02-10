package fr.epf.min.yumscan.data.preferences

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fr.epf.min.yumscan.data.preferences.model.Allergen
import fr.epf.min.yumscan.data.preferences.model.Diet

@Dao
interface PreferencesDAO {
    @Query("select * from allergens")
    suspend fun getAllergens(): List<Allergen>

    @Query("delete from allergens")
    suspend fun clearAllergens()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllergens(allergens: List<Allergen?>?)

    @Query("select * from diets ")
    suspend fun getDiets(): List<Diet>

    @Query("delete from diets")
    suspend fun clearDiets()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiets(diets: List<Diet?>?)
}