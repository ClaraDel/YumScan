package fr.epf.min.yumscan.ui.home

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.google.android.material.textview.MaterialTextView
import fr.epf.min.yumscan.R
import fr.epf.min.yumscan.data.preferences.PreferencesDAO
import fr.epf.min.yumscan.data.preferences.PreferencesDatabase
import fr.epf.min.yumscan.data.preferences.model.Allergen
import fr.epf.min.yumscan.data.preferences.model.Diet
import fr.epf.min.yumscan.data.product.ProductDatabase
import kotlinx.coroutines.runBlocking


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        // Pour que onCreateOptionsMenu soit appelé :
        setHasOptionsMenu(true)
        // là on va initialiser la barre de recherche
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        // On a besoin d'enregistrer et mettre à jour de façon persistante les préférences
        val preferencesDAO = Room
            .databaseBuilder(requireContext(), PreferencesDatabase::class.java, "db_pref")
            .build()
            .getPreferencesDAO()

        // On écoute les changements liés au menu déroulant des allergies
        val spinner = root.findViewById<Spinner>(R.id.allergies_spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // On récupères la liste d'allergènes à bannir
                var allergenNames = listOf((selectedItemView as MaterialTextView).text as String)

                // On enregistre ces allergènes de façon persistante
                runBlocking {
                    preferencesDAO.clearAllergens()
                    preferencesDAO.insertAllergens(allergenNames.map { name -> Allergen(name) })
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // On supprime mes allergènes de façon persistante
                runBlocking {
                    preferencesDAO.clearAllergens()
                }
            }
        }

        // On écoute les changements liés au menu déroulant des allergies
        val spinnerRegime = root.findViewById<Spinner>(R.id.regime_spinner)
        spinnerRegime.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // On récupère le régime
                var regimes = listOf((selectedItemView as MaterialTextView).text as String)

                // On enregistre ce régime de façon persistante
                runBlocking {
                    preferencesDAO.clearDiets()
                    preferencesDAO.insertDiets(regimes.map { regime -> Diet(regime) })
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // On supprime mes allergènes de façon persistante
                runBlocking {
                    preferencesDAO.clearDiets()
                }
            }
        }

        return root
    }
}