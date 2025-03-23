package net.nutrishare.app.retrofit

import net.nutrishare.app.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenFoodFactsAPI {
    @GET("cgi/search.pl?search_simple=1&json=1")
    suspend fun searchProducts(
        @Query("search_terms") query: String
    ): SearchResponse
}
