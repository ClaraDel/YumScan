package fr.epf.min.yumscan.data.preferences

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.epf.min.yumscan.data.preferences.model.Allergen
import fr.epf.min.yumscan.data.preferences.model.Diet

@Database(entities = [Allergen::class, Diet::class], version = 1)
abstract class PreferencesDatabase : RoomDatabase(){
    abstract fun getPreferencesDAO() : PreferencesDAO
}