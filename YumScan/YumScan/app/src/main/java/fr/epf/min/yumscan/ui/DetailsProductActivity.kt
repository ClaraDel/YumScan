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
        // On met ?? jour l'ic??ne favoris si on a d??j?? r??cup??r?? les informations du produit
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
            // Retourner voir l'activit?? pr??c??dente lorsqu'on click sur la fl??che
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
     * Envoi de la requ??te ?? l'API pour r??cup??rer les donn??es du produit ?? partir de son code barre
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun synchro() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        // On tente de r??cup??rer les donn??es du produit ?? partir de son code
        val service = retrofit.create(GetProductService::class.java)
        progressBar.visibility = View.VISIBLE
        runBlocking {
            val code = intent.getStringExtra("code")
            if(code != null){
                val result = service.fetchProduct(code)
                if (result.product != null) {
                    // On a r??cup??r?? les donn??es du produit
                    productResult = ProductResult(code, result.product)
                    onProductFetch()
                } else
                    // On n'a pas r??ussi ?? r??cup??rer les donn??es du produit
                    showNotFoundDialog()
            }
            progressBar.visibility = View.INVISIBLE
        }
    }

    /**
     * Appel?? d??s que l'ic??ne favoris est appuy??
     */
    fun onFavorite() {
        // On doit d'assurer que le produit a bien ??t?? trouv??
        if(!::productResult.isInitialized) return
        // Mise ?? jour de la base de donn??es (et de l'ic??ne favoris)
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
     * Appel?? d??s que les donn??es du produit sont r??cup??r??es
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun onProductFetch() {
        // On affiche les informations du produit
        showProductInfo(productResult.fetchedProduct)
        // Ajout ?? la base de donn??es et mise ?? jour de l'ic??ne favoris
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
     * Mise ?? jour l'ic??ne favoris, en s'assurant d'abord qu'il soit bien initialis??
     * @param isFavorite L'??tat de l'ic??ne
     */
    private fun updateFavoriteItem(isFavorite: Boolean) {
        // On doit d'assurer que l'ic??ne favoris ??t?? trouv?? avant de le mettre ?? jour
        if(::mFavoriteItem.isInitialized) mFavoriteItem.setIcon(if (isFavorite) R.drawable.ic_favorite_yellow_24dp else R.drawable.ic_favorite_border_white_24dp)
    }

    /**
     * Affiche les donn??es du produit (nom, quantit??, ingr??dients, nutriscore)
     * @param fetchedProduct Les donn??es du produit r??colt??es depuis l'API
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
            textVegetarien.text = "V??g??tarien"
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
                    textNonVegetarien.text = "Non v??g??tarien"
                }
            }
        }
        if(huilePalme == "en:palm-oil" || huilePalme == "palm-oil") {
            imageHuilePalme.visibility = View.VISIBLE
            textHuilePalme.visibility = View.VISIBLE
        }
        if(vegan.length > 3 && huilePalme.substring(3) == "palm-oil") textHuilePalme.visibility = View.VISIBLE


        // V??rification des allergies
        runBlocking {
            // On r??cup??re les noms d'allerg??nes commun ?? ceux du produit et ?? ceux ajout??s dans les pr??f??rences
            val allergenNames = getAllergenNamesFromCategories(preferencesDAO.getAllergens().map { allergen -> allergen.name })
                .filter { allergen -> fetchedProduct.allergens_from_user.contains(allergen) }
            // S'il y a au moins une allergie de d??tect??e, on affiche les allerg??nes
            if (allergenNames.isNotEmpty()) {
                labelAllergies.visibility = View.VISIBLE
                imageAllergieWarning.visibility = View.VISIBLE
                var mSpannableString = SpannableString("Allergies : pr??sence de " + "${allergenNames.reduce { acc, s -> "$acc, $s" }}")
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
     * Affiche l'image nutriscore du produit ?? partir de la lettre
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
     * Affiche une fen??tre de dialogue expliquant qu'aucun produit n'a ??t?? trouv??
     */
    private fun showNotFoundDialog() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Produit non trouv??")
            .setMessage("D??sol??e, nous n'avons pas trouv?? ce produit :(")
            .setNeutralButton("Ok") { _, _ ->
                finish()
            }
            .show()
    }
}