package fr.epf.min.yumscan.ui.favorites

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fr.epf.min.yumscan.R
import fr.epf.min.yumscan.data.product.model.Product
import fr.epf.min.yumscan.ui.DetailsProductActivity
import kotlinx.android.synthetic.main.product_view.view.*

class ProductAdapter(var products: List<Product>) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(val productView : View) : RecyclerView.ViewHolder(productView)

    override fun getItemCount()  = products.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val inflater : LayoutInflater = LayoutInflater.from(parent.context)
        val view : View =
            inflater.inflate(R.layout.product_view, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        holder.productView.product_name_textview.text = product.name
        Glide.with(holder.productView)
            .load(product.thumbUri)
            .into(holder.productView.product_imageview);
        holder.productView.setOnClickListener {
            val intent = Intent(it.context, DetailsProductActivity::class.java)
            intent.putExtra("code", product.code)
            it.context.startActivity(intent)
        }
    }

    fun replaceAll(newProducts: List<Product>) {
        products = newProducts
    }
}