package fr.epf.min.yumscan.ui.history

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import fr.epf.min.yumscan.R
import fr.epf.min.yumscan.data.product.ProductDatabase
import fr.epf.min.yumscan.data.product.model.Product
import kotlinx.coroutines.runBlocking

class HistoryFragment : Fragment() {
    private lateinit var recyclerView : RecyclerView
    private lateinit var allScannedProducts : MutableList<Product>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        // Ok j'avoue j'ai cherché un peu ^^'
        // explications : https://stackoverflow.com/a/38653443 et https://stackoverflow.com/a/45297610
        recyclerView = root.findViewById(R.id.products_recyclerview) as RecyclerView
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        val productDAO = Room
            .databaseBuilder(requireActivity().applicationContext, ProductDatabase::class.java, "db2")
            .build()
            .getProductDAO()
        runBlocking {
            allScannedProducts = productDAO.getAllProducts().toMutableList()
            recyclerView.adapter = ProductAdapter(allScannedProducts)
        }

        // Pour que onCreateOptionsMenu soit appelé :
        setHasOptionsMenu(true)

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        // Ici on ajoute la loupe à la barre de l'activité
        inflater.inflate(R.menu.search_menu, menu)
        // puis on initialise la barre de recherche
        setupSearchView(menu.findItem(R.id.search_item).actionView as SearchView)
    }

    /**
     * C'est ici qu'on met en place les listeners de la barre de recherche ;)
     * @param root La vue dans laquelle on se place
     */
    private fun setupSearchView(searchView: SearchView){
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }
            override fun onQueryTextChange(query: String): Boolean {
                // Nettoyer le recycler view, explications : https://stackoverflow.com/a/37110764
                recyclerView.recycledViewPool.clear()
                recyclerView.adapter?.notifyDataSetChanged()
                // On doit d'abord s'assurer de bien récupérer la liste entière des produits favoris
                if(!::allScannedProducts.isInitialized) return true
                // si c'est le cas, on filtre cette liste :
                val adapter: ProductAdapter = (recyclerView.adapter as ProductAdapter)
                adapter.replaceAll(allScannedProducts.filter { product -> product.name.toLowerCase().contains(query.toLowerCase()) })
                return true
            }
        })
    }
}