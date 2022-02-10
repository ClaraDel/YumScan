package fr.epf.min.yumscan.ui

import retrofit2.http.GET
import retrofit2.http.Path

interface GetProductService {
    @GET("/api/v0/product/{code}.json")
    suspend fun fetchProduct(@Path("code") code : String): GetProductResult
}

data class GetProductResult(val product: FetchedProduct?)

data class FetchedProduct(
    val product_name : String = "",
    val product_quantity : String = "",
    val nutriscore_grade: String = "",
    val ingredients_text: String = "",
    val image_front_url: String = "",
    val ingredients_analysis_tags: Array<String> = arrayOf("", "", ""),
    val allergens_from_user: String = ""
)
