package fr.epf.min.yumscan.ui

import android.util.Log

/**
 * Récupérer les traductions des allegènes suivant une catégorie
 */
fun getAllergenNamesFromCategory(category: String): List<String> {
    Log.d("DEBUG", "getAllergenNamesFromCategory from $category")
    when(category) {
        "Anhyride sulfureux et sulfites" -> return listOf<String>("anhyride sulfureux et sulfites", "sulphur dioxide and sulphites")
        "Arachides" -> return listOf<String>("arachides", "peanuts")
        "Céleri" -> return listOf<String>("céleri", "celery")
        "Crustacés" -> return listOf<String>("crustacés", "crustaceans")
        "Fruits à coque" -> return listOf<String>("fruits à coque", "nuts")
        "Gluten" -> return listOf<String>("gluten")
        "Graines de sésame" -> return listOf<String>("graines de sésame", "sesame seeds")
        "Lait" -> return listOf<String>("lait", "milk")
        "Mollusques" -> return listOf<String>("mollusques", "molluscs")
        "Moutarde" -> return listOf<String>("moutarde", "mustard")
        "Oeufs" -> return listOf<String>("oeufs", "eggs")
        "Poisson" -> return listOf<String>("poisson", "fish")
        "Soja" -> return listOf<String>("soja", "soybeans")
    }
    return listOf<String>()
}

/**
 * Récupérer les traductions des allegènes suivant une liste de catégories
 */
fun getAllergenNamesFromCategories(categories: List<String>): List<String> = categories.map { category -> getAllergenNamesFromCategory(category) }.flatten()
