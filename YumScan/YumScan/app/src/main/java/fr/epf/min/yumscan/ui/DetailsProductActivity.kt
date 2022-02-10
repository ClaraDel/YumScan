package fr.epf.min.yumscan.ui

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import fr.epf.min.yumscan.R
import fr.epf.min.yumscan.data.preferences.PreferencesDAO
import fr.epf.min.yumscan.data.preferences.PreferencesDatabase
import fr.epf.min.yumscan.data.product.ProductDAO
import fr.epf.min.yumscan.data.product.ProductDatabase
import fr.epf.min.yumscan.data.product.model.Product
import kotlinx.android.synthetic.main.activity_details_product.*
import kotlinx.coroutines.runBlocking
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Instant
import java.util.*

data class ProductResult(val code: String, val fetchedProduct: FetchedProduct)

class DetailsProductActivity : AppCompatActivity() {
    private lateinit var mFavoriteItem: MenuItem
    private lateinit var productResult: ProductResult
    private lateinit var productDAO: ProductDAO
    private lateinit var preferencesDAO: PreferencesDAO

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_product)

        productDAO = Room
            .databaseBuilder(this, ProductDatabase::class.java, "db2")
            .build()
            .getProductDAO()

        preferencesDAO = Room
            .databaseBuilder(this, PreferencesDatabase::class.java, "db_pref")
            .build()
            .getPreferencesDAO()

        synchro()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.product_details_menu, menu)
        mFavoriteItem = menu.findItem(R.id.favorite);
        // On met à jour l'icône favoris si on a déjà récupéré les informations du produit
        if(::productResult.isInitialized) {
            runBlocking {
                val isFavorite = productDAO.isFavorite(productResult.code)
                updateFavoriteItem(isFavorite)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            // Retourner voir l'activité précédente lorsqu'on click sur la flèche
            android.R.id.home -> {
                finish()
            }
            R.id.favorite -> {
                onFavorite()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Envoi de la requête à l'API pour récupérer les données du produit à partir de son code barre
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun synchro() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        // On tente de récupérer les données du produit à partir de son code
        val service = retrofit.create(GetProductService::class.java)
        progressBar.visibility = View.VISIBLE
        runBlocking {
            val code = intent.getStringExtra("code")
            if(code != null){
                val result = service.fetchProduct(code)
                if (result.product != null) {
                    // On a récupéré les données du produit
                    productResult = ProductResult(code, result.product)
                    onProductFetch()
                } else
                    // On n'a pas réussi à récupérer les données du produit
                    showNotFoundDialog()
            }
            progressBar.visibility = View.INVISIBLE
        }
    }

    /**
     * Appelé dès que l'icône favoris est appuyé
     */
    fun onFavorite() {
        // On doit d'assurer que le produit a bien été trouvé
        if(!::productResult.isInitialized) return
        // Mise à jour de la base de données (et de l'icône favoris)
        runBlocking {
            val isFavorite = productDAO.isFavorite(productResult.code)
            productDAO.favProduct(productResult.code, !isFavorite)
            Toast.makeText(
                applicationContext,
                if(!isFavorite) R.string.toast_favorite else R.string.toast_unfavorite,
                Toast.LENGTH_SHORT
            ).show()
            updateFavoriteItem(!isFavorite)
        }
    }

    /**
     * Appelé dès que les données du produit sont récupérées
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onProductFetch() {
        // On affiche les informations du produit
        showProductInfo(productResult.fetchedProduct)
        // Ajout à la base de données et mise à jour de l'icône favoris
        runBlocking {
            val productName = productResult.fetchedProduct.product_name
            val productThumbUri = productResult.fetchedProduct.image_front_url
            val isFavorite = productDAO.isFavorite(productResult.code)
            productDAO.addProduct(
                Product(productResult.code, productName, productThumbUri, isFavorite, Date.from(
                Instant.now()))
            )
        }
    }

    /**
     * Mise à jour l'icône favoris, en s'assurant d'abord qu'il soit bien initialisé
     * @param isFavorite L'état de l'icône
     */
    private fun updateFavoriteItem(isFavorite: Boolean) {
        // On doit d'assurer que l'icône favoris été trouvé avant de le mettre à jour
        if(::mFavoriteItem.isInitialized) mFavoriteItem.setIcon(if (isFavorite) R.drawable.ic_favorite_yellow_24dp else R.drawable.ic_favorite_border_white_24dp)
    }

    /**
     * Affiche les données du produit (nom, quantité, ingrédients, nutriscore)
     * @param fetchedProduct Les données du produit récoltées depuis l'API
     */
    private fun showProductInfo(fetchedProduct: FetchedProduct) {
        imageVegetarien.visibility = View.INVISIBLE
        imageVegetarienWarning.visibility = View.INVISIBLE
        textNonVegetarien.visibility = View.INVISIBLE
        imageHuilePalme.visibility = View.INVISIBLE
        textVegetarien.visibility = View.INVISIBLE
        textHuilePalme.visibility = View.INVISIBLE
        // Affichage des informations sur le xml
        nomProduct.text = fetchedProduct.product_name
        quantityProduct.text = fetchedProduct.product_quantity + " g"
        textIngredients.text = fetchedProduct.ingredients_text
        val nutriscoreLetter = fetchedProduct.nutriscore_grade
        showNutriscore(nutriscoreLetter)
        val huilePalme = fetchedProduct.ingredients_analysis_tags[0].toLowerCase()
        val vegan = fetchedProduct.ingredients_analysis_tags[1].toLowerCase()
        val vegetarian = fetchedProduct.ingredients_analysis_tags[2].toLowerCase()

        val isVegan = vegan == "en:vegan" || vegan == "vegan"
        val isVegetarien = vegetarian == "en:vegetarian" || vegetarian == "vegetarian"
        if(isVegan){
            imageVegetarien.visibility = View.VISIBLE
            textVegetarien.visibility = View.VISIBLE
            textVegetarien.text = "Vegan"
        } else if (isVegetarien){
            imageVegetarien.visibility = View.VISIBLE
            textVegetarien.visibility = View.VISIBLE
            textVegetarien.text = "Végétarien"
            runBlocking {
                if (preferencesDAO.getDiets().map { diet -> diet.name }.contains("Vegan")) {
                    imageVegetarienWarning.visibility = View.VISIBLE
                    textNonVegetarien.visibility = View.VISIBLE
                    textNonVegetarien.text = "Non vegan"
                }
            }
        } else {
            runBlocking {
                if (preferencesDAO.getDiets().map { diet -> diet.name }.isNotEmpty()) {
                    imageVegetarienWarning.visibility = View.VISIBLE
                    textNonVegetarien.visibility = View.VISIBLE
                    textNonVegetarien.text = "Non végétarien"
                }
            }
        }
        if(huilePalme == "en:palm-oil" || huilePalme == "palm-oil") {
            imageHuilePalme.visibility = View.VISIBLE
            textHuilePalme.visibility = View.VISIBLE
        }
        if(vegan.length > 3 && huilePalme.substring(3) == "palm-oil") textHuilePalme.visibility = View.VISIBLE


        // Vérification des allergies
        runBlocking {
            // On récupère les noms d'allergènes commun à ceux du produit et à ceux ajoutés dans les préférences
            val allergenNames = getAllergenNamesFromCategories(preferencesDAO.getAllergens().map { allergen -> allergen.name })
                .filter { allergen -> fetchedProduct.allergens_from_user.contains(allergen) }
            // S'il y a au moins une allergie de détectée, on affiche les allergènes
            if (allergenNames.isNotEmpty()) {
                labelAllergies.visibility = View.VISIBLE
                imageAllergieWarning.visibility = View.VISIBLE
                var mSpannableString = SpannableString("Allergies : présence de " + "${allergenNames.reduce { acc, s -> "$acc, $s" }}")
                mSpannableString.setSpan(ForegroundColorSpan(Color.RED), 23, mSpannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                labelAllergies.text = mSpannableString
            } else {
                labelAllergies.visibility = View.INVISIBLE
                imageAllergieWarning.visibility = View.INVISIBLE
            }
        }

        Glide.with(this@DetailsProductActivity)
            .load(fetchedProduct.image_front_url)
            .into(imageProduct);
    }

    /**
     * Affiche l'image nutriscore du produit à partir de la lettre
     * @param nutriscoreLetter Lettre nutriscore
     */
    private fun showNutriscore(nutriscoreLetter: String?) = when (nutriscoreLetter) {
        "a" -> imageNutriscore.setImageResource(R.drawable.nutriscore_a)
        "b" -> imageNutriscore.setImageResource(R.drawable.nutriscore_b)
        "c" -> imageNutriscore.setImageResource(R.drawable.nutriscore_c)
        "d" -> imageNutriscore.setImageResource(R.drawable.nutriscore_d)
        "e" -> imageNutriscore.setImageResource(R.drawable.nutriscore_e)
        else -> imageNutriscore.setBackgroundColor(getResources().getColor(android.R.color.white));
    }

    /**
     * Affiche une fenêtre de dialogue expliquant qu'aucun produit n'a été trouvé
     */
    private fun showNotFoundDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Produit non trouvé")
            .setMessage("Désolée, nous n'avons pas trouvé ce produit :(")
            .setNeutralButton("Ok") { _, _ ->
                finish()
            }
            .show()
    }
}